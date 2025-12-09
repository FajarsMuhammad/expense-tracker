package com.fajars.expensetracker.common.validation;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Validator for date range operations.
 *
 * Enforces business rules:
 * - Free tier: max 90 days range
 * - Premium tier: max 365 days range
 * - Start date must be before end date
 * - Dates cannot be in the future
 */
@Component
public class DateRangeValidator {

    private static final long FREE_TIER_MAX_DAYS = 90;
    private static final long PREMIUM_TIER_MAX_DAYS = 365;

    /**
     * Validate date range for free tier users.
     *
     * @param startDate start date
     * @param endDate end date
     * @throws IllegalArgumentException if validation fails
     * @deprecated Since Milestone 6. Reports are now premium-only, free tier validation no longer needed.
     *             Only {@link #validatePremiumTier(LocalDateTime, LocalDateTime)} is used.
     *             This method is kept for backward compatibility but will be removed in future versions.
     */
    @Deprecated(since = "Milestone 6", forRemoval = true)
    public void validateFreeTier(LocalDateTime startDate, LocalDateTime endDate) {
        validate(startDate, endDate, FREE_TIER_MAX_DAYS);
    }

    /**
     * Validate date range for premium tier users.
     *
     * @param startDate start date
     * @param endDate end date
     * @throws IllegalArgumentException if validation fails
     */
    public void validatePremiumTier(LocalDateTime startDate, LocalDateTime endDate) {
        validate(startDate, endDate, PREMIUM_TIER_MAX_DAYS);
    }

    /**
     * Validate date range with custom max days limit.
     *
     * @param startDate start date
     * @param endDate end date
     * @param maxDays maximum allowed days between dates
     * @throws IllegalArgumentException if validation fails
     */
    private void validate(LocalDateTime startDate, LocalDateTime endDate, long maxDays) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        if (endDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("End date cannot be in the future");
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > maxDays) {
            throw new IllegalArgumentException(
                String.format("Date range cannot exceed %d days. Current range: %d days",
                    maxDays, daysBetween)
            );
        }
    }

    /**
     * Check if date range is valid (without throwing exception).
     *
     * @param startDate start date
     * @param endDate end date
     * @param isPremium whether user is premium
     * @return true if valid, false otherwise
     */
    public boolean isValid(LocalDateTime startDate, LocalDateTime endDate, boolean isPremium) {
        try {
            if (isPremium) {
                validatePremiumTier(startDate, endDate);
            } else {
                validateFreeTier(startDate, endDate);
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get maximum days allowed for user tier.
     *
     * @param isPremium whether user is premium
     * @return max days allowed
     */
    public long getMaxDaysAllowed(boolean isPremium) {
        return isPremium ? PREMIUM_TIER_MAX_DAYS : FREE_TIER_MAX_DAYS;
    }
}
