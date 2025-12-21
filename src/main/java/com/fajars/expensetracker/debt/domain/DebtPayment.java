package com.fajars.expensetracker.debt.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Domain entity representing a payment made towards a debt.
 */
@Entity
@Table(name = "debt_payments", indexes = {
    @Index(name = "idx_payment_debt_id", columnList = "debt_id"),
    @Index(name = "idx_payment_paid_at", columnList = "paid_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebtPayment {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debt_id", nullable = false)
    @NotNull
    private Debt debt;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Double amount;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime paidAt;

    @Size(max = 500)
    @Column(length = 500)
    private String note;

    /**
     * Business rule: Validate payment amount is positive.
     */
    @PrePersist
    @PreUpdate
    protected void validatePayment() {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }
}
