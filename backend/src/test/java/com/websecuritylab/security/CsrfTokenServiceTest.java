package com.websecuritylab.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;

class CsrfTokenServiceTest {

  private final CsrfTokenService service = new CsrfTokenService(
    new SecureRandomService()
  );
  private final HttpSession session = mock(HttpSession.class);

  @Test
  void validTokenPasses() {
    when(session.getAttribute("csrfToken")).thenReturn("token");
    assertTrue(service.validateCsrfToken(session, "token"));
  }

  @Test
  void invalidTokenFails() {
    when(session.getAttribute("csrfToken")).thenReturn("token");
    assertFalse(service.validateCsrfToken(session, "different"));
  }

  @Test
  void missingTokenFails() {
    when(session.getAttribute("csrfToken")).thenReturn("token");
    assertFalse(service.validateCsrfToken(session, null));
  }

  @Test
  void generatedTokensAreUnique() {
    assertNotEquals(service.createCsrfToken(), service.createCsrfToken());
  }
}
