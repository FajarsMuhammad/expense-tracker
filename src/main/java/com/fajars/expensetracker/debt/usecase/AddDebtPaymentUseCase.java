package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.debt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Use case implementation for adding a payment to a debt.
 * Ensures transactional integrity and business rule enforcement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddDebtPaymentUseCase implements AddDebtPayment {

    private final DebtRepository debtRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public AddDebtPaymentResult addPayment(UUID userId, UUID debtId, AddDebtPaymentRequest request) {
        long startTime = System.currentTimeMillis();
        log.debug("Adding payment to debt {} for user {}: {}", debtId, userId, request);

        // Validate payment amount (use case responsibility)
        validatePaymentAmount(request.amount());

        // Validate and get debt (ownership check)
        Debt debt = debtRepository.findByIdAndUserId(debtId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Debt", debtId.toString()));

        // Validate debt is not already paid
        if (debt.getStatus() == DebtStatus.PAID) {
            throw new IllegalStateException("Cannot add payment to a debt that is already fully paid");
        }

        // Apply payment (business rule in entity)
        debt.applyPayment(request.amount());

        // Create payment record
        DebtPayment payment = buildPayment(debt, request);

        // Persist both (transactional)
        payment = debtPaymentRepository.save(payment);
        debt = debtRepository.save(debt);

        // Log and metrics
        logBusinessEvent(debt, payment);
        recordMetrics(startTime);

        log.info("Payment {} added to debt {} for user {}", payment.getId(), debtId, userId);

        return new AddDebtPaymentResult(
            DebtPaymentResponse.from(payment),
            DebtResponse.from(debt)
        );
    }

    private DebtPayment buildPayment(Debt debt, AddDebtPaymentRequest request) {
        LocalDateTime paidAt = request.paidAt() != null ? request.paidAt() : LocalDateTime.now();

        return DebtPayment.builder()
            .id(UUID.randomUUID())
            .debt(debt)
            .amount(request.amount())
            .paidAt(paidAt)
            .note(request.note())
            .build();
    }

    private void logBusinessEvent(Debt debt, DebtPayment payment) {
        String username = getCurrentUsername();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("debtId", debt.getId());
        attributes.put("paymentId", payment.getId());
        attributes.put("amount", payment.getAmount());
        attributes.put("remainingAmount", debt.getRemainingAmount());
        businessEventLogger.logBusinessEvent("DEBT_PAYMENT_ADDED", username, attributes);
    }

    private void recordMetrics(long startTime) {
        metricsService.incrementCounter("debt.payments.added.total");
        metricsService.recordTimer("debt.payment.processing.duration", startTime);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    /**
     * Validate that payment amount is positive.
     * This is a use case-level validation.
     *
     * @param amount the payment amount to validate
     * @throws IllegalArgumentException if amount is null or not positive
     */
    private void validatePaymentAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }
}
