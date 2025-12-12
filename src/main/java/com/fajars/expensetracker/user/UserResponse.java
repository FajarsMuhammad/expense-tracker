package com.fajars.expensetracker.user;

import java.util.UUID;

public record UserResponse(UUID id, String email, String name) {}
