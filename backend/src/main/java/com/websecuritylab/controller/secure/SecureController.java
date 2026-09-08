package com.websecuritylab.controller.secure;

import com.websecuritylab.dto.*;
import com.websecuritylab.model.User;
import com.websecuritylab.repository.secure.SecureUserRepository;
import com.websecuritylab.security.CsrfTokenService;
import com.websecuritylab.service.secure.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/** Protected flow: server validation, PBKDF2, parameterized SQL, session auth and CSRF tokens. */
@RestController
@RequestMapping("/api/secure")
public class SecureController {

  private final SecureAuthenticationService authentication;
  private final SecureUserRepository users;
  private final CsrfTokenService csrf;
  private final InputValidator validator;

  public SecureController(
    SecureAuthenticationService a,
    SecureUserRepository u,
    CsrfTokenService c,
    InputValidator v
  ) {
    authentication = a;
    users = u;
    csrf = c;
    validator = v;
  }

  @PostMapping("/register")
  public ApiResponse register(@RequestBody RegisterRequest r) {
    authentication.register(r.displayName(), r.email(), r.password());
    return ApiResponse.ok(
      "Secure registration created a PBKDF2 hash and unique salt"
    );
  }

  @PostMapping("/login")
  public ApiResponse login(@RequestBody AuthRequest r, HttpSession session) {
    return authentication
      .authenticate(r.email(), r.password())
      .map(user -> {
        session.setAttribute("secureUserId", user.id());
        String token = csrf.createCsrfToken();
        csrf.storeCsrfTokenInSession(session, token);
        return ApiResponse.ok(
          "Secure login successful",
          Map.of("profile", profile(user), "csrfToken", token)
        );
      })
      .orElseGet(() ->
        ApiResponse.error("Invalid credentials", "INVALID_CREDENTIALS")
      );
  }

  @GetMapping("/profile")
  public ApiResponse profile(HttpSession s) {
    return current(s)
      .map(u -> ApiResponse.ok("Profile loaded", profile(u)))
      .orElseGet(() -> ApiResponse.error("Login required", "UNAUTHORIZED"));
  }

  @PostMapping("/profile/message")
  public ApiResponse message(
    @RequestBody MessageRequest r,
    @RequestHeader(value = "X-CSRF-Token", required = false) String token,
    HttpSession s
  ) {
    requireCsrf(s, token);
    validator.validateMessage(r.message());
    User u = require(s);
    users.updateMessage(u.id(), r.message());
    return ApiResponse.ok("Profile message saved safely");
  }

  @PostMapping("/profile/email")
  public ApiResponse email(
    @RequestBody EmailChangeRequest r,
    @RequestHeader(value = "X-CSRF-Token", required = false) String token,
    HttpSession s
  ) {
    requireCsrf(s, token);
    validator.validateEmail(r.email());
    User u = require(s);
    users.updateEmail(u.id(), r.email().toLowerCase());
    return ApiResponse.ok("Email changed after CSRF verification");
  }

  private void requireCsrf(HttpSession s, String token) {
    if (!csrf.validateCsrfToken(s, token)) throw new InvalidCsrfException();
  }

  private java.util.Optional<User> current(HttpSession s) {
    Object id = s.getAttribute("secureUserId");
    return id instanceof Long n
      ? users.findById(n)
      : java.util.Optional.empty();
  }

  private User require(HttpSession s) {
    return current(s).orElseThrow(() ->
      new SecurityException("Login required")
    );
  }

  private Map<String, Object> profile(User u) {
    return Map.of(
      "displayName",
      u.displayName(),
      "email",
      u.email(),
      "message",
      u.profileMessage()
    );
  }

  public static class InvalidCsrfException extends RuntimeException {}
}
