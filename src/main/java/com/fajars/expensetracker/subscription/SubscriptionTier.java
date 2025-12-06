package com.fajars.expensetracker.subscription;

/**
 * Subscription tier enum.
 */
public enum SubscriptionTier {
    /**
     * Free tier with limited features.
     * - Max 90 days date range
     * - Max 100 records per export
     * - CSV export only
     */
    FREE,

    /**
     * Premium tier with full features.
     * - Max 365 days date range
     * - Max 10,000 records per export
     * - All export formats (CSV, Excel, PDF)
     * - Advanced analytics
     * - Priority support
     */
    PREMIUM
}
