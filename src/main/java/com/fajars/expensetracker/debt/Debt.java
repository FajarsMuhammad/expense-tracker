package com.fajars.expensetracker.debt;

import com.fajars.expensetracker.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Domain entity representing a debt record.
 * Enforces business rules for debt tracking and payment management.
 */
@Entity
@Table(name = "debts", indexes = {
    @Index(name = "idx_debt_user_id", columnList = "user_id"),
    @Index(name = "idx_debt_status", columnList = "status"),
    @Index(name = "idx_debt_due_date", columnList = "due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Debt {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String counterpartyName;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Double totalAmount;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Double remainingAmount;

    @Column(nullable = true)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private DebtStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "debt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DebtPayment> payments = new ArrayList<>();

    /**
     * Business rule: Apply a payment to this debt.
     * Updates remainingAmount and status based on payment.
     * Note: Amount validation should be done at use case layer.
     *
     * @param paymentAmount the amount being paid (must be positive and validated)
     * @throws IllegalArgumentException if payment amount exceeds remaining debt
     */
    public void applyPayment(Double paymentAmount) {
        double newRemainingAmount = this.remainingAmount - paymentAmount;

        if (newRemainingAmount < 0) {
            throw new IllegalArgumentException("Payment amount exceeds remaining debt");
        }

        this.remainingAmount = newRemainingAmount;
        updateStatus();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business rule: Mark debt as fully paid.
     */
    public void markAsPaid() {
        this.remainingAmount = 0.0;
        this.status = DebtStatus.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business rule: Update status based on remaining amount.
     */
    private void updateStatus() {
        if (this.remainingAmount == 0) {
            this.status = DebtStatus.PAID;
        } else if (this.remainingAmount < this.totalAmount) {
            this.status = DebtStatus.PARTIAL;
        } else {
            this.status = DebtStatus.OPEN;
        }
    }

    /**
     * Business rule: Ensure remaining amount is never negative.
     */
    @PrePersist
    @PreUpdate
    protected void validateInvariants() {
        if (remainingAmount < 0) {
            throw new IllegalStateException("Remaining amount cannot be negative");
        }
        if (remainingAmount > totalAmount) {
            throw new IllegalStateException("Remaining amount cannot exceed total amount");
        }
    }

    /**
     * Check if the debt is overdue.
     */
    public boolean isOverdue() {
        return dueDate != null
            && status != DebtStatus.PAID
            && LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Helper method to add a payment to the collection.
     */
    public void addPayment(DebtPayment payment) {
        payments.add(payment);
        payment.setDebt(this);
    }
}
