package com.fajars.expensetracker.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating user profile.
 *
 * <p>Used by PUT /api/v1/me endpoint.
 * Users can update:
 * <ul>
 *   <li>Name - display name</li>
 *   <li>Locale - preferred language/locale (e.g., "id_ID", "en_US")</li>
 * </ul>
 *
 * <p>Email cannot be changed via this endpoint (security concern).
 *
 * @since Milestone 6
 */
public record UpdateProfileRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,

    @Pattern(
        regexp = "^[a-z]{2}_[A-Z]{2}$",
        message = "Locale must be in format: language_COUNTRY (e.g., id_ID, en_US)"
    )
    String locale  // Optional - can be null
) {}
