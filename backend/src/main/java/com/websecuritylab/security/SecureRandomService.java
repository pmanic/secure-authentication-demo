package com.websecuritylab.security;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Service;

/** Makes every security-sensitive random step visible for the demonstration. */
@Service
public class SecureRandomService {

  private final SecureRandom secureRandom = new SecureRandom();

  public byte[] generateRandomBytes(int length) {
    byte[] randomBytes = new byte[length];
    secureRandom.nextBytes(randomBytes);
    return randomBytes;
  }

  public String generateSecureToken(int numberOfBytes) {
    return Base64.getUrlEncoder()
      .withoutPadding()
      .encodeToString(generateRandomBytes(numberOfBytes));
  }
}
