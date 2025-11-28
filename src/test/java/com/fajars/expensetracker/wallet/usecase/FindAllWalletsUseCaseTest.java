package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.wallet.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindAllWalletsUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private FindAllWalletsUseCase useCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void findAllByUserId_ShouldReturnWallets() {
        // Arrange
        Wallet wallet1 = Wallet.builder()
                .id(UUID.randomUUID())
                .user(com.fajars.expensetracker.user.User.builder().id(userId).build())
                .name("Main Wallet")
                .currency(Currency.IDR)
                .initialBalance(1000000.0)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        Wallet wallet2 = Wallet.builder()
                .id(UUID.randomUUID())
                .user(com.fajars.expensetracker.user.User.builder().id(userId).build())
                .name("Savings")
                .currency(Currency.USD)
                .initialBalance(500.0)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        when(walletRepository.findByUserId(userId)).thenReturn(Arrays.asList(wallet1, wallet2));

        // Act
        List<WalletResponse> result = useCase.findAllByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Main Wallet", result.get(0).name());
        assertEquals("Savings", result.get(1).name());
        verify(walletRepository).findByUserId(userId);
    }

    @Test
    void findAllByUserId_ShouldReturnEmptyList_WhenNoWallets() {
        // Arrange
        when(walletRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        List<WalletResponse> result = useCase.findAllByUserId(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(walletRepository).findByUserId(userId);
    }
}
