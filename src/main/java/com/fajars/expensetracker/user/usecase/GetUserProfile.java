package com.fajars.expensetracker.user.usecase;

import com.fajars.expensetracker.user.api.ProfileResponse;

/**
 * Use case for getting complete user profile with subscription information.
 *
 * @since Milestone 6
 */
public interface GetUserProfile {

    /**
     * Get complete user profile including subscription details.
     *
     * @return complete profile response
     * @throws com.fajars.expensetracker.common.exception.BusinessException if user not found
     */
    ProfileResponse getProfile();
}
