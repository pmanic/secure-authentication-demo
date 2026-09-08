package com.websecuritylab.dto;

public record RegisterRequest(
  String displayName,
  String email,
  String password
) {}
