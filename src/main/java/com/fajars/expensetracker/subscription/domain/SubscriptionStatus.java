package com.fajars.expensetracker.subscription.domain;

/**
 * Subscription status enumeration.
 * Represents the lifecycle states of a user subscription.
 */
public enum SubscriptionStatus {
    /**
     * User is in trial period (14 days free).
     * Has access to all premium features.
     */
    TRIAL,

    /**
     * Active paid subscription or free tier.
     * Has access according to subscription tier.
     */
    ACTIVE,

    /**
     * Subscription has expired.
     * User downgraded to FREE tier with limited features.
     */
    EXPIRED,

    /**
     * Subscription was cancelled by user or admin.
     * User has FREE tier access.
     */
    CANCELLED
}
