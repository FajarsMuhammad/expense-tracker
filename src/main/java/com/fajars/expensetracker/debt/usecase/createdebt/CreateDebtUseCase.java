package com.fajars.expensetracker.debt.usecase.createdebt;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.debt.api.CreateDebtRequest;
import com.fajars.expensetracker.debt.api.DebtResponse;
import com.fajars.expensetracker.debt.domain.Debt;
import com.fajars.expensetracker.debt.domain.DebtRepository;
import com.fajars.expensetracker.debt.domain.DebtStatus;
import com.fajars.expensetracker.subscription.SubscriptionHelper;
import com.fajars.expensetracker.user.domain.User;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SubscriptionHelper subscriptionHelper;
    private final CurrentUserProvider userProvider;

    private static final int FREE_TIER_DEBT_LIMIT = 10;

    @Override
    @Transactional
    public DebtResponse create(CreateDebtRequest request) {
        long startTime = System.currentTimeMillis();
        UUID userId = userProvider.getUserId();

        log.debug("Creating debt for user {}: {}", userId, request);

        // Check debt limit for FREE tier users
        validateDebtLimit(userId);

        Debt debt = buildDebt(userId, request);

        debt = debtRepository.save(debt);

        logBusinessEvent(debt);
        recordMetrics(startTime);

        log.info("Debt {} created successfully for user {}", debt.getId(), userId);
        return DebtResponse.from(debt);
    }

    /**
     * Validate debt limit for FREE tier users.
     * PREMIUM users bypass this check.
     *
     * @param userId User ID
     * @throws BusinessException if FREE user has reached debt limit
     */
    private void validateDebtLimit(UUID userId) {
        if (subscriptionHelper.isPremiumUser(userId)) {
            log.debug("User {} is PREMIUM - bypassing debt limit check", userId);
            return;
        }

        Long activeDebtCount = debtRepository.countActiveDebtsByUserId(userId);

        if (activeDebtCount >= FREE_TIER_DEBT_LIMIT) {
            log.warn("User {} exceeded debt limit: {}/{}", userId, activeDebtCount, FREE_TIER_DEBT_LIMIT);
            throw BusinessException.forbidden(
                    "You have reached the maximum limit of " + FREE_TIER_DEBT_LIMIT + " active debts for FREE tier. " +
                    "Upgrade to PREMIUM for unlimited debt tracking."
            );
        }

        log.debug("User {} debt count: {}/{}", userId, activeDebtCount, FREE_TIER_DEBT_LIMIT);
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
        String username = userProvider.getEmail();
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
}
