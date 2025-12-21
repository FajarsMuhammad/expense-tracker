package com.fajars.expensetracker.payment.domain;

import com.fajars.expensetracker.common.converter.JsonbConverter;
import com.fajars.expensetracker.subscription.domain.Subscription;
import com.fajars.expensetracker.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Domain entity representing a payment transaction.
 * Tracks all payment-related information from payment gateway (Midtrans).
 * Enforces business rules for payment lifecycle and state transitions.
 */
@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_payment_user", columnList = "user_id"),
    @Index(name = "idx_payment_order", columnList = "order_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    /**
     * Unique order ID sent to payment gateway.
     * Format: ORDER-{userId}-{timestamp}
     */
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    private String orderId;

    /**
     * Transaction ID received from payment gateway.
     * Populated after payment webhook is received.
     */
    @Size(max = 255)
    @Column(name = "transaction_id")
    private String transactionId;

    /**
     * Payment amount in decimal format for precision.
     */
    @NotNull
    @Positive
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Currency code (default: IDR).
     */
    @NotBlank
    @Size(min = 3, max = 3)
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "IDR";

    /**
     * Payment method used (e.g., CREDIT_CARD, BANK_TRANSFER, EWALLET).
     * Populated from webhook notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod;

    /**
     * Current payment status.
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    /**
     * Payment gateway provider.
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentProvider provider = PaymentProvider.MIDTRANS;

    /**
     * Midtrans Snap token for payment page.
     */
    @Column(name = "snap_token", columnDefinition = "TEXT")
    private String snapToken;

    /**
     * Midtrans Snap redirect URL.
     */
    @Column(name = "snap_redirect_url", columnDefinition = "TEXT")
    private String snapRedirectUrl;

    /**
     * Full webhook payload from payment gateway for audit trail.
     * Stored as JSONB for queryability.
     */
    @Convert(converter = JsonbConverter.class)
    @Column(name = "webhook_payload", columnDefinition = "jsonb")
    private Map<String, Object> webhookPayload;

    /**
     * Client-provided idempotency key to prevent duplicate payments.
     */
    @Size(max = 255)
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    /**
     * Additional metadata (device info, IP, campaign tracking, etc.).
     * Stored as JSONB for flexibility.
     */
    @Convert(converter = JsonbConverter.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Timestamp when payment was successfully completed.
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * Timestamp when payment link expired.
     */
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Business rule: Mark payment as successful.
     * Updates status and sets paid timestamp.
     *
     * @param transactionId the transaction ID from payment gateway
     * @param paymentMethod the payment method used
     */
    public void markAsSuccess(String transactionId, PaymentMethod paymentMethod) {
        validateStatusTransition(PaymentStatus.SUCCESS);
        this.status = PaymentStatus.SUCCESS;
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business rule: Mark payment as failed.
     */
    public void markAsFailed() {
        validateStatusTransition(PaymentStatus.FAILED);
        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business rule: Mark payment as expired.
     */
    public void markAsExpired() {
        validateStatusTransition(PaymentStatus.EXPIRED);
        this.status = PaymentStatus.EXPIRED;
        this.expiredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business rule: Mark payment as cancelled.
     */
    public void markAsCancelled() {
        validateStatusTransition(PaymentStatus.CANCELLED);
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business rule: Validate status transitions.
     * Prevents invalid state changes (e.g., SUCCESS -> PENDING).
     *
     * @param newStatus the target status
     * @throws IllegalStateException if transition is invalid
     */
    private void validateStatusTransition(PaymentStatus newStatus) {
        if (this.status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                "Cannot change status from SUCCESS to " + newStatus
            );
        }

        if (this.status == PaymentStatus.EXPIRED && newStatus != PaymentStatus.CANCELLED) {
            throw new IllegalStateException(
                "Expired payment can only be cancelled, not " + newStatus
            );
        }
    }

    /**
     * Check if payment is in a final state (cannot be changed).
     */
    public boolean isFinalState() {
        return status == PaymentStatus.SUCCESS
            || status == PaymentStatus.FAILED
            || status == PaymentStatus.EXPIRED
            || status == PaymentStatus.CANCELLED;
    }

    /**
     * Check if payment is successful and can activate subscription.
     */
    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS && paidAt != null;
    }

    /**
     * PrePersist callback to set timestamps, generate ID, and validate.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        // Validate on create
        validateInvariants();
    }

    /**
     * PreUpdate callback to update timestamp and validate.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // Validate on update
        validateInvariants();
    }

    /**
     * Business rule: Validate invariants.
     */
    private void validateInvariants() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Payment amount must be positive");
        }

        if (status == PaymentStatus.SUCCESS && paidAt == null) {
            throw new IllegalStateException("Successful payment must have paidAt timestamp");
        }

        if (status == PaymentStatus.EXPIRED && expiredAt == null) {
            throw new IllegalStateException("Expired payment must have expiredAt timestamp");
        }
    }
}
