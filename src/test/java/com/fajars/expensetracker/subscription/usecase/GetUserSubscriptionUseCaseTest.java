package com.fajars.expensetracker.subscription.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.subscription.domain.Subscription;
import com.fajars.expensetracker.subscription.domain.SubscriptionRepository;
import com.fajars.expensetracker.subscription.domain.SubscriptionStatus;
import com.fajars.expensetracker.subscription.domain.SubscriptionTier;
import com.fajars.expensetracker.subscription.usecase.getusersubscription.GetUserSubscriptionUseCase;
import com.fajars.expensetracker.user.domain.User;
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
 * Unit tests for GetUserSubscriptionUseCase.
 */
@ExtendWith(MockitoExtension.class)
class GetUserSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private GetUserSubscriptionUseCase useCase;

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
    void getSubscription_ShouldReturnSubscription_WhenExists() {
        // Arrange
        Subscription subscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .startedAt(LocalDateTime.now())
            .endedAt(LocalDateTime.now().plusDays(30))
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(subscription));

        // Act
        Subscription result = useCase.getSubscription();

        // Assert
        assertNotNull(result);
        assertEquals(subscription.getId(), result.getId());
        assertEquals(SubscriptionTier.PREMIUM, result.getPlan());
        assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());

        verify(subscriptionRepository).findActiveSubscriptionByUserId(userId);
    }

    @Test
    void getSubscription_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> useCase.getSubscription());

        assertEquals("No active subscription found", exception.getMessage());
        verify(subscriptionRepository).findActiveSubscriptionByUserId(userId);
    }

    @Test
    void getSubscription_ShouldReturnFreeSubscription_WhenUserHasFree() {
        // Arrange
        Subscription freeSubscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .startedAt(LocalDateTime.now())
            .build();

        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
            .thenReturn(Optional.of(freeSubscription));

        // Act
        Subscription result = useCase.getSubscription();

        // Assert
        assertNotNull(result);
        assertEquals(SubscriptionTier.FREE, result.getPlan());
        assertNull(result.getEndedAt()); // FREE has no expiry
    }
}
