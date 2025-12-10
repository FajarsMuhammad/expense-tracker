package com.fajars.expensetracker.user.usecase;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.subscription.Subscription;
import com.fajars.expensetracker.subscription.SubscriptionRepository;
import com.fajars.expensetracker.user.ProfileResponse;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Use case implementation for getting complete user profile.
 *
 * @since Milestone 6
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetUserProfileUseCase implements GetUserProfile {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        log.debug("Getting profile for user: {}", userId);

        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("User not found"));

        // Get current subscription
        Subscription subscription = subscriptionRepository.findActiveSubscriptionByUserId(userId)
            .orElseThrow(() -> BusinessException.notFound("No active subscription found"));

        // Build subscription info
        ProfileResponse.SubscriptionInfo subscriptionInfo = buildSubscriptionInfo(subscription);

        // Build profile response
        ProfileResponse response = ProfileResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .locale(user.getLocale())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .subscription(subscriptionInfo)
            .build();

        log.debug("Profile retrieved for user: {}", userId);
        return response;
    }

    /**
     * Build subscription info with trial days remaining calculation.
     */
    private ProfileResponse.SubscriptionInfo buildSubscriptionInfo(Subscription subscription) {
        Integer trialDaysRemaining = null;

        if (subscription.isTrial() && subscription.getEndedAt() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndedAt());
            trialDaysRemaining = (int) Math.max(0, daysRemaining);
        }

        return ProfileResponse.SubscriptionInfo.builder()
            .subscriptionId(subscription.getId())
            .tier(subscription.getPlan())
            .status(subscription.getStatus())
            .isPremium(subscription.isPremium())
            .isTrial(subscription.isTrial())
            .startedAt(subscription.getStartedAt())
            .endedAt(subscription.getEndedAt())
            .trialDaysRemaining(trialDaysRemaining)
            .build();
    }
}
