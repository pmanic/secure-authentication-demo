package com.websecuritylab.model;

public record User(
  long id,
  String displayName,
  String email,
  String passwordHash,
  String passwordSalt,
  int passwordIterations,
  String profileMessage,
  int failedLoginAttempts
) {}
