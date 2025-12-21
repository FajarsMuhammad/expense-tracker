package com.fajars.expensetracker.subscription.usecase.checktrialeligibility;

import java.util.UUID;

/**
 * Use case for checking trial eligibility.
 */
public interface CheckTrialEligibility {

    /**
     * Check if user is eligible for trial subscription.
     * User is eligible if they have never had a premium subscription.
     *
     * @param userId user ID
     * @return true if eligible, false otherwise
     */
    boolean isEligible(UUID userId);
}
