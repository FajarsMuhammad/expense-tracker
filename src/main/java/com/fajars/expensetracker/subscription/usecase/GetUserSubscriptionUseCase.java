package com.fajars.expensetracker.subscription.usecase;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.subscription.Subscription;
import com.fajars.expensetracker.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case implementation for retrieving user's subscription.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetUserSubscriptionUseCase implements GetUserSubscription {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public Subscription getSubscription(UUID userId) {
        log.debug("Getting subscription for user: {}", userId);

        return subscriptionRepository.findActiveSubscriptionByUserId(userId)
            .orElseThrow(() -> BusinessException.notFound("No active subscription found"));
    }
}
