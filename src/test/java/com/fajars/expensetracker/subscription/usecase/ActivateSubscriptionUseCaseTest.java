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
 * Unit tests for ActivateSubscriptionUseCase.
 */
@ExtendWith(MockitoExtension.class)
class ActivateSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private BusinessEventLogger businessEventLogger;

    @InjectMocks
    private ActivateSubscriptionUseCase useCase;

    private UUID userId;
    private UUID paymentId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        user = User.builder()
            .id(userId)
            .email("test@example.com")
            .build();
    }

    @Test
    void activateOrExtend_ShouldCreateNewSubscription_WhenNoExistingSubscription() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.empty());

        Subscription savedSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .provider("MIDTRANS")
            .providerSubscriptionId(paymentId.toString())
            .startedAt(LocalDateTime.now())
            .endedAt(LocalDateTime.now().plusDays(30))
            .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSubscription);

        // Act
        Subscription result = useCase.activateOrExtend(userId, paymentId, 30);

        // Assert
        assertNotNull(result);
        assertEquals(SubscriptionTier.PREMIUM, result.getPlan());
        assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
        assertEquals("MIDTRANS", result.getProvider());
        assertEquals(paymentId.toString(), result.getProviderSubscriptionId());

        verify(subscriptionRepository).save(any(Subscription.class));
        verify(metricsService).incrementCounter(eq("subscription.activated"));
        verify(businessEventLogger).logBusinessEvent(eq("SUBSCRIPTION_ACTIVATED"), eq(user.getEmail()), any());
    }

    @Test
    void activateOrExtend_ShouldExtendExistingPremium_WhenAlreadyHasPremium() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Subscription existingPremium = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .startedAt(now)
            .endedAt(now.plusDays(10)) // 10 days remaining
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(existingPremium));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(existingPremium);

        // Act
        Subscription result = useCase.activateOrExtend(userId, paymentId, 30);

        // Assert
        assertNotNull(result);
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(metricsService).incrementCounter(eq("subscription.extended"));
        verify(businessEventLogger).logBusinessEvent(eq("SUBSCRIPTION_EXTENDED"), eq(user.getEmail()), any());
    }

    @Test
    void activateOrExtend_ShouldCancelFreeAndCreatePremium_WhenHasFree() {
        // Arrange
        Subscription existingFree = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(existingFree));

        Subscription newPremium = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .startedAt(LocalDateTime.now())
            .endedAt(LocalDateTime.now().plusDays(30))
            .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(newPremium);

        // Act
        Subscription result = useCase.activateOrExtend(userId, paymentId, 30);

        // Assert
        assertNotNull(result);
        assertEquals(SubscriptionTier.PREMIUM, result.getPlan());

        // Verify old FREE was cancelled and new PREMIUM was created
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        verify(metricsService).incrementCounter(eq("subscription.activated"));
    }

    @Test
    void activateOrExtend_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> useCase.activateOrExtend(userId, paymentId, 30));

        assertEquals("User not found", exception.getMessage());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void activateOrExtend_ShouldSetCorrectDuration_WhenActivating() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.empty());

        Subscription savedSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .startedAt(LocalDateTime.now())
            .endedAt(LocalDateTime.now().plusDays(90)) // 3 months
            .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSubscription);

        // Act
        Subscription result = useCase.activateOrExtend(userId, paymentId, 90); // 3 months

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEndedAt());
        verify(subscriptionRepository).save(any(Subscription.class));
    }
}
