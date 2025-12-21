package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.user.domain.User;
import com.fajars.expensetracker.wallet.api.CreateWalletRequest;
import com.fajars.expensetracker.wallet.api.WalletResponse;
import com.fajars.expensetracker.wallet.domain.Currency;
import com.fajars.expensetracker.wallet.domain.Wallet;
import com.fajars.expensetracker.wallet.domain.WalletRepository;
import com.fajars.expensetracker.wallet.usecase.createwallet.CreateWalletUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWalletUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private com.fajars.expensetracker.common.metrics.MetricsService metricsService;

    @Mock
    private com.fajars.expensetracker.common.logging.BusinessEventLogger businessEventLogger;

    @Mock
    private com.fajars.expensetracker.subscription.SubscriptionHelper subscriptionHelper;

    @InjectMocks
    private CreateWalletUseCase useCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void create_ShouldCreateWallet_WhenValidRequest() {
        // Arrange
        CreateWalletRequest request = new CreateWalletRequest("Main Wallet", Currency.IDR, 1000000.0);
        when(subscriptionHelper.isPremiumUser(userId)).thenReturn(false);
        when(walletRepository.countByUserId(userId)).thenReturn(0L);

        Wallet savedWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(userId).build())
                .name("Main Wallet")
                .currency(Currency.IDR)
                .initialBalance(1000000.0)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        // Act
        WalletResponse result = useCase.create(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Main Wallet", result.name());
        assertEquals(Currency.IDR, result.currency());
        assertEquals(1000000.0, result.initialBalance());
        verify(walletRepository).countByUserId(userId);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void create_ShouldThrowException_WhenWalletLimitExceeded() {
        // Arrange
        CreateWalletRequest request = new CreateWalletRequest("Second Wallet", Currency.IDR, 1000000.0);
        when(subscriptionHelper.isPremiumUser(userId)).thenReturn(false);
        when(walletRepository.countByUserId(userId)).thenReturn(1L);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> useCase.create(userId, request));
        verify(walletRepository).countByUserId(userId);
        verify(walletRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenNameIsEmpty() {
        // Arrange
        CreateWalletRequest request = new CreateWalletRequest("", Currency.IDR, 1000000.0);
        when(subscriptionHelper.isPremiumUser(userId)).thenReturn(false);
        when(walletRepository.countByUserId(userId)).thenReturn(0L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.create(userId, request));
        verify(walletRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenInitialBalanceIsNegative() {
        // Arrange
        CreateWalletRequest request = new CreateWalletRequest("Wallet", Currency.IDR, -100.0);
        when(subscriptionHelper.isPremiumUser(userId)).thenReturn(false);
        when(walletRepository.countByUserId(userId)).thenReturn(0L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.create(userId, request));
        verify(walletRepository, never()).save(any());
    }
}
