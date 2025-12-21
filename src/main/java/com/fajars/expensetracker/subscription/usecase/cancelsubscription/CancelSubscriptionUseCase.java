package com.fajars.expensetracker.subscription.usecase.cancelsubscription;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.subscription.domain.Subscription;
import com.fajars.expensetracker.subscription.domain.SubscriptionRepository;
import com.fajars.expensetracker.subscription.domain.SubscriptionTier;
import com.fajars.expensetracker.user.domain.User;
import com.fajars.expensetracker.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Use case implementation for cancelling subscription.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancelSubscriptionUseCase implements CancelSubscription {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider userProvider;

    @Override
    @Transactional
    public void cancel() {
        UUID userId = userProvider.getUserId();

        log.info("Cancelling subscription for user: {}", userId);

        Subscription subscription = subscriptionRepository
            .findActiveSubscriptionByUserId(userId)
            .orElseThrow(() -> BusinessException.notFound("No active subscription found"));

        if (subscription.getPlan() == SubscriptionTier.FREE) {
            throw BusinessException.badRequest("Cannot cancel FREE tier subscription");
        }

        subscription.cancel();
        subscriptionRepository.save(subscription);

        logBusinessEvent(userId, subscription);
        metricsService.incrementCounter("subscription.cancelled");

        log.info("Subscription cancelled for user: {}", userId);
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

        businessEventLogger.logBusinessEvent("SUBSCRIPTION_CANCELLED", username, attributes);
    }
}
