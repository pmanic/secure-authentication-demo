# Web Security Laboratory

Studentski projekat za **BG/NIS – Praktikum: Zaštita u informacionim sistemima / Sajber bezbednost**. Poredi namerno ranjivu lokalnu demonstraciju sa zaštićenom implementacijom autentifikacije, SQL Injection, XSS i CSRF problema.

> Unsafe API postoji isključivo za lokalnu laboratorijsku demonstraciju sa izmišljenim podacima. Ne objavljivati ga na internetu.

## Arhitektura i tehnologije

`Browser → Vanilla HTML/CSS/JS + Fetch → Spring Boot REST API → JDBC → local H2 database`

Java 17, Spring Boot 3.3, Spring Web, JdbcTemplate/direktni `PreparedStatement`, H2 i Maven. H2 je ugrađena relaciona SQL baza: ne zahteva instalaciju servera, nalog, internet ili database credentials, ali i dalje stvarno izvršava SQL upite potrebne za demonstraciju.

## Pokretanje

Preduslovi su JDK 17+ i Maven 3.9+.

1. Nisu potrebni Supabase ni environment podaci za bazu. Ako su `DB_URL`, `DB_USERNAME` i `DB_PASSWORD` ranije postavljeni, ukloni ih ili otvori nov terminal.
2. Pokreni backend: `cd backend`, zatim `mvn spring-boot:run`. Spring automatski pravi lokalnu bazu i izvršava `schema.sql` i `data.sql` iz resources foldera.
3. Pokreni frontend iz root-a, npr. VS Code Live Server na `http://localhost:5500/frontend/`, ili `python -m http.server 5500 --directory frontend`.
4. Unsafe demo već ima `student@example.com` / `Test123!`. Na secure strani prvo registruj iste demo podatke; hash i salt se tada bezbedno generišu.

Lokalni podaci se čuvaju u `backend/data/`, koji je ignorisan u Git-u. H2 konzola je dostupna dok backend radi na `http://localhost:8080/h2-console` sa JDBC URL-om `jdbc:h2:file:./data/web-security-lab;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH`, korisnikom `sa` i praznom lozinkom.

Za HTTPS deployment postavi `COOKIE_SECURE=true`. Za lokalni HTTP ostaje `false`.

## Šta se poredi

| Oblast         | Unsafe                         | Secure                                                                        |
| -------------- | ------------------------------ | ----------------------------------------------------------------------------- |
| SQL            | konkatenacija inputa           | `PreparedStatement` parametar                                                 |
| Lozinka        | plaintext                      | PBKDF2-HMAC-SHA-256, jedinstven 16-byte salt, 310.000 iteracija, 256-bit hash |
| XSS            | namenski `innerHTML` container | `textContent`                                                                 |
| CSRF           | nema provere                   | 256-bitni `SecureRandom` token u sesiji i `X-CSRF-Token` headeru              |
| Validacija     | praktično izostavljena         | server ponovo proverava svaki input                                           |
| Login pokušaji | bez ograničenja                | zaključavanje nakon 5 neuspeha                                                |

Frontend validacija poboljšava UX, ali nije bezbednosna granica: HTTP zahtev se može poslati bez frontend forme, zato secure backend sve proverava ponovo.

## Testovi

Iz `backend/` pokreni `mvn test`. Testovi proveravaju različite salt/hash rezultate, ispravnu i pogrešnu lozinku, validan/nevalidan/nedostajući CSRF token i jedinstvenost tokena. Repository testovi koriste izolovanu H2 bazu i proveravaju pronalaženje korisnika, nepostojeći email i tretiranje SQL Injection unosa kao podatka.

## Manual demonstration scenarios

| Scenario        | Preduslov i akcija                                                         | Unsafe očekivanje       | Secure očekivanje                    | Pokazuje                     |
| --------------- | -------------------------------------------------------------------------- | ----------------------- | ------------------------------------ | ---------------------------- |
| Normal login    | Seed/registracija; unesi demo kredencijale                                 | uspeh                   | uspeh + CSRF token                   | normalan tok                 |
| SQL Injection   | Samo lokalno: u unsafe email unesi `' OR '1'='1' --` i proizvoljnu lozinku | upit može biti izmenjen | vrednost se tretira samo kao podatak | konkatenacija vs parametar   |
| XSS             | Sačuvaj `<img src=x onerror=alert(1)>` kao poruku                          | browser tumači HTML     | prikazuje se doslovan tekst          | `innerHTML` vs `textContent` |
| CSRF            | Pošalji secure promenu emaila bez headera                                  | unsafe promena prolazi  | secure vraća 403                     | synchronizer token           |
| Lozinke u bazi  | Pregledaj obe demo tabele                                                  | vidljiv plaintext       | samo Base64 hash/salt/iterations     | pravilno čuvanje             |
| Failed attempts | Pet puta pogreši secure lozinku                                            | nema limita             | nalog se privremeno zaključava       | osnovna zaštita od pogađanja |

Za reset izvrši [reset-demo.sql](database/reset-demo.sql) u H2 konzoli, zatim ponovo registruj secure demo korisnika. Za potpuni reset zaustavi backend i obriši generisani `backend/data/` folder; sledeće pokretanje ponovo pravi bazu i unsafe demo korisnika.

## Struktura

- `backend/` – Spring Boot aplikacija i testovi
- `frontend/` – statički vanilla interfejs
- `backend/src/main/resources/schema.sql` – definicija tabela koju aplikacija izvršava pri pokretanju
- `backend/src/main/resources/data.sql` – inicijalizacija demo korisnika
- `database/reset-demo.sql` – ručno resetovanje demo podataka
- [Projekat_Bezbednost_Web_Aplikacije.docx](Projekat_Bezbednost_Web_Aplikacije.docx) – akademski rad sa arhitekturom i objašnjenjima za odbranu

Preporučene VS Code ekstenzije: **Extension Pack for Java**, **Spring Boot Extension Pack**, **Live Server**.
