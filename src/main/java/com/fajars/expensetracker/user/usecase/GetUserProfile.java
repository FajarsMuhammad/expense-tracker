package com.fajars.expensetracker.user.usecase;

import com.fajars.expensetracker.user.ProfileResponse;
import java.util.UUID;

/**
 * Use case for getting complete user profile with subscription information.
 *
 * @since Milestone 6
 */
public interface GetUserProfile {

    /**
     * Get complete user profile including subscription details.
     *
     * @param userId the user ID
     * @return complete profile response
     * @throws com.fajars.expensetracker.common.exception.BusinessException if user not found
     */
    ProfileResponse getProfile(UUID userId);
}
