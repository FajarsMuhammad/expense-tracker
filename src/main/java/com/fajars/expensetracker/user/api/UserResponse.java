package com.fajars.expensetracker.user.api;

import java.util.UUID;

public record UserResponse(UUID id, String email, String name) {}
