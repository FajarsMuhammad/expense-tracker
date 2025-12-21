package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.debt.api.DebtResponse;
import com.fajars.expensetracker.debt.api.UpdateDebtRequest;
import com.fajars.expensetracker.debt.domain.Debt;
import com.fajars.expensetracker.debt.domain.DebtRepository;
import com.fajars.expensetracker.debt.domain.DebtStatus;
import com.fajars.expensetracker.debt.domain.DebtType;
import com.fajars.expensetracker.debt.usecase.updatedebt.UpdateDebtUseCase;
import com.fajars.expensetracker.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class UpdateDebtUseCaseTest {

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private BusinessEventLogger businessEventLogger;

    @InjectMocks
    private UpdateDebtUseCase useCase;

    private UUID userId;
    private UUID debtId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        debtId = UUID.randomUUID();
    }

    @Test
    void update_ShouldUpdateDebt_WhenValidRequest() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(30);
        UpdateDebtRequest request = new UpdateDebtRequest(
            DebtType.RECEIVABLE,
            "Jane Doe",
            1200.0,
            dueDate,
            "Updated loan"
        );

        Debt existingDebt = Debt.builder()
            .id(debtId)
            .user(User.builder().id(userId).build())
            .type(DebtType.PAYABLE)
            .counterpartyName("John Doe")
            .totalAmount(1000.0)
            .remainingAmount(1000.0)
            .dueDate(LocalDateTime.now().plusDays(20))
            .status(DebtStatus.OPEN)
            .note("Original loan")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(existingDebt));
        when(debtRepository.save(any(Debt.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        DebtResponse result = useCase.update(userId, debtId, request);

        // Assert
        assertNotNull(result);
        assertEquals(DebtType.RECEIVABLE, result.type());
        assertEquals("Jane Doe", result.counterpartyName());
        assertEquals(1200.0, result.totalAmount());
        assertEquals("Updated loan", result.note());

        verify(debtRepository).findByIdAndUserId(debtId, userId);
        verify(debtRepository).save(any(Debt.class));
        verify(metricsService).incrementCounter(eq("debts.updated.total"));
        verify(businessEventLogger).logBusinessEvent(eq("DEBT_UPDATED"), anyString(), anyMap());
    }

    @Test
    void update_ShouldThrowException_WhenDebtNotFound() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(30);
        UpdateDebtRequest request = new UpdateDebtRequest(
            DebtType.PAYABLE,
            "Test User",
            1000.0,
            dueDate,
            null
        );

        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            useCase.update(userId, debtId, request);
        });

        verify(debtRepository).findByIdAndUserId(debtId, userId);
        verify(debtRepository, never()).save(any(Debt.class));
    }

    @Test
    void update_ShouldRecalculateRemainingAmount_WhenTotalAmountChanges() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(30);
        UpdateDebtRequest request = new UpdateDebtRequest(
            DebtType.PAYABLE,
            "John Doe",
            1500.0,  // Increase from 1000 to 1500
            dueDate,
            "Updated amount"
        );

        Debt existingDebt = Debt.builder()
            .id(debtId)
            .user(User.builder().id(userId).build())
            .type(DebtType.PAYABLE)
            .counterpartyName("John Doe")
            .totalAmount(1000.0)
            .remainingAmount(700.0)  // 300 already paid
            .dueDate(LocalDateTime.now().plusDays(20))
            .status(DebtStatus.PARTIAL)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        ArgumentCaptor<Debt> debtCaptor = ArgumentCaptor.forClass(Debt.class);
        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(existingDebt));
        when(debtRepository.save(debtCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        DebtResponse result = useCase.update(userId, debtId, request);

        // Assert
        Debt savedDebt = debtCaptor.getValue();
        assertEquals(1500.0, savedDebt.getTotalAmount());
        assertEquals(1200.0, savedDebt.getRemainingAmount());  // 1500 - 300 (already paid)
        assertEquals(DebtStatus.PARTIAL, savedDebt.getStatus());
    }

    @Test
    void update_ShouldThrowException_WhenNewTotalLessThanPaidAmount() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(30);
        UpdateDebtRequest request = new UpdateDebtRequest(
            DebtType.PAYABLE,
            "John Doe",
            200.0,  // Decrease from 1000 to 200, but 300 already paid
            dueDate,
            null
        );

        Debt existingDebt = Debt.builder()
            .id(debtId)
            .user(User.builder().id(userId).build())
            .type(DebtType.PAYABLE)
            .counterpartyName("John Doe")
            .totalAmount(1000.0)
            .remainingAmount(700.0)  // 300 already paid
            .dueDate(LocalDateTime.now().plusDays(20))
            .status(DebtStatus.PARTIAL)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(existingDebt));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.update(userId, debtId, request);
        });

        assertTrue(exception.getMessage().contains("less than the amount already paid"));
        verify(debtRepository, never()).save(any(Debt.class));
    }

    @Test
    void update_ShouldUpdateStatusToPaid_WhenRemainingBecomesZero() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(30);
        UpdateDebtRequest request = new UpdateDebtRequest(
            DebtType.PAYABLE,
            "John Doe",
            300.0,  // Reduce total to exactly match paid amount
            dueDate,
            null
        );

        Debt existingDebt = Debt.builder()
            .id(debtId)
            .user(User.builder().id(userId).build())
            .type(DebtType.PAYABLE)
            .counterpartyName("John Doe")
            .totalAmount(1000.0)
            .remainingAmount(700.0)  // 300 already paid
            .dueDate(LocalDateTime.now().plusDays(20))
            .status(DebtStatus.PARTIAL)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        ArgumentCaptor<Debt> debtCaptor = ArgumentCaptor.forClass(Debt.class);
        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(existingDebt));
        when(debtRepository.save(debtCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        useCase.update(userId, debtId, request);

        // Assert
        Debt savedDebt = debtCaptor.getValue();
        assertEquals(300.0, savedDebt.getTotalAmount());
        assertEquals(0.0, savedDebt.getRemainingAmount());
        assertEquals(DebtStatus.PAID, savedDebt.getStatus());
    }

    @Test
    void update_ShouldRecordMetrics() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(30);
        UpdateDebtRequest request = new UpdateDebtRequest(
            DebtType.PAYABLE,
            "Test User",
            1000.0,
            dueDate,
            null
        );

        Debt existingDebt = Debt.builder()
            .id(debtId)
            .user(User.builder().id(userId).build())
            .type(DebtType.PAYABLE)
            .counterpartyName("Test User")
            .totalAmount(1000.0)
            .remainingAmount(1000.0)
            .dueDate(dueDate)
            .status(DebtStatus.OPEN)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(existingDebt));
        when(debtRepository.save(any(Debt.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        useCase.update(userId, debtId, request);

        // Assert
        verify(metricsService).incrementCounter(eq("debts.updated.total"));
        verify(metricsService).recordTimer(eq("debt.update.duration"), anyLong());
    }
}
