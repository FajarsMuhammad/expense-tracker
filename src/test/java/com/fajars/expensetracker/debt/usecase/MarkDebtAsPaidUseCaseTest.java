package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.debt.Debt;
import com.fajars.expensetracker.debt.DebtRepository;
import com.fajars.expensetracker.debt.DebtResponse;
import com.fajars.expensetracker.debt.DebtStatus;
import com.fajars.expensetracker.user.User;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkDebtAsPaidUseCaseTest {

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private BusinessEventLogger businessEventLogger;

    @InjectMocks
    private MarkDebtAsPaidUseCase useCase;

    private UUID userId;
    private UUID debtId;
    private Debt debt;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        debtId = UUID.randomUUID();

        debt = Debt.builder()
            .id(debtId)
            .user(User.builder().id(userId).build())
            .counterpartyName("John Doe")
            .totalAmount(1000.0)
            .remainingAmount(500.0)
            .status(DebtStatus.PARTIAL)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void markAsPaid_ShouldMarkDebtAsPaid_WhenValid() {
        // Arrange
        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(debt));
        when(debtRepository.save(any(Debt.class))).thenReturn(debt);

        // Act
        DebtResponse result = useCase.markAsPaid(userId, debtId);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.remainingAmount());
        assertEquals(DebtStatus.PAID, result.status());

        verify(debtRepository).findByIdAndUserId(debtId, userId);
        verify(debtRepository).save(debt);
        verify(businessEventLogger).logBusinessEvent(eq("DEBT_MARKED_PAID"), anyString(), anyMap());
    }

    @Test
    void markAsPaid_ShouldReturnSameDebt_WhenAlreadyPaid() {
        // Arrange
        debt.setStatus(DebtStatus.PAID);
        debt.setRemainingAmount(0.0);

        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(debt));

        // Act
        DebtResponse result = useCase.markAsPaid(userId, debtId);

        // Assert
        assertNotNull(result);
        assertEquals(DebtStatus.PAID, result.status());
        verify(debtRepository, never()).save(any());
    }

    @Test
    void markAsPaid_ShouldThrowException_WhenDebtNotFound() {
        // Arrange
        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> useCase.markAsPaid(userId, debtId));

        verify(debtRepository, never()).save(any());
    }

    @Test
    void markAsPaid_ShouldRecordMetrics() {
        // Arrange
        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(debt));
        when(debtRepository.save(any(Debt.class))).thenReturn(debt);

        // Act
        useCase.markAsPaid(userId, debtId);

        // Assert
        verify(metricsService).incrementCounter(eq("debts.marked.paid.total"));
        verify(metricsService).recordTimer(eq("debt.mark.paid.duration"), anyLong());
    }
}
