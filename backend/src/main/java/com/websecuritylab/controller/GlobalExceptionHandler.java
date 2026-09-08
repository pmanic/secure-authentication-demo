package com.websecuritylab.controller;

import com.websecuritylab.controller.secure.SecureController.InvalidCsrfException;
import com.websecuritylab.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidCsrfException.class)
  ResponseEntity<ApiResponse> csrf() {
    return ResponseEntity.status(403).body(
      ApiResponse.error(
        "CSRF token is missing or invalid",
        "INVALID_CSRF_TOKEN"
      )
    );
  }

  @ExceptionHandler(SecurityException.class)
  ResponseEntity<ApiResponse> unauthorized() {
    return ResponseEntity.status(401).body(
      ApiResponse.error("Login required", "UNAUTHORIZED")
    );
  }

  @ExceptionHandler(
    { IllegalArgumentException.class, IllegalStateException.class }
  )
  ResponseEntity<ApiResponse> bad(RuntimeException e) {
    return ResponseEntity.badRequest().body(
      ApiResponse.error(e.getMessage(), "INVALID_REQUEST")
    );
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiResponse> conflict() {
    return ResponseEntity.status(409).body(
      ApiResponse.error("That email is already registered", "EMAIL_EXISTS")
    );
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse> other() {
    return ResponseEntity.status(500).body(
      ApiResponse.error("The request could not be completed", "INTERNAL_ERROR")
    );
  }
}
