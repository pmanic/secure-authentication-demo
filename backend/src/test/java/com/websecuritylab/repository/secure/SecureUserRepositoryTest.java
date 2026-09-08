package com.websecuritylab.repository.secure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

/** Uses an isolated in-memory database; local demo data is not modified. */
@JdbcTest
@Import(SecureUserRepository.class)
class SecureUserRepositoryTest {

  @Autowired
  private SecureUserRepository users;

  @Test
  void findsUserByExactEmail() {
    users.create("Test Student", "repository@example.com", "test-hash", "test-salt", 310_000);

    var found = users.findByEmail("repository@example.com");

    assertTrue(found.isPresent());
    assertEquals("Test Student", found.orElseThrow().displayName());
    assertEquals(310_000, found.orElseThrow().passwordIterations());
  }

  @Test
  void returnsEmptyWhenEmailDoesNotExist() {
    assertTrue(users.findByEmail("missing@example.com").isEmpty());
  }

  @Test
  void treatsSqlSyntaxAsEmailData() {
    users.create("Test Student", "repository@example.com", "test-hash", "test-salt", 310_000);

    assertTrue(users.findByEmail("' OR '1'='1' --").isEmpty());
  }
}
