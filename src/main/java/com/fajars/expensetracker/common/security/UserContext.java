package com.fajars.expensetracker.common.security;

import com.fajars.expensetracker.user.UserDto;
import com.fajars.expensetracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility component for accessing the current authenticated user context.
 *
 * This eliminates boilerplate code in controllers by centralizing
 * user authentication lookup logic.
 */
@Component
@RequiredArgsConstructor
public class UserContext {

    private final UserService userService;

    /**
     * Get the current authenticated user's ID.
     *
     * @return UUID of the current user
     * @throws IllegalStateException if user is not authenticated
     */
    public UUID getCurrentUserId() {
        UserDto user = getCurrentUser();
        return user.id();
    }

    /**
     * Get the current authenticated user's full information.
     *
     * @return UserDto of the current user
     * @throws IllegalStateException if user is not authenticated
     */
    public UserDto getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        String email = auth.getName();
        return userService.getByEmail(email);
    }

    /**
     * Check if a user is currently authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }
}
