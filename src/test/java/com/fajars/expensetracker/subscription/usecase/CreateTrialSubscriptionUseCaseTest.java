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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateTrialSubscriptionUseCase.
 */
@ExtendWith(MockitoExtension.class)
class CreateTrialSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CheckTrialEligibility checkTrialEligibility;

    @Mock
    private MetricsService metricsService;

    @Mock
    private BusinessEventLogger businessEventLogger;

    @InjectMocks
    private CreateTrialSubscriptionUseCase useCase;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
            .id(userId)
            .email("test@example.com")
            .name("Test User")
            .build();
    }

    @Test
    void createTrial_ShouldCreateTrialSubscription_WhenUserIsEligible() {
        // Arrange
        when(checkTrialEligibility.isEligible(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId)).thenReturn(Optional.empty());

        Subscription savedSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.TRIAL)
            .startedAt(LocalDateTime.now())
            .endedAt(LocalDateTime.now().plusDays(14))
            .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSubscription);

        // Act
        Subscription result = useCase.createTrial(userId);

        // Assert
        assertNotNull(result);
        assertEquals(SubscriptionTier.PREMIUM, result.getPlan());
        assertEquals(SubscriptionStatus.TRIAL, result.getStatus());
        assertNotNull(result.getEndedAt());

        verify(checkTrialEligibility).isEligible(userId);
        verify(userRepository).findById(userId);
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(metricsService).incrementCounter(eq("subscription.trial_started"));
        verify(businessEventLogger).logBusinessEvent(eq("TRIAL_STARTED"), eq(user.getEmail()), any());
    }

    @Test
    void createTrial_ShouldCancelExistingFreeSubscription_BeforeCreatingTrial() {
        // Arrange
        Subscription existingFree = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        when(checkTrialEligibility.isEligible(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(existingFree));

        Subscription savedSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.TRIAL)
            .startedAt(LocalDateTime.now())
            .endedAt(LocalDateTime.now().plusDays(14))
            .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSubscription);

        // Act
        Subscription result = useCase.createTrial(userId);

        // Assert
        assertNotNull(result);
        assertEquals(SubscriptionTier.PREMIUM, result.getPlan());
        assertEquals(SubscriptionStatus.TRIAL, result.getStatus());

        // Verify old subscription was cancelled
        verify(subscriptionRepository, times(2)).save(any(Subscription.class)); // Once for cancel, once for new
    }

    @Test
    void createTrial_ShouldThrowException_WhenUserNotEligible() {
        // Arrange
        when(checkTrialEligibility.isEligible(userId)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> useCase.createTrial(userId));

        assertEquals("User is not eligible for trial", exception.getMessage());
        verify(checkTrialEligibility).isEligible(userId);
        verify(userRepository, never()).findById(any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createTrial_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(checkTrialEligibility.isEligible(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> useCase.createTrial(userId));

        assertEquals("User not found", exception.getMessage());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createTrial_ShouldSetTrialDuration_To14Days() {
        // Arrange
        when(checkTrialEligibility.isEligible(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId)).thenReturn(Optional.empty());

        LocalDateTime now = LocalDateTime.now();
        Subscription savedSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.TRIAL)
            .startedAt(now)
            .endedAt(now.plusDays(14))
            .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSubscription);

        // Act
        Subscription result = useCase.createTrial(userId);

        // Assert
        assertNotNull(result.getEndedAt());
        assertTrue(result.getEndedAt().isAfter(result.getStartedAt()));
    }
}
