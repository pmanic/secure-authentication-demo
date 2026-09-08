package com.websecuritylab.repository.unsafe;

import com.websecuritylab.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * EDUCATIONAL VULNERABLE IMPLEMENTATION. DO NOT USE THIS CODE IN PRODUCTION.
 * User input is intentionally concatenated into SQL to demonstrate SQL injection.
 */
@Repository
public class UnsafeUserRepository {

  private final JdbcTemplate jdbc;

  public UnsafeUserRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<User> findByCredentials(String email, String password) {
    String sql =
      "select * from unsafe_users where email = '" +
      email +
      "' and password_plaintext = '" +
      password +
      "'";
    List<User> users = jdbc.query(sql, (rs, n) -> map(rs));
    return users.stream().findFirst();
  }

  public Optional<User> findById(long id) {
    return jdbc
      .query("select * from unsafe_users where id=?", (rs, n) -> map(rs), id)
      .stream()
      .findFirst();
  }

  public void create(String name, String email, String password) {
    // Also intentionally unsafe: values and plaintext password are placed directly in SQL.
    jdbc.execute(
      "insert into unsafe_users(display_name,email,password_plaintext) values ('" +
        name +
        "','" +
        email +
        "','" +
        password +
        "')"
    );
  }

  public void updateEmail(long id, String email) {
    jdbc.execute(
      "update unsafe_users set email='" + email + "' where id=" + id
    );
  }

  public void updateMessage(long id, String message) {
    jdbc.update(
      "update unsafe_users set profile_message=? where id=?",
      message,
      id
    );
  }

  private User map(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new User(
      rs.getLong("id"),
      rs.getString("display_name"),
      rs.getString("email"),
      rs.getString("password_plaintext"),
      null,
      0,
      rs.getString("profile_message"),
      rs.getInt("failed_login_attempts")
    );
  }
}
