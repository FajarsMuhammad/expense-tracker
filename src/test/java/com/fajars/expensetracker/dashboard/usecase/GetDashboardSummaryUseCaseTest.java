package com.fajars.expensetracker.dashboard.usecase;

import com.fajars.expensetracker.dashboard.DashboardSummaryDto;
import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.transaction.TransactionRepository;
import com.fajars.expensetracker.wallet.Currency;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetDashboardSummaryUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private com.fajars.expensetracker.common.metrics.MetricsService metricsService;

    @InjectMocks
    private GetDashboardSummaryUseCase useCase;

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
                .transactions(new ArrayList<>())
                .build();
    }

    @Test
    void getSummary_ShouldReturnSummary_WhenWalletIdProvided() {
        // Arrange
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByUserIdAndWalletIdAndDateBetween(eq(userId), eq(walletId), any(), any()))
                .thenReturn(new ArrayList<>());
        when(transactionRepository.findTop5ByUserIdAndWalletId(userId, walletId))
                .thenReturn(new ArrayList<>());

        // Act
        DashboardSummaryDto result = useCase.getSummary(userId, walletId);

        // Assert
        assertNotNull(result);
        assertEquals(1000000.0, result.walletBalance());
        assertEquals(0.0, result.todayIncome());
        assertEquals(0.0, result.todayExpense());
        assertNotNull(result.weeklyTrend());
        assertNotNull(result.recentTransactions());
        verify(walletRepository).findByIdAndUserId(walletId, userId);
    }

    @Test
    void getSummary_ShouldReturnSummary_WhenWalletIdIsNull() {
        // Arrange
        when(walletRepository.findByUserId(userId)).thenReturn(Arrays.asList(wallet));
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(new ArrayList<>());
        when(transactionRepository.findTop5ByUserId(userId))
                .thenReturn(new ArrayList<>());

        // Act
        DashboardSummaryDto result = useCase.getSummary(userId, null);

        // Assert
        assertNotNull(result);
        assertEquals(1000000.0, result.walletBalance());
        assertEquals(0.0, result.todayIncome());
        assertEquals(0.0, result.todayExpense());
        verify(walletRepository).findByUserId(userId);
    }

    @Test
    void getSummary_ShouldReturnZeroBalance_WhenWalletNotFound() {
        // Arrange
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());
        when(transactionRepository.findByUserIdAndWalletIdAndDateBetween(eq(userId), eq(walletId), any(), any()))
                .thenReturn(new ArrayList<>());
        when(transactionRepository.findTop5ByUserIdAndWalletId(userId, walletId))
                .thenReturn(new ArrayList<>());

        // Act
        DashboardSummaryDto result = useCase.getSummary(userId, walletId);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.walletBalance());
        verify(walletRepository).findByIdAndUserId(walletId, userId);
    }

    @Test
    void getSummary_ShouldCalculateWeeklyTrend() {
        // Arrange
        when(walletRepository.findByUserId(userId)).thenReturn(Arrays.asList(wallet));
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(new ArrayList<>());
        when(transactionRepository.findTop5ByUserId(userId))
                .thenReturn(new ArrayList<>());

        // Act
        DashboardSummaryDto result = useCase.getSummary(userId, null);

        // Assert
        assertNotNull(result);
        assertNotNull(result.weeklyTrend());
        assertEquals(7, result.weeklyTrend().size()); // Should have 7 days
    }
}
