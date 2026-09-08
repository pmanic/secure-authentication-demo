package com.websecuritylab.service.secure;

import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class InputValidator {

  private static final Pattern EMAIL = Pattern.compile(
    "^[^@\\s]{1,64}@[^@\\s]{1,189}$"
  );

  public void validateRegistration(String name, String email, String password) {
    if (
      name == null || name.isBlank() || name.length() > 80
    ) throw new IllegalArgumentException(
      "Display name must contain 1-80 characters"
    );
    validateEmail(email);
    if (
      password == null || password.length() < 8 || password.length() > 128
    ) throw new IllegalArgumentException(
      "Password must contain 8-128 characters"
    );
  }

  public void validateEmail(String email) {
    if (
      email == null || email.length() > 254 || !EMAIL.matcher(email).matches()
    ) throw new IllegalArgumentException("Email format is invalid");
  }

  public void validateMessage(String message) {
    if (
      message == null || message.length() > 500
    ) throw new IllegalArgumentException(
      "Message may contain at most 500 characters"
    );
  }
}
