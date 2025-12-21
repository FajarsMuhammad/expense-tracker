package com.fajars.expensetracker.subscription.usecase.getusersubscription;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.subscription.domain.Subscription;
import com.fajars.expensetracker.subscription.domain.SubscriptionRepository;
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

    private final CurrentUserProvider userProvider;

    @Override
    public Subscription getSubscription() {
        UUID userId = userProvider.getUserId();

        log.debug("Getting subscription for user: {}", userId);

        return subscriptionRepository.findActiveSubscriptionByUserId(userId)
            .orElseThrow(() -> BusinessException.notFound("No active subscription found"));
    }
}
