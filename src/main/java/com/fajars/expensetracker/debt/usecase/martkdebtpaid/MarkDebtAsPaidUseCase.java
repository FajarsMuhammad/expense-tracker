package com.fajars.expensetracker.debt.usecase.martkdebtpaid;

import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.debt.api.DebtResponse;
import com.fajars.expensetracker.debt.domain.Debt;
import com.fajars.expensetracker.debt.domain.DebtRepository;
import com.fajars.expensetracker.debt.domain.DebtStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final CurrentUserProvider userProvider;

    @Override
    @Transactional
    public DebtResponse markAsPaid(UUID debtId) {
        long startTime = System.currentTimeMillis();
        UUID userId = userProvider.getUserId();

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
        String username = userProvider.getEmail();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("debtId", debt.getId());
        attributes.put("counterparty", debt.getCounterpartyName());
        businessEventLogger.logBusinessEvent("DEBT_MARKED_PAID", username, attributes);
    }

    private void recordMetrics(long startTime) {
        metricsService.incrementCounter("debts.marked.paid.total");
        metricsService.recordTimer("debt.mark.paid.duration", startTime);
    }
}
