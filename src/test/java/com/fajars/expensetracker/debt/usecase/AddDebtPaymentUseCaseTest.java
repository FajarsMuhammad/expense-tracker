package com.fajars.expensetracker.debt.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.debt.api.AddDebtPaymentRequest;
import com.fajars.expensetracker.debt.domain.Debt;
import com.fajars.expensetracker.debt.domain.DebtPayment;
import com.fajars.expensetracker.debt.domain.DebtPaymentRepository;
import com.fajars.expensetracker.debt.domain.DebtRepository;
import com.fajars.expensetracker.debt.domain.DebtStatus;
import com.fajars.expensetracker.debt.usecase.adddebt.AddDebtPayment;
import com.fajars.expensetracker.debt.usecase.adddebt.AddDebtPaymentUseCase;
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

@ExtendWith(MockitoExtension.class)
class AddDebtPaymentUseCaseTest {

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private DebtPaymentRepository debtPaymentRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private BusinessEventLogger businessEventLogger;

    @InjectMocks
    private AddDebtPaymentUseCase useCase;

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
            .remainingAmount(1000.0)
            .status(DebtStatus.OPEN)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void addPayment_ShouldAddPayment_WhenValidRequest() {
        // Arrange
        AddDebtPaymentRequest request = new AddDebtPaymentRequest(250.0, null, "First payment");

        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(debt));
        when(debtPaymentRepository.save(any(DebtPayment.class))).thenAnswer(
            i -> i.getArguments()[0]);
        when(debtRepository.save(any(Debt.class))).thenReturn(debt);

        // Act
        AddDebtPayment.AddDebtPaymentResult result = useCase.addPayment(debtId, request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.payment());
        assertNotNull(result.updatedDebt());
        assertEquals(250.0, result.payment().amount());
        assertEquals(750.0, result.updatedDebt().remainingAmount());
        assertEquals(DebtStatus.PARTIAL, result.updatedDebt().status());

        verify(debtRepository).findByIdAndUserId(debtId, userId);
        verify(debtPaymentRepository).save(any(DebtPayment.class));
        verify(debtRepository).save(debt);
    }

    @Test
    void addPayment_ShouldUpdateStatusToPaid_WhenFullyPaid() {
        // Arrange
        AddDebtPaymentRequest request = new AddDebtPaymentRequest(1000.0, null, "Full payment");

        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(debt));
        when(debtPaymentRepository.save(any(DebtPayment.class))).thenAnswer(
            i -> i.getArguments()[0]);
        when(debtRepository.save(any(Debt.class))).thenReturn(debt);

        // Act
        AddDebtPayment.AddDebtPaymentResult result = useCase.addPayment(debtId, request);

        // Assert
        assertEquals(0.0, result.updatedDebt().remainingAmount());
        assertEquals(DebtStatus.PAID, result.updatedDebt().status());
    }

    @Test
    void addPayment_ShouldThrowException_WhenDebtNotFound() {
        // Arrange
        AddDebtPaymentRequest request = new AddDebtPaymentRequest(250.0, null, null);
        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                     () -> useCase.addPayment(debtId, request));

        verify(debtPaymentRepository, never()).save(any());
        verify(debtRepository, never()).save(any());
    }

    @Test
    void addPayment_ShouldThrowException_WhenDebtAlreadyPaid() {
        // Arrange
        debt.setStatus(DebtStatus.PAID);
        debt.setRemainingAmount(0.0);

        AddDebtPaymentRequest request = new AddDebtPaymentRequest(100.0, null, null);
        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(debt));

        // Act & Assert
        assertThrows(IllegalStateException.class,
                     () -> useCase.addPayment(debtId, request));

        verify(debtPaymentRepository, never()).save(any());
    }

    @Test
    void addPayment_ShouldThrowException_WhenPaymentExceedsRemaining() {
        // Arrange
        AddDebtPaymentRequest request = new AddDebtPaymentRequest(1500.0, null, null);
        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(debt));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                     () -> useCase.addPayment(debtId, request));
    }

    @Test
    void addPayment_ShouldRecordMetrics() {
        // Arrange
        AddDebtPaymentRequest request = new AddDebtPaymentRequest(250.0, null, null);
        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(debt));
        when(debtPaymentRepository.save(any(DebtPayment.class))).thenAnswer(
            i -> i.getArguments()[0]);
        when(debtRepository.save(any(Debt.class))).thenReturn(debt);

        // Act
        useCase.addPayment(debtId, request);

        // Assert
        verify(metricsService).incrementCounter(eq("debt.payments.added.total"));
        verify(metricsService).recordTimer(eq("debt.payment.processing.duration"), anyLong());
    }

    @Test
    void addPayment_ShouldUseCurrentTime_WhenPaidAtNotProvided() {
        // Arrange
        AddDebtPaymentRequest request = new AddDebtPaymentRequest(250.0, null, null);
        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(debt));
        when(debtPaymentRepository.save(any(DebtPayment.class))).thenAnswer(
            i -> i.getArguments()[0]);
        when(debtRepository.save(any(Debt.class))).thenReturn(debt);

        // Act
        AddDebtPayment.AddDebtPaymentResult result = useCase.addPayment(debtId, request);

        // Assert
        assertNotNull(result.payment().paidAt());
    }

    @Test
    void addPayment_ShouldUseProvidedTime_WhenPaidAtProvided() {
        // Arrange
        LocalDateTime customTime = LocalDateTime.of(2025, 1, 1, 12, 0);
        AddDebtPaymentRequest request = new AddDebtPaymentRequest(250.0, customTime, null);

        when(debtRepository.findByIdAndUserId(debtId, userId)).thenReturn(Optional.of(debt));
        when(debtPaymentRepository.save(any(DebtPayment.class))).thenAnswer(
            i -> i.getArguments()[0]);
        when(debtRepository.save(any(Debt.class))).thenReturn(debt);

        // Act
        AddDebtPayment.AddDebtPaymentResult result = useCase.addPayment(debtId, request);

        // Assert
        assertEquals(customTime, result.payment().paidAt());
    }
}
