package com.websecuritylab.repository.secure;

import com.websecuritylab.model.User;
import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Secure database access: every user-controlled value is a PreparedStatement parameter. */
@Repository
public class SecureUserRepository {

  private final JdbcTemplate jdbc;

  public SecureUserRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<User> findByEmail(String email) {
    return jdbc.execute(
      connection -> {
        PreparedStatement statement = connection.prepareStatement(
          "select * from secure_users where email = ?"
        );
        statement.setString(1, email); // Data remains data; it cannot change SQL syntax.
        return statement;
      },
      (PreparedStatement preparedStatement) -> {
        try (var rs = preparedStatement.executeQuery()) {
          return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        }
      }
    );
  }

  public Optional<User> findById(long id) {
    return jdbc
      .query("select * from secure_users where id=?", (rs, n) -> map(rs), id)
      .stream()
      .findFirst();
  }

  public void create(
    String name,
    String email,
    String hash,
    String salt,
    int iterations
  ) {
    jdbc.update(
      "insert into secure_users(display_name,email,password_hash,password_salt,password_iterations) values (?,?,?,?,?)",
      name,
      email,
      hash,
      salt,
      iterations
    );
  }

  public void updateEmail(long id, String email) {
    jdbc.update("update secure_users set email=? where id=?", email, id);
  }

  public void updateMessage(long id, String message) {
    jdbc.update(
      "update secure_users set profile_message=? where id=?",
      message,
      id
    );
  }

  public void incrementFailedAttempts(long id) {
    jdbc.update(
      "update secure_users set failed_login_attempts=failed_login_attempts+1 where id=?",
      id
    );
  }

  public void resetFailedAttempts(long id) {
    jdbc.update(
      "update secure_users set failed_login_attempts=0 where id=?",
      id
    );
  }

  private User map(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new User(
      rs.getLong("id"),
      rs.getString("display_name"),
      rs.getString("email"),
      rs.getString("password_hash"),
      rs.getString("password_salt"),
      rs.getInt("password_iterations"),
      rs.getString("profile_message"),
      rs.getInt("failed_login_attempts")
    );
  }
}
