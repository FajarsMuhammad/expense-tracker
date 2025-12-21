package com.fajars.expensetracker.subscription.usecase.createtrialsubscription;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.subscription.domain.Subscription;
import com.fajars.expensetracker.subscription.domain.SubscriptionRepository;
import com.fajars.expensetracker.subscription.domain.SubscriptionStatus;
import com.fajars.expensetracker.subscription.domain.SubscriptionTier;
import com.fajars.expensetracker.subscription.usecase.checktrialeligibility.CheckTrialEligibility;
import com.fajars.expensetracker.user.domain.User;
import com.fajars.expensetracker.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.subscription.trial-days:14}")
    private int trialDays;

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CheckTrialEligibility checkTrialEligibility;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public Subscription createTrial() {
        UUID userId = currentUserProvider.getUserId();
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
            .endedAt(now.plusDays(trialDays))
            .build();

        subscription = subscriptionRepository.save(subscription);

        logBusinessEvent(user, subscription, "TRIAL_STARTED");
        metricsService.incrementCounter("subscription.trial_started");

        log.info("TRIAL subscription created for user {} (expires: {})",
                 userId, subscription.getEndedAt());
        return subscription;
    }

    @Override
    @Transactional
    public Subscription createTrialForNewUser() {
        UUID userId = currentUserProvider.getUserId();
        log.info("Creating TRIAL subscription for new user registration: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("User not found"));

        // Skip eligibility check - new users automatically get trial
        // No need to cancel existing subscription - this is a new user

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
            .endedAt(now.plusDays(trialDays))
            .build();

        subscription = subscriptionRepository.save(subscription);

        logBusinessEvent(user, subscription, "USER_REGISTERED_WITH_TRIAL");
        metricsService.incrementCounter("subscription.trial_started");
        metricsService.incrementCounter("user.registered.trial");

        log.info("TRIAL subscription created for new user {} (expires: {})",
                 userId, subscription.getEndedAt());
        return subscription;
    }

    private void logBusinessEvent(User user, Subscription subscription, String eventName) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("subscriptionId", subscription.getId());
        attributes.put("plan", subscription.getPlan());
        attributes.put("status", subscription.getStatus());
        attributes.put("startedAt", subscription.getStartedAt());
        attributes.put("endedAt", subscription.getEndedAt());

        String username = user.getEmail();
        businessEventLogger.logBusinessEvent(eventName, username, attributes);
    }
}
