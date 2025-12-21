package com.fajars.expensetracker.subscription.usecase;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.subscription.domain.Subscription;
import com.fajars.expensetracker.subscription.domain.SubscriptionRepository;
import com.fajars.expensetracker.subscription.domain.SubscriptionStatus;
import com.fajars.expensetracker.subscription.domain.SubscriptionTier;
import com.fajars.expensetracker.subscription.usecase.createfreesubscription.CreateFreeSubscriptionUseCase;
import com.fajars.expensetracker.user.domain.User;
import com.fajars.expensetracker.user.domain.UserRepository;
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
 * Unit tests for CreateFreeSubscriptionUseCase.
 */
@ExtendWith(MockitoExtension.class)
class CreateFreeSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private BusinessEventLogger businessEventLogger;

    @InjectMocks
    private CreateFreeSubscriptionUseCase useCase;

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
    void createFree_ShouldCreateFreeSubscription_WhenUserExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Subscription savedSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .startedAt(LocalDateTime.now())
            .endedAt(null)
            .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSubscription);

        // Act
        Subscription result = useCase.createFree(userId);

        // Assert
        assertNotNull(result);
        assertEquals(SubscriptionTier.FREE, result.getPlan());
        assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
        assertNull(result.getEndedAt()); // FREE tier has no expiry
        assertNotNull(result.getStartedAt());

        verify(userRepository).findById(userId);
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(metricsService).incrementCounter(eq("subscription.created"), eq("tier"), eq("FREE"));
        verify(businessEventLogger).logBusinessEvent(eq("SUBSCRIPTION_CREATED"), eq(user.getEmail()), any());
    }

    @Test
    void createFree_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> useCase.createFree(userId));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(subscriptionRepository, never()).save(any());
        verify(metricsService, never()).incrementCounter(any(), any(), any());
    }

    @Test
    void createFree_ShouldHaveNoProvider_ForFreeSubscription() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Subscription savedSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .provider(null)
            .providerSubscriptionId(null)
            .startedAt(LocalDateTime.now())
            .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSubscription);

        // Act
        Subscription result = useCase.createFree(userId);

        // Assert
        assertNull(result.getProvider());
        assertNull(result.getProviderSubscriptionId());
    }
}
