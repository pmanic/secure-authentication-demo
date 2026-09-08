package com.websecuritylab.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordSecurityServiceTest {

  private final PasswordSecurityService service = new PasswordSecurityService(
    new SecureRandomService()
  );

  @Test
  void differentSaltsProduceDifferentHashes() {
    byte[] a = service.generateSalt(),
      b = service.generateSalt();
    assertFalse(
      java.util.Arrays.equals(
        service.derivePasswordHash("Test123!".toCharArray(), a, 10_000),
        service.derivePasswordHash("Test123!".toCharArray(), b, 10_000)
      )
    );
  }

  @Test
  void correctPasswordPassesVerification() {
    byte[] salt = service.generateSalt();
    byte[] hash = service.derivePasswordHash(
      "Test123!".toCharArray(),
      salt,
      10_000
    );
    assertTrue(
      service.verifyPassword("Test123!".toCharArray(), salt, 10_000, hash)
    );
  }

  @Test
  void wrongPasswordFailsVerification() {
    byte[] salt = service.generateSalt();
    byte[] hash = service.derivePasswordHash(
      "Test123!".toCharArray(),
      salt,
      10_000
    );
    assertFalse(
      service.verifyPassword("wrong-pass".toCharArray(), salt, 10_000, hash)
    );
  }
}
