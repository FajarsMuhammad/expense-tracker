package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.user.domain.User;
import com.fajars.expensetracker.wallet.api.UpdateWalletRequest;
import com.fajars.expensetracker.wallet.api.WalletResponse;
import com.fajars.expensetracker.wallet.domain.Currency;
import com.fajars.expensetracker.wallet.domain.Wallet;
import com.fajars.expensetracker.wallet.domain.WalletRepository;
import com.fajars.expensetracker.wallet.usecase.update.UpdateWalletUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateWalletUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private com.fajars.expensetracker.common.logging.BusinessEventLogger businessEventLogger;

    @InjectMocks
    private UpdateWalletUseCase useCase;

    private UUID userId;
    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();

        wallet = Wallet.builder()
                .id(walletId)
                .user(User.builder().id(userId).build())
                .name("Main Wallet")
                .currency(Currency.IDR)
                .initialBalance(1000000.0)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    @Test
    void update_ShouldUpdateWallet_WhenValidRequest() {
        // Arrange
        UpdateWalletRequest request = new UpdateWalletRequest("Updated Wallet", Currency.USD, 2000.0);
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // Act
        WalletResponse result = useCase.update(walletId, userId, request);

        // Assert
        assertNotNull(result);
        verify(walletRepository).findByIdAndUserId(walletId, userId);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void update_ShouldThrowException_WhenWalletNotFound() {
        // Arrange
        UpdateWalletRequest request = new UpdateWalletRequest("Updated", Currency.USD, 2000.0);
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.update(walletId, userId, request));
        verify(walletRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenNameIsEmpty() {
        // Arrange
        UpdateWalletRequest request = new UpdateWalletRequest("", Currency.USD, 2000.0);
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.update(walletId, userId, request));
        verify(walletRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenInitialBalanceIsNegative() {
        // Arrange
        UpdateWalletRequest request = new UpdateWalletRequest("Wallet", Currency.USD, -100.0);
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.update(walletId, userId, request));
        verify(walletRepository, never()).save(any());
    }
}
