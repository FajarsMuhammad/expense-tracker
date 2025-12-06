package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryRepository;
import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.transaction.CreateTransactionRequest;
import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.transaction.TransactionResponse;
import com.fajars.expensetracker.transaction.TransactionRepository;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTransactionUseCase implements CreateTransaction {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    @Override
    @Transactional
    public TransactionResponse create(UUID userId, CreateTransactionRequest request) {
        long startTime = System.currentTimeMillis();
        log.debug("Creating transaction for user {}: {}", userId, request);

        Wallet wallet = validateAndGetWallet(userId, request.walletId());
        Category category = validateAndGetCategory(userId, request.categoryId());

        Transaction transaction = buildTransaction(userId, request, wallet, category);
        transaction = transactionRepository.save(transaction);

        logBusinessEvent(transaction);
        recordMetrics(startTime);

        log.info("Transaction {} created successfully for user {}", transaction.getId(), userId);
        return TransactionResponse.from(transaction);
    }

    private Wallet validateAndGetWallet(UUID userId, UUID walletId) {
        return walletRepository.findByIdAndUserId(walletId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", walletId.toString()));
    }

    private Category validateAndGetCategory(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId.toString()));

        // Validate category belongs to user or is a default category
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Category", categoryId.toString());
        }

        return category;
    }

    private Transaction buildTransaction(
        UUID userId, CreateTransactionRequest request,
        Wallet wallet, Category category
    ) {
        // Use Jakarta timezone for audit timestamps
        LocalDateTime now = ZonedDateTime.now(JAKARTA_ZONE).toLocalDateTime();

        // Convert transaction date to Jakarta timezone
        // Request date comes from frontend, needs to be converted to Jakarta timezone
        LocalDateTime transactionDate = convertToJakartaTime(request.date());

        return Transaction.builder()
            .id(UUID.randomUUID())
            .user(User.builder().id(userId).build())
            .wallet(wallet)
            .category(category)
            .type(request.type())
            .amount(request.amount())
            .note(request.note())
            .date(transactionDate)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /**
     * Convert LocalDateTime to Jakarta timezone.
     * Assumes input is in UTC (from frontend's ISO format with Z suffix).
     */
    private LocalDateTime convertToJakartaTime(LocalDateTime utcDateTime) {
        // Treat the LocalDateTime as UTC and convert to Jakarta
        return utcDateTime.atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(JAKARTA_ZONE)
            .toLocalDateTime();
    }

    private void logBusinessEvent(Transaction transaction) {
        String username = getCurrentUsername();
        businessEventLogger.logTransactionCreated(
            transaction.getId().getMostSignificantBits(),
            username,
            transaction.getType().name(),
            transaction.getAmount()
        );
    }

    private void recordMetrics(long startTime) {
        metricsService.recordTransactionCreated();
        metricsService.recordTransactionCreationTime(startTime);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
