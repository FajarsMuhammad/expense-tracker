package com.fajars.expensetracker.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String token) {}
