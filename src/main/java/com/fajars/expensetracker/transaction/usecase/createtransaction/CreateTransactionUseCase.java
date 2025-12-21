package com.fajars.expensetracker.transaction.usecase.createtransaction;

import com.fajars.expensetracker.category.domain.Category;
import com.fajars.expensetracker.category.domain.CategoryRepository;
import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.transaction.api.CreateTransactionRequest;
import com.fajars.expensetracker.transaction.api.TransactionResponse;
import com.fajars.expensetracker.transaction.domain.Transaction;
import com.fajars.expensetracker.transaction.domain.TransactionRepository;
import com.fajars.expensetracker.user.domain.User;
import com.fajars.expensetracker.wallet.domain.Wallet;
import com.fajars.expensetracker.wallet.domain.WalletRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTransactionUseCase implements CreateTransaction {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider currentUserProvider;

    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    @Override
    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        long startTime = System.currentTimeMillis();
        UUID userId = currentUserProvider.getUserId();
        log.debug("Creating transaction for user {}: {}", userId, request);

        Wallet wallet = validateAndGetWallet(userId, request.walletId());
        Category category = validateAndGetCategory(userId, request.categoryId());

        Transaction transaction = buildTransaction(userId, request, wallet, category);
        transaction = transactionRepository.save(transaction);

        // Build response BEFORE transaction commit to avoid lazy loading issues
        // Wallet and category are already loaded in this transaction context
        TransactionResponse response = TransactionResponse.from(transaction);

        logBusinessEvent(transaction);
        recordMetrics(startTime);

        log.info("Transaction {} created successfully for user {}", transaction.getId(), userId);
        return response;
    }

    private Wallet validateAndGetWallet(UUID userId, UUID walletId) {
        return walletRepository.findByIdAndUserId(walletId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", walletId.toString()));
    }

    private Category validateAndGetCategory(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId.toString()));

        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Category", categoryId.toString());
        }

        return category;
    }

    private Transaction buildTransaction(
        UUID userId, CreateTransactionRequest request,
        Wallet wallet, Category category
    ) {
        LocalDateTime now = ZonedDateTime.now(JAKARTA_ZONE).toLocalDateTime();
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
     * Convert LocalDateTime to Jakarta timezone. Assumes input is in UTC (from frontend's ISO
     * format with Z suffix).
     */
    private LocalDateTime convertToJakartaTime(LocalDateTime utcDateTime) {
        return utcDateTime.atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(JAKARTA_ZONE)
            .toLocalDateTime();
    }

    private void logBusinessEvent(Transaction transaction) {
        String username = currentUserProvider.getEmail();
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
}
