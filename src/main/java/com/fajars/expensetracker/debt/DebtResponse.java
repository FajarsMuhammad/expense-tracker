package com.fajars.expensetracker.debt;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for debt information.
 */
@Schema(description = "Debt information response")
public record DebtResponse(

    @Schema(description = "Unique identifier of the debt")
    UUID id,

    @Schema(description = "Name of the counterparty")
    String counterpartyName,

    @Schema(description = "Total amount of the debt")
    Double totalAmount,

    @Schema(description = "Remaining amount to be paid")
    Double remainingAmount,

    @Schema(description = "Amount already paid")
    Double paidAmount,

    @Schema(description = "Due date of the debt")
    LocalDateTime dueDate,

    @Schema(description = "Current status of the debt")
    DebtStatus status,

    @Schema(description = "Whether the debt is overdue")
    Boolean isOverdue,

    @Schema(description = "Number of payments made")
    Integer paymentCount,

    @Schema(description = "When the debt was created")
    LocalDateTime createdAt,

    @Schema(description = "When the debt was last updated")
    LocalDateTime updatedAt
) {

    /**
     * Convert a Debt entity to DebtResponse DTO.
     */
    public static DebtResponse from(Debt debt) {
        double paidAmount = debt.getTotalAmount() - debt.getRemainingAmount();
        int paymentCount = debt.getPayments() != null ? debt.getPayments().size() : 0;

        return new DebtResponse(
            debt.getId(),
            debt.getCounterpartyName(),
            debt.getTotalAmount(),
            debt.getRemainingAmount(),
            paidAmount,
            debt.getDueDate(),
            debt.getStatus(),
            debt.isOverdue(),
            paymentCount,
            debt.getCreatedAt(),
            debt.getUpdatedAt()
        );
    }
}
