package com.websecuritylab.security;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.stereotype.Service;

/** Synchronizer-token CSRF protection, deliberately implemented step by step. */
@Service
public class CsrfTokenService {

  private static final String SESSION_KEY = "csrfToken";
  private final SecureRandomService secureRandomService;

  public CsrfTokenService(SecureRandomService secureRandomService) {
    this.secureRandomService = secureRandomService;
  }

  public String createCsrfToken() {
    return secureRandomService.generateSecureToken(32);
  }

  public void storeCsrfTokenInSession(HttpSession session, String token) {
    session.setAttribute(SESSION_KEY, token);
  }

  public boolean validateCsrfToken(HttpSession session, String providedToken) {
    Object stored = session.getAttribute(SESSION_KEY);
    if (
      !(stored instanceof String storedToken) || providedToken == null
    ) return false;
    return MessageDigest.isEqual(
      storedToken.getBytes(StandardCharsets.UTF_8),
      providedToken.getBytes(StandardCharsets.UTF_8)
    );
  }
}
