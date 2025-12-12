package com.fajars.expensetracker.user.usecase;

import com.fajars.expensetracker.user.ProfileResponse;
import com.fajars.expensetracker.user.UpdateProfileRequest;
import java.util.UUID;

/**
 * Use case for updating user profile.
 *
 * @since Milestone 6
 */
public interface UpdateUserProfile {

    /**
     * Update user profile (name, locale).
     *
     * @param userId the user ID
     * @param request the update request
     * @return updated profile response
     * @throws com.fajars.expensetracker.common.exception.BusinessException if user not found
     */
    ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);
}
