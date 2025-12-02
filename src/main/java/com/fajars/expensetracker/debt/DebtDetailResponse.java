package com.fajars.expensetracker.debt;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Response DTO for detailed debt information including all payments.
 */
@Schema(description = "Detailed debt information with payment history")
public record DebtDetailResponse(

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

    @Schema(description = "List of all payments made")
    List<DebtPaymentResponse> payments,

    @Schema(description = "When the debt was created")
    LocalDateTime createdAt,

    @Schema(description = "When the debt was last updated")
    LocalDateTime updatedAt
) {

    /**
     * Convert a Debt entity to DebtDetailResponse DTO.
     */
    public static DebtDetailResponse from(Debt debt) {
        double paidAmount = debt.getTotalAmount() - debt.getRemainingAmount();

        List<DebtPaymentResponse> paymentResponses = debt.getPayments() != null
            ? debt.getPayments().stream()
                .map(DebtPaymentResponse::from)
                .collect(Collectors.toList())
            : List.of();

        return new DebtDetailResponse(
            debt.getId(),
            debt.getCounterpartyName(),
            debt.getTotalAmount(),
            debt.getRemainingAmount(),
            paidAmount,
            debt.getDueDate(),
            debt.getStatus(),
            debt.isOverdue(),
            paymentResponses,
            debt.getCreatedAt(),
            debt.getUpdatedAt()
        );
    }
}
