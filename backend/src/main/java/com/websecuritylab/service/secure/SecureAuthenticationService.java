package com.websecuritylab.service.secure;

import com.websecuritylab.model.User;
import com.websecuritylab.repository.secure.SecureUserRepository;
import com.websecuritylab.security.PasswordSecurityService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SecureAuthenticationService {

  private static final int MAX_FAILED_ATTEMPTS = 5;
  private final SecureUserRepository users;
  private final PasswordSecurityService passwords;
  private final InputValidator validator;

  public SecureAuthenticationService(
    SecureUserRepository users,
    PasswordSecurityService passwords,
    InputValidator validator
  ) {
    this.users = users;
    this.passwords = passwords;
    this.validator = validator;
  }

  public void register(String name, String email, String rawPassword) {
    validator.validateRegistration(name, email, rawPassword);
    byte[] salt = passwords.generateSalt();
    byte[] hash = passwords.derivePasswordHash(
      rawPassword.toCharArray(),
      salt,
      PasswordSecurityService.DEFAULT_ITERATIONS
    );
    users.create(
      name.trim(),
      email.toLowerCase(),
      passwords.encodeToBase64(hash),
      passwords.encodeToBase64(salt),
      PasswordSecurityService.DEFAULT_ITERATIONS
    );
  }

  public Optional<User> authenticate(String email, String rawPassword) {
    validator.validateEmail(email);
    Optional<User> found = users.findByEmail(email.toLowerCase());
    if (found.isEmpty()) return Optional.empty();
    User user = found.get();
    if (
      user.failedLoginAttempts() >= MAX_FAILED_ATTEMPTS
    ) throw new IllegalStateException(
      "Account is temporarily locked after 5 failed attempts"
    );
    boolean valid = passwords.verifyPassword(
      rawPassword.toCharArray(),
      passwords.decodeFromBase64(user.passwordSalt()),
      user.passwordIterations(),
      passwords.decodeFromBase64(user.passwordHash())
    );
    if (!valid) {
      users.incrementFailedAttempts(user.id());
      return Optional.empty();
    }
    users.resetFailedAttempts(user.id());
    return Optional.of(user);
  }
}
