package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.debt.*;
import com.fajars.expensetracker.user.User;
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
 * Use case implementation for creating a new debt.
 * Follows Clean Architecture principles - contains business logic only.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateDebtUseCase implements CreateDebt {

    private final DebtRepository debtRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public DebtResponse create(UUID userId, CreateDebtRequest request) {
        long startTime = System.currentTimeMillis();
        log.debug("Creating debt for user {}: {}", userId, request);

        // Build debt entity with business rules
        Debt debt = buildDebt(userId, request);

        // Persist
        debt = debtRepository.save(debt);

        // Log and metrics
        logBusinessEvent(debt);
        recordMetrics(startTime);

        log.info("Debt {} created successfully for user {}", debt.getId(), userId);
        return DebtResponse.from(debt);
    }

    private Debt buildDebt(UUID userId, CreateDebtRequest request) {
        LocalDateTime now = LocalDateTime.now();

        return Debt.builder()
            .id(UUID.randomUUID())
            .user(User.builder().id(userId).build())
            .type(request.type())
            .counterpartyName(request.counterpartyName())
            .totalAmount(request.totalAmount())
            .remainingAmount(request.totalAmount()) // Initially, remaining = total
            .dueDate(request.dueDate())
            .status(DebtStatus.OPEN)
            .note(request.note())
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private void logBusinessEvent(Debt debt) {
        String username = getCurrentUsername();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("debtId", debt.getId());
        attributes.put("counterparty", debt.getCounterpartyName());
        attributes.put("amount", debt.getTotalAmount());
        businessEventLogger.logBusinessEvent("DEBT_CREATED", username, attributes);
    }

    private void recordMetrics(long startTime) {
        metricsService.incrementCounter("debts.created.total");
        metricsService.recordTimer("debt.creation.duration", startTime);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
