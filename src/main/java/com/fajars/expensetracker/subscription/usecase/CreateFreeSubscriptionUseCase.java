package com.fajars.expensetracker.subscription.usecase;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.subscription.Subscription;
import com.fajars.expensetracker.subscription.SubscriptionRepository;
import com.fajars.expensetracker.subscription.SubscriptionStatus;
import com.fajars.expensetracker.subscription.SubscriptionTier;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Use case implementation for creating free subscription.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateFreeSubscriptionUseCase implements CreateFreeSubscription {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public Subscription createFree(UUID userId) {
        log.info("Creating FREE subscription for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("User not found"));

        Subscription subscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .provider(null)
            .providerSubscriptionId(null)
            .startedAt(LocalDateTime.now())
            .endedAt(null) // No expiry for FREE tier
            .build();

        subscription = subscriptionRepository.save(subscription);

        logBusinessEvent(userId, subscription);
        metricsService.incrementCounter("subscription.created", "tier", "FREE");

        log.info("FREE subscription created for user: {}", userId);
        return subscription;
    }

    private void logBusinessEvent(UUID userId, Subscription subscription) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("subscriptionId", subscription.getId());
        attributes.put("plan", subscription.getPlan());
        attributes.put("status", subscription.getStatus());
        attributes.put("startedAt", subscription.getStartedAt());
        attributes.put("endedAt", subscription.getEndedAt());

        User user = userRepository.findById(userId).orElse(null);
        String username = user != null ? user.getEmail() : userId.toString();

        businessEventLogger.logBusinessEvent("SUBSCRIPTION_CREATED", username, attributes);
    }
}
