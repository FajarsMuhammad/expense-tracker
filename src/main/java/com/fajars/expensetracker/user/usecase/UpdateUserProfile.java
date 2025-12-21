package com.fajars.expensetracker.user.usecase;

import com.fajars.expensetracker.user.api.ProfileResponse;
import com.fajars.expensetracker.user.api.UpdateProfileRequest;

/**
 * Use case for updating user profile.
 *
 * @since Milestone 6
 */
public interface UpdateUserProfile {

    /**
     * Update user profile (name, locale).
     *
     * @param request the update request
     * @return updated profile response
     * @throws com.fajars.expensetracker.common.exception.BusinessException if user not found
     */
    ProfileResponse updateProfile(UpdateProfileRequest request);
}
