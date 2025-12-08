package com.fajars.expensetracker.subscription.usecase;


import com.fajars.expensetracker.subscription.Subscription;
import com.fajars.expensetracker.subscription.SubscriptionRepository;
import com.fajars.expensetracker.subscription.SubscriptionTier;
import com.fajars.expensetracker.subscription.UpgradeInfoResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author : fajars
 * @since : 06/12/25
 */
@Service
@RequiredArgsConstructor
public class UpgradeSubscriptionUseCase implements UpgradeSubscription {

    private final SubscriptionRepository subscriptionRepository;

    public UpgradeInfoResponse upgrade(UUID userId) {

        Subscription current = subscriptionRepository
            .findActiveSubscriptionByUserId(userId)
            .orElseThrow(() -> new RuntimeException("No active subscription found"));

        if (current.isPremium()) {
            return UpgradeInfoResponse.builder()
                .message("You already have an active premium subscription")
                .currentTier(current.getPlan())
                .premium(true)
                .build();
        }

        return UpgradeInfoResponse.builder()
            .message("To upgrade to premium, create a payment using POST /payments/subscription")
            .currentTier(current.getPlan())
            .targetTier(SubscriptionTier.PREMIUM)
            .price(25000.00)
            .currency("IDR")
            .duration("30 days")
            .premium(false)
            .paymentEndpoint("/payments/subscription")
            .build();
    }
}
