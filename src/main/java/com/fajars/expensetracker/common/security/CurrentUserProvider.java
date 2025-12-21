package com.fajars.expensetracker.common.security;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getUserId();
    String getEmail(); // optional
}
