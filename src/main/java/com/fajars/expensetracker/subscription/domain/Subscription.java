package com.fajars.expensetracker.subscription.domain;

import com.fajars.expensetracker.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing a user subscription.
 * Tracks subscription lifecycle and premium access.
 */
@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_subscription_user_status", columnList = "user_id, status"),
    @Index(name = "idx_subscription_ended", columnList = "ended_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    /**
     * Payment provider (MIDTRANS, MANUAL).
     * NULL for FREE tier subscriptions.
     */
    @Column(length = 50)
    private String provider;

    /**
     * Provider's subscription ID (for reference).
     * NULL for FREE tier subscriptions.
     */
    @Column(name = "provider_subscription_id")
    private String providerSubscriptionId;

    /**
     * Subscription plan/tier (FREE, PREMIUM).
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private SubscriptionTier plan;

    /**
     * Subscription status (TRIAL, ACTIVE, EXPIRED, CANCELLED).
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    /**
     * Subscription start date.
     */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    /**
     * Subscription end date (NULL for FREE tier or unlimited subscriptions).
     */
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    /**
     * Business rule: Check if subscription is active.
     *
     * @return true if subscription is active or in trial
     */
    public boolean isActive() {
        if (status != SubscriptionStatus.ACTIVE && status != SubscriptionStatus.TRIAL) {
            return false;
        }

        if (endedAt == null) {
            return true; // No expiry (FREE tier or unlimited)
        }

        return LocalDateTime.now().isBefore(endedAt);
    }

    /**
     * Business rule: Check if subscription is premium.
     *
     * @return true if plan is PREMIUM and subscription is active
     */
    public boolean isPremium() {
        return plan == SubscriptionTier.PREMIUM && isActive();
    }

    /**
     * Business rule: Check if subscription is in trial period.
     *
     * @return true if status is TRIAL and not expired
     */
    public boolean isTrial() {
        return status == SubscriptionStatus.TRIAL && isActive();
    }

    /**
     * Business rule: Extend subscription by adding days to end date.
     *
     * @param days number of days to extend
     */
    public void extendBy(int days) {
        if (endedAt == null) {
            endedAt = LocalDateTime.now().plusDays(days);
        } else {
            endedAt = endedAt.plusDays(days);
        }
    }

    /**
     * Business rule: Mark subscription as expired.
     */
    public void markAsExpired() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    /**
     * Business rule: Cancel subscription.
     */
    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        if (this.endedAt == null || this.endedAt.isAfter(LocalDateTime.now())) {
            this.endedAt = LocalDateTime.now();
        }
    }

    /**
     * PrePersist callback to generate ID.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
}
