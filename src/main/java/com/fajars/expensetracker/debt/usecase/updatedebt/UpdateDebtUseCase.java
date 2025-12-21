package com.fajars.expensetracker.debt.usecase.updatedebt;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.debt.api.DebtResponse;
import com.fajars.expensetracker.debt.api.UpdateDebtRequest;
import com.fajars.expensetracker.debt.domain.Debt;
import com.fajars.expensetracker.debt.domain.DebtRepository;
import com.fajars.expensetracker.debt.domain.DebtStatus;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case implementation for updating an existing debt.
 * Follows Clean Architecture principles - contains business logic only.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateDebtUseCase implements UpdateDebt {

    private final DebtRepository debtRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider userProvider;

    @Override
    @Transactional
    public DebtResponse update(UUID debtId, UpdateDebtRequest request) {
        long startTime = System.currentTimeMillis();
        UUID userId = userProvider.getUserId();

        log.debug("Updating debt {} for user {}: {}", debtId, userId, request);

        Debt debt = debtRepository.findByIdAndUserId(debtId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Debt not found"));

        updateDebtFields(debt, request);

        debt = debtRepository.save(debt);

        logBusinessEvent(debt);
        recordMetrics(startTime);

        log.info("Debt {} updated successfully for user {}", debtId, userId);
        return DebtResponse.from(debt);
    }

    private void updateDebtFields(Debt debt, UpdateDebtRequest request) {
        debt.setType(request.type());
        debt.setCounterpartyName(request.counterpartyName());
        debt.setDueDate(request.dueDate());
        debt.setNote(request.note());
        debt.setUpdatedAt(LocalDateTime.now());

        if (!debt.getTotalAmount().equals(request.totalAmount())) {
            double paidAmount = debt.getTotalAmount() - debt.getRemainingAmount();
            debt.setTotalAmount(request.totalAmount());

            double newRemainingAmount = request.totalAmount() - paidAmount;

            if (newRemainingAmount < 0) {
                throw new IllegalArgumentException(
                    "New total amount is less than the amount already paid. " +
                    "Cannot update total amount to be less than paid amount."
                );
            }

            debt.setRemainingAmount(newRemainingAmount);

            updateStatus(debt);
        }
    }

    private void updateStatus(Debt debt) {
        if (debt.getRemainingAmount() == 0) {
            debt.setStatus(DebtStatus.PAID);
        } else if (debt.getRemainingAmount() < debt.getTotalAmount()) {
            debt.setStatus(DebtStatus.PARTIAL);
        } else {
            debt.setStatus(DebtStatus.OPEN);
        }
    }

    private void logBusinessEvent(Debt debt) {
        String username = userProvider.getEmail();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("debtId", debt.getId());
        attributes.put("counterparty", debt.getCounterpartyName());
        attributes.put("totalAmount", debt.getTotalAmount());
        attributes.put("remainingAmount", debt.getRemainingAmount());
        businessEventLogger.logBusinessEvent("DEBT_UPDATED", username, attributes);
    }

    private void recordMetrics(long startTime) {
        metricsService.incrementCounter("debts.updated.total");
        metricsService.recordTimer("debt.update.duration", startTime);
    }
}
