package com.websecuritylab.dto;

public record ApiResponse(
  boolean success,
  String message,
  String code,
  Object data
) {
  public static ApiResponse ok(String message) {
    return new ApiResponse(true, message, null, null);
  }

  public static ApiResponse ok(String message, Object data) {
    return new ApiResponse(true, message, null, data);
  }

  public static ApiResponse error(String message, String code) {
    return new ApiResponse(false, message, code, null);
  }
}
