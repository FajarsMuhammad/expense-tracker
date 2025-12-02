package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.debt.Debt;
import com.fajars.expensetracker.debt.DebtRepository;
import com.fajars.expensetracker.debt.DebtResponse;
import com.fajars.expensetracker.debt.DebtStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Use case implementation for marking a debt as fully paid.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarkDebtAsPaidUseCase implements MarkDebtAsPaid {

    private final DebtRepository debtRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public DebtResponse markAsPaid(UUID userId, UUID debtId) {
        long startTime = System.currentTimeMillis();
        log.debug("Marking debt {} as paid for user {}", debtId, userId);

        // Validate and get debt (ownership check)
        Debt debt = debtRepository.findByIdAndUserId(debtId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Debt", debtId.toString()));

        // Check if already paid
        if (debt.getStatus() == DebtStatus.PAID) {
            log.info("Debt {} is already marked as paid", debtId);
            return DebtResponse.from(debt);
        }

        // Apply business rule
        debt.markAsPaid();

        // Persist
        debt = debtRepository.save(debt);

        // Log and metrics
        logBusinessEvent(debt);
        recordMetrics(startTime);

        log.info("Debt {} marked as paid for user {}", debtId, userId);
        return DebtResponse.from(debt);
    }

    private void logBusinessEvent(Debt debt) {
        String username = getCurrentUsername();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("debtId", debt.getId());
        attributes.put("counterparty", debt.getCounterpartyName());
        businessEventLogger.logBusinessEvent("DEBT_MARKED_PAID", username, attributes);
    }

    private void recordMetrics(long startTime) {
        metricsService.incrementCounter("debts.marked.paid.total");
        metricsService.recordTimer("debt.mark.paid.duration", startTime);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
