package com.fajars.expensetracker.subscription.usecase.checktrialeligibility;

import com.fajars.expensetracker.payment.domain.PaymentRepository;
import com.fajars.expensetracker.subscription.domain.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case implementation for checking trial eligibility.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CheckTrialEligibilityUseCase implements CheckTrialEligibility {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public boolean isEligible(UUID userId) {
        log.debug("Checking trial eligibility for user: {}", userId);

        // User is not eligible if they have EVER had a trial (including registration trial)
        // Since Milestone 6: All users get trial at registration, so they can only trial once
        boolean hasHadTrial = subscriptionRepository.hasHadTrialSubscription(userId);
        if (hasHadTrial) {
            log.debug("User {} not eligible: already had trial (including registration trial)", userId);
            return false;
        }

        // User is not eligible if they have had premium subscription before
        boolean hasHadPremium = subscriptionRepository.hasHadPremiumSubscription(userId);
        if (hasHadPremium) {
            log.debug("User {} not eligible: has had premium subscription", userId);
            return false;
        }

        // User is not eligible if they have successful payment before
        boolean hasSuccessfulPayment = paymentRepository.hasSuccessfulPayment(userId);
        if (hasSuccessfulPayment) {
            log.debug("User {} not eligible: has successful payment", userId);
            return false;
        }

        log.debug("User {} is eligible for trial", userId);
        return true;
    }
}
