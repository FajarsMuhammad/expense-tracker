package com.fajars.expensetracker.subscription;

import com.fajars.expensetracker.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubscriptionHelper.
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionHelperTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionHelper subscriptionHelper;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
            .id(userId)
            .email("test@example.com")
            .build();
    }

    // ========== isPremiumUser Tests ==========

    @Test
    void isPremiumUser_ShouldReturnTrue_WhenUserHasActivePremium() {
        // Arrange
        Subscription premiumSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(premiumSubscription));

        // Act
        boolean result = subscriptionHelper.isPremiumUser(userId);

        // Assert
        assertTrue(result);
        verify(subscriptionRepository).findActiveSubscriptionByUserId(userId);
    }

    @Test
    void isPremiumUser_ShouldReturnFalse_WhenUserHasFree() {
        // Arrange
        Subscription freeSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(freeSubscription));

        // Act
        boolean result = subscriptionHelper.isPremiumUser(userId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isPremiumUser_ShouldReturnFalse_WhenNoSubscription() {
        // Arrange
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.empty());

        // Act
        boolean result = subscriptionHelper.isPremiumUser(userId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isPremiumUser_ShouldReturnTrue_WhenUserHasTrial() {
        // Arrange
        Subscription trialSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.TRIAL)
            .startedAt(LocalDateTime.now())
            .endedAt(LocalDateTime.now().plusDays(14))
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(trialSubscription));

        // Act
        boolean result = subscriptionHelper.isPremiumUser(userId);

        // Assert
        assertTrue(result); // Trial is considered premium
    }

    // ========== getUserTier Tests ==========

    @Test
    void getUserTier_ShouldReturnPREMIUM_WhenUserIsPremium() {
        // Arrange
        Subscription premiumSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(premiumSubscription));

        // Act
        SubscriptionTier result = subscriptionHelper.getUserTier(userId);

        // Assert
        assertEquals(SubscriptionTier.PREMIUM, result);
    }

    @Test
    void getUserTier_ShouldReturnFREE_WhenUserHasFree() {
        // Arrange
        Subscription freeSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(freeSubscription));

        // Act
        SubscriptionTier result = subscriptionHelper.getUserTier(userId);

        // Assert
        assertEquals(SubscriptionTier.FREE, result);
    }

    @Test
    void getUserTier_ShouldReturnFREE_WhenNoSubscription() {
        // Arrange
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.empty());

        // Act
        SubscriptionTier result = subscriptionHelper.getUserTier(userId);

        // Assert
        assertEquals(SubscriptionTier.FREE, result);
    }

    // ========== getExportLimit Tests ==========

    @Test
    void getExportLimit_ShouldReturn10000_ForPremiumUser() {
        // Arrange
        Subscription premiumSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(premiumSubscription));

        // Act
        int result = subscriptionHelper.getExportLimit(userId);

        // Assert
        assertEquals(10000, result);
    }

    @Test
    void getExportLimit_ShouldReturn100_ForFreeUser() {
        // Arrange
        Subscription freeSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(freeSubscription));

        // Act
        int result = subscriptionHelper.getExportLimit(userId);

        // Assert
        assertEquals(100, result);
    }

    @Test
    void getExportLimit_ShouldReturn10000_ForTrialUser() {
        // Arrange
        Subscription trialSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.TRIAL)
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(trialSubscription));

        // Act
        int result = subscriptionHelper.getExportLimit(userId);

        // Assert
        assertEquals(10000, result); // Trial gets premium limit
    }

    // ========== getDateRangeLimit Tests ==========

    @Test
    void getDateRangeLimit_ShouldReturn365_ForPremiumUser() {
        // Arrange
        Subscription premiumSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(premiumSubscription));

        // Act
        int result = subscriptionHelper.getDateRangeLimit(userId);

        // Assert
        assertEquals(365, result);
    }

    @Test
    void getDateRangeLimit_ShouldReturn90_ForFreeUser() {
        // Arrange
        Subscription freeSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(freeSubscription));

        // Act
        int result = subscriptionHelper.getDateRangeLimit(userId);

        // Assert
        assertEquals(90, result);
    }

    @Test
    void getDateRangeLimit_ShouldReturn90_WhenNoSubscription() {
        // Arrange
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.empty());

        // Act
        int result = subscriptionHelper.getDateRangeLimit(userId);

        // Assert
        assertEquals(90, result); // Defaults to FREE limit
    }
}
