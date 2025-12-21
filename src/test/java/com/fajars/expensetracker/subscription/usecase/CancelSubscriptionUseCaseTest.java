package com.fajars.expensetracker.subscription.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.subscription.domain.Subscription;
import com.fajars.expensetracker.subscription.domain.SubscriptionRepository;
import com.fajars.expensetracker.subscription.domain.SubscriptionStatus;
import com.fajars.expensetracker.subscription.domain.SubscriptionTier;
import com.fajars.expensetracker.subscription.usecase.cancelsubscription.CancelSubscriptionUseCase;
import com.fajars.expensetracker.user.domain.User;
import com.fajars.expensetracker.user.domain.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for CancelSubscriptionUseCase.
 */
@ExtendWith(MockitoExtension.class)
class CancelSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private BusinessEventLogger businessEventLogger;

    @InjectMocks
    private CancelSubscriptionUseCase useCase;

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

    @Test
    void cancel_ShouldCancelPremiumSubscription_WhenActive() {
        // Arrange
        Subscription premiumSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .startedAt(LocalDateTime.now())
            .endedAt(LocalDateTime.now().plusDays(30))
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(premiumSubscription));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        useCase.cancel();

        // Assert
        verify(subscriptionRepository).findActiveSubscriptionByUserId(userId);
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(metricsService).incrementCounter(eq("subscription.cancelled"));
        verify(businessEventLogger).logBusinessEvent(eq("SUBSCRIPTION_CANCELLED"), eq(user.getEmail()), any());
    }

    @Test
    void cancel_ShouldThrowException_WhenNoActiveSubscription() {
        // Arrange
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> useCase.cancel());

        assertEquals("No active subscription found", exception.getMessage());
        verify(subscriptionRepository).findActiveSubscriptionByUserId(userId);
        verify(subscriptionRepository, never()).save(any());
        verify(metricsService, never()).incrementCounter(any());
    }

    @Test
    void cancel_ShouldThrowException_WhenTryingToCancelFreeSubscription() {
        // Arrange
        Subscription freeSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(freeSubscription));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> useCase.cancel());

        assertEquals("Cannot cancel FREE tier subscription", exception.getMessage());
        verify(subscriptionRepository).findActiveSubscriptionByUserId(userId);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void cancel_ShouldCancelTrialSubscription_WhenUserHasTrial() {
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
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        useCase.cancel();

        // Assert
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(metricsService).incrementCounter(eq("subscription.cancelled"));
    }
}
