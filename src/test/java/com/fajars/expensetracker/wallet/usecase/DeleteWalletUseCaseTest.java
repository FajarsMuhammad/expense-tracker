package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.wallet.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteWalletUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private com.fajars.expensetracker.common.logging.BusinessEventLogger businessEventLogger;

    @InjectMocks
    private DeleteWalletUseCase useCase;

    private UUID userId;
    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();

        wallet = Wallet.builder()
                .id(walletId)
                .user(com.fajars.expensetracker.user.User.builder().id(userId).build())
                .name("Main Wallet")
                .currency(Currency.IDR)
                .initialBalance(1000000.0)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    @Test
    void delete_ShouldDeleteWallet_WhenValidRequest() {
        // Arrange
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));

        // Act
        useCase.delete(walletId, userId);

        // Assert
        verify(walletRepository).findByIdAndUserId(walletId, userId);
        verify(walletRepository).delete(wallet);
    }

    @Test
    void delete_ShouldThrowException_WhenWalletNotFound() {
        // Arrange
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.delete(walletId, userId));
        verify(walletRepository, never()).delete(any());
    }
}
