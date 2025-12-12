package com.fajars.expensetracker.subscription.usecase;

import com.fajars.expensetracker.payment.PaymentRepository;
import com.fajars.expensetracker.subscription.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CheckTrialEligibilityUseCase.
 */
@ExtendWith(MockitoExtension.class)
class CheckTrialEligibilityUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private CheckTrialEligibilityUseCase useCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void isEligible_ShouldReturnTrue_WhenUserNeverHadPremiumAndNoPayment() {
        // Arrange
        when(subscriptionRepository.hasHadTrialSubscription(userId)).thenReturn(false);
        when(subscriptionRepository.hasHadPremiumSubscription(userId)).thenReturn(false);
        when(paymentRepository.hasSuccessfulPayment(userId)).thenReturn(false);

        // Act
        boolean result = useCase.isEligible(userId);

        // Assert
        assertTrue(result);
        verify(subscriptionRepository).hasHadTrialSubscription(userId);
        verify(subscriptionRepository).hasHadPremiumSubscription(userId);
        verify(paymentRepository).hasSuccessfulPayment(userId);
    }

    @Test
    void isEligible_ShouldReturnFalse_WhenUserHadPremiumBefore() {
        // Arrange
        when(subscriptionRepository.hasHadTrialSubscription(userId)).thenReturn(false);
        when(subscriptionRepository.hasHadPremiumSubscription(userId)).thenReturn(true);

        // Act
        boolean result = useCase.isEligible(userId);

        // Assert
        assertFalse(result);
        verify(subscriptionRepository).hasHadTrialSubscription(userId);
        verify(subscriptionRepository).hasHadPremiumSubscription(userId);
        verify(paymentRepository, never()).hasSuccessfulPayment(any()); // Should short-circuit
    }

    @Test
    void isEligible_ShouldReturnFalse_WhenUserHasSuccessfulPayment() {
        // Arrange
        when(subscriptionRepository.hasHadTrialSubscription(userId)).thenReturn(false);
        when(subscriptionRepository.hasHadPremiumSubscription(userId)).thenReturn(false);
        when(paymentRepository.hasSuccessfulPayment(userId)).thenReturn(true);

        // Act
        boolean result = useCase.isEligible(userId);

        // Assert
        assertFalse(result);
        verify(subscriptionRepository).hasHadTrialSubscription(userId);
        verify(subscriptionRepository).hasHadPremiumSubscription(userId);
        verify(paymentRepository).hasSuccessfulPayment(userId);
    }

    @Test
    void isEligible_ShouldReturnFalse_WhenUserHadPremiumAndHasPayment() {
        // Arrange
        when(subscriptionRepository.hasHadTrialSubscription(userId)).thenReturn(false);
        when(subscriptionRepository.hasHadPremiumSubscription(userId)).thenReturn(true);

        // Act
        boolean result = useCase.isEligible(userId);

        // Assert
        assertFalse(result);
        verify(subscriptionRepository).hasHadTrialSubscription(userId);
        verify(subscriptionRepository).hasHadPremiumSubscription(userId);
        // Payment check is skipped due to short-circuit
    }

    @Test
    void isEligible_ShouldReturnFalse_WhenUserAlreadyHadTrial() {
        // Arrange - User had trial at registration
        when(subscriptionRepository.hasHadTrialSubscription(userId)).thenReturn(true);

        // Act
        boolean result = useCase.isEligible(userId);

        // Assert
        assertFalse(result);
        verify(subscriptionRepository).hasHadTrialSubscription(userId);
        // Should short-circuit - no need to check premium or payment
        verify(subscriptionRepository, never()).hasHadPremiumSubscription(any());
        verify(paymentRepository, never()).hasSuccessfulPayment(any());
    }
}
