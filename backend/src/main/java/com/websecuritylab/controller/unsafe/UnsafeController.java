package com.websecuritylab.controller.unsafe;

import com.websecuritylab.dto.*;
import com.websecuritylab.model.User;
import com.websecuritylab.repository.unsafe.UnsafeUserRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/** EDUCATIONAL VULNERABLE IMPLEMENTATION. Never expose this controller publicly. */
@RestController
@RequestMapping("/api/unsafe")
public class UnsafeController {

  private final UnsafeUserRepository users;

  public UnsafeController(UnsafeUserRepository users) {
    this.users = users;
  }

  @PostMapping("/register")
  public ApiResponse register(@RequestBody RegisterRequest r) {
    users.create(r.displayName(), r.email(), r.password());
    return ApiResponse.ok("Unsafe registration saved the plaintext password");
  }

  @PostMapping("/login")
  public ApiResponse login(@RequestBody AuthRequest r, HttpSession session) {
    return users
      .findByCredentials(r.email(), r.password())
      .map(user -> {
        session.setAttribute("unsafeUserId", user.id());
        return ApiResponse.ok("Unsafe login successful", profile(user));
      })
      .orElseGet(() ->
        ApiResponse.error("Invalid credentials", "INVALID_CREDENTIALS")
      );
  }

  @GetMapping("/profile")
  public ApiResponse profile(HttpSession session) {
    return current(session)
      .map(u -> ApiResponse.ok("Profile loaded", profile(u)))
      .orElseGet(() -> ApiResponse.error("Login required", "UNAUTHORIZED"));
  }

  @PostMapping("/profile/message")
  public ApiResponse message(@RequestBody MessageRequest r, HttpSession s) {
    User u = require(s);
    users.updateMessage(u.id(), r.message());
    return ApiResponse.ok("Profile message saved");
  }

  // Intentionally no CSRF token check.
  @PostMapping("/profile/email")
  public ApiResponse email(@RequestBody EmailChangeRequest r, HttpSession s) {
    User u = require(s);
    users.updateEmail(u.id(), r.email());
    return ApiResponse.ok("Email changed without CSRF verification");
  }

  private java.util.Optional<User> current(HttpSession s) {
    Object id = s.getAttribute("unsafeUserId");
    return id instanceof Long n
      ? users.findById(n)
      : java.util.Optional.empty();
  }

  private User require(HttpSession s) {
    return current(s).orElseThrow(() ->
      new SecurityException("Login required")
    );
  }

  private Map<String, Object> profile(User u) {
    return Map.of(
      "displayName",
      u.displayName(),
      "email",
      u.email(),
      "message",
      u.profileMessage()
    );
  }
}
