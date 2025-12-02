package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.debt.*;
import com.fajars.expensetracker.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateDebtUseCaseTest {

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private BusinessEventLogger businessEventLogger;

    @InjectMocks
    private CreateDebtUseCase useCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void create_ShouldCreateDebt_WhenValidRequest() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(30);
        CreateDebtRequest request = new CreateDebtRequest(
            "John Doe",
            1000.0,
            dueDate,
            "Business loan"
        );

        Debt savedDebt = Debt.builder()
            .id(UUID.randomUUID())
            .user(User.builder().id(userId).build())
            .counterpartyName("John Doe")
            .totalAmount(1000.0)
            .remainingAmount(1000.0)
            .dueDate(dueDate)
            .status(DebtStatus.OPEN)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(debtRepository.save(any(Debt.class))).thenReturn(savedDebt);

        // Act
        DebtResponse result = useCase.create(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.counterpartyName());
        assertEquals(1000.0, result.totalAmount());
        assertEquals(1000.0, result.remainingAmount());
        assertEquals(DebtStatus.OPEN, result.status());
        assertEquals(0.0, result.paidAmount());

        verify(debtRepository).save(any(Debt.class));
        verify(metricsService).incrementCounter(eq("debts.created.total"));
        verify(businessEventLogger).logBusinessEvent(eq("DEBT_CREATED"), anyString(), anyMap());
    }

    @Test
    void create_ShouldSetStatusToOpen_WhenCreated() {
        // Arrange
        CreateDebtRequest request = new CreateDebtRequest(
            "Jane Smith",
            500.0,
            null,
            null
        );

        ArgumentCaptor<Debt> debtCaptor = ArgumentCaptor.forClass(Debt.class);
        when(debtRepository.save(debtCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        useCase.create(userId, request);

        // Assert
        Debt savedDebt = debtCaptor.getValue();
        assertEquals(DebtStatus.OPEN, savedDebt.getStatus());
        assertEquals(savedDebt.getTotalAmount(), savedDebt.getRemainingAmount());
    }

    @Test
    void create_ShouldSetRemainingAmountEqualToTotalAmount() {
        // Arrange
        CreateDebtRequest request = new CreateDebtRequest(
            "Test User",
            750.0,
            null,
            null
        );

        ArgumentCaptor<Debt> debtCaptor = ArgumentCaptor.forClass(Debt.class);
        when(debtRepository.save(debtCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        useCase.create(userId, request);

        // Assert
        Debt savedDebt = debtCaptor.getValue();
        assertEquals(750.0, savedDebt.getTotalAmount());
        assertEquals(750.0, savedDebt.getRemainingAmount());
    }

    @Test
    void create_ShouldHandleNullDueDate() {
        // Arrange
        CreateDebtRequest request = new CreateDebtRequest(
            "Test User",
            1000.0,
            null,
            null
        );

        when(debtRepository.save(any(Debt.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        DebtResponse result = useCase.create(userId, request);

        // Assert
        assertNotNull(result);
        assertNull(result.dueDate());
    }

    @Test
    void create_ShouldRecordMetrics() {
        // Arrange
        CreateDebtRequest request = new CreateDebtRequest(
            "Test User",
            1000.0,
            null,
            null
        );

        when(debtRepository.save(any(Debt.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        useCase.create(userId, request);

        // Assert
        verify(metricsService).incrementCounter(eq("debts.created.total"));
        verify(metricsService).recordTimer(eq("debt.creation.duration"), anyLong());
    }
}
