package com.fajars.expensetracker.auth;

import java.util.UUID;

public record AuthResponse(String token, UUID userId, String email, String name) {}
