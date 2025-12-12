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
 * Use case implementation for creating trial subscription.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTrialSubscriptionUseCase implements CreateTrialSubscription {

    private static final int TRIAL_DAYS = 14;

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CheckTrialEligibility checkTrialEligibility;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public Subscription createTrial(UUID userId) {
        log.info("Creating TRIAL subscription for user: {}", userId);

        // Check eligibility
        if (!checkTrialEligibility.isEligible(userId)) {
            throw BusinessException.forbidden("User is not eligible for trial");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("User not found"));

        // Cancel any existing FREE subscription
        subscriptionRepository.findActiveSubscriptionByUserId(userId)
            .ifPresent(sub -> {
                sub.cancel();
                subscriptionRepository.save(sub);
            });

        // Create trial subscription
        LocalDateTime now = LocalDateTime.now();
        Subscription subscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.TRIAL)
            .provider(null)
            .providerSubscriptionId(null)
            .startedAt(now)
            .endedAt(now.plusDays(TRIAL_DAYS))
            .build();

        subscription = subscriptionRepository.save(subscription);

        logBusinessEvent(userId, subscription);
        metricsService.incrementCounter("subscription.trial_started");

        log.info("TRIAL subscription created for user {} (expires: {})",
                 userId, subscription.getEndedAt());
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

        businessEventLogger.logBusinessEvent("TRIAL_STARTED", username, attributes);
    }
}
