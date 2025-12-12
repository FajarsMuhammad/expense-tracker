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
 * Use case implementation for activating or extending subscription.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivateSubscriptionUseCase implements ActivateSubscription {

    private static final int MONTHLY_DAYS = 30;

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public Subscription activateOrExtend(UUID userId, UUID paymentId, int days) {
        log.info("Activating/extending subscription for user {} with {} days", userId, days);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("User not found"));

        Subscription subscription = subscriptionRepository
            .findActiveSubscriptionByUserId(userId)
            .orElse(null);

        if (subscription != null && subscription.isPremium()) {
            // Extend existing premium subscription
            subscription.extendBy(days);
            subscription = subscriptionRepository.save(subscription);

            logBusinessEvent("SUBSCRIPTION_EXTENDED", userId, subscription);
            metricsService.incrementCounter("subscription.extended");

            log.info("Subscription extended for user {} until {}", userId, subscription.getEndedAt());
        } else {
            // Cancel any existing subscription (FREE or expired)
            if (subscription != null) {
                subscription.cancel();
                subscriptionRepository.save(subscription);
            }

            // Create new premium subscription
            LocalDateTime now = LocalDateTime.now();
            subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .user(user)
                .plan(SubscriptionTier.PREMIUM)
                .status(SubscriptionStatus.ACTIVE)
                .provider("MIDTRANS")
                .providerSubscriptionId(paymentId.toString())
                .startedAt(now)
                .endedAt(now.plusDays(days))
                .build();

            subscription = subscriptionRepository.save(subscription);

            logBusinessEvent("SUBSCRIPTION_ACTIVATED", userId, subscription);
            metricsService.incrementCounter("subscription.activated");

            log.info("Premium subscription activated for user {} until {}",
                userId, subscription.getEndedAt());
        }

        return subscription;
    }

    private void logBusinessEvent(String eventType, UUID userId, Subscription subscription) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("subscriptionId", subscription.getId());
        attributes.put("plan", subscription.getPlan());
        attributes.put("status", subscription.getStatus());
        attributes.put("startedAt", subscription.getStartedAt());
        attributes.put("endedAt", subscription.getEndedAt());

        User user = userRepository.findById(userId).orElse(null);
        String username = user != null ? user.getEmail() : userId.toString();

        businessEventLogger.logBusinessEvent(eventType, username, attributes);
    }
}
