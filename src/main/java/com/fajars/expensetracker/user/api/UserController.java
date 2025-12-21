package com.fajars.expensetracker.user.api;

import com.fajars.expensetracker.user.usecase.GetUserProfile;
import com.fajars.expensetracker.user.usecase.UpdateUserProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user profile management.
 *
 * <p>Provides endpoints for:
 * <ul>
 *   <li>GET /me - Get complete user profile with subscription info</li>
 *   <li>PUT /me - Update user profile (name, locale)</li>
 * </ul>
 *
 * <p>All endpoints require authentication.
 *
 * @since Milestone 6
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile", description = "User profile management endpoints")
public class UserController {

    private final GetUserProfile getUserProfile;
    private final UpdateUserProfile updateUserProfile;

    /**
     * Get current user's complete profile including subscription details.
     *
     * <p>Returns comprehensive profile information:
     * <ul>
     *   <li>User info: id, email, name, locale</li>
     *   <li>Account metadata: createdAt, updatedAt</li>
     *   <li>Subscription details: tier, status, trial info</li>
     * </ul>
     *
     * @param authenticatedUser authenticated user identity
     * @return complete profile response
     */
    @GetMapping("/me")
    @Operation(
        summary = "Get my profile",
        description = "Get complete user profile including subscription information. " +
                      "Returns user details, preferences, and current subscription status with trial countdown.",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<ProfileResponse> getMyProfile() {
        log.debug("GET /api/v1/users/me called");

        ProfileResponse profile = getUserProfile.getProfile();

        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user's profile.
     *
     * <p>Allows updating:
     * <ul>
     *   <li>Name - display name</li>
     *   <li>Locale - preferred language/locale (e.g., "id_ID", "en_US")</li>
     * </ul>
     *
     * <p>Email cannot be changed via this endpoint for security reasons.
     *
     * @param request update profile request
     * @return updated profile response
     */
    @PutMapping("/me")
    @Operation(
        summary = "Update my profile",
        description = "Update user profile (name, locale). Email cannot be changed via this endpoint. " +
                      "Returns updated profile with current subscription information.",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<ProfileResponse> updateMyProfile(
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        log.info("PUT /api/v1/users/me called");

        ProfileResponse updatedProfile = updateUserProfile.updateProfile(request);

        log.info("Profile updated successfully for user: {}", updatedProfile.id());
        return ResponseEntity.ok(updatedProfile);
    }
}
