package com.websecuritylab.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Service;

/** Explicit PBKDF2 password derivation using the standard Java cryptography API. */
@Service
public class PasswordSecurityService {

  public static final int DEFAULT_ITERATIONS = 310_000;
  private static final int SALT_BYTES = 16;
  private static final int HASH_BITS = 256;
  private final SecureRandomService secureRandomService;

  public PasswordSecurityService(SecureRandomService secureRandomService) {
    this.secureRandomService = secureRandomService;
  }

  public byte[] generateSalt() {
    return secureRandomService.generateRandomBytes(SALT_BYTES);
  }

  public byte[] derivePasswordHash(
    char[] password,
    byte[] salt,
    int iterations
  ) {
    PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance(
        "PBKDF2WithHmacSHA256"
      );
      return factory.generateSecret(keySpec).getEncoded();
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException(
        "Required PBKDF2 algorithm is unavailable",
        exception
      );
    } finally {
      keySpec.clearPassword();
    }
  }

  public boolean verifyPassword(
    char[] password,
    byte[] salt,
    int iterations,
    byte[] expectedHash
  ) {
    byte[] actualHash = derivePasswordHash(password, salt, iterations);
    return MessageDigest.isEqual(actualHash, expectedHash);
  }

  public String encodeToBase64(byte[] value) {
    return Base64.getEncoder().encodeToString(value);
  }

  public byte[] decodeFromBase64(String value) {
    return Base64.getDecoder().decode(value);
  }
}
