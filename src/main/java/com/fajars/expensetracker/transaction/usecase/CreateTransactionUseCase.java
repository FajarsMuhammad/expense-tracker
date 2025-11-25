package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryRepository;
import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.transaction.CreateTransactionRequest;
import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.transaction.TransactionDto;
import com.fajars.expensetracker.transaction.TransactionRepository;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateTransactionUseCase implements CreateTransaction {

    private static final Set<String> VALID_TRANSACTION_TYPES = Set.of("INCOME", "EXPENSE");

    private final TransactionRepository transactionRepository;

    private final WalletRepository walletRepository;

    private final CategoryRepository categoryRepository;

    private final MetricsService metricsService;

    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public TransactionDto create(UUID userId, CreateTransactionRequest request) {
        long startTime = System.currentTimeMillis();

        validateTransactionType(request.type());
        Wallet wallet = validateAndGetWallet(userId, request.walletId());
        Category category = validateAndGetCategory(request.categoryId());

        Transaction transaction = buildTransaction(userId, request, wallet, category);
        transaction = transactionRepository.save(transaction);

        logBusinessEvent(transaction);
        recordMetrics(startTime);

        return TransactionDto.from(transaction);
    }

    private void validateTransactionType(String type) {
        if (!VALID_TRANSACTION_TYPES.contains(type)) {
            throw new IllegalArgumentException(
                "Transaction type must be either INCOME or EXPENSE");
        }
    }

    private Wallet validateAndGetWallet(UUID userId, UUID walletId) {
        return walletRepository.findByIdAndUserId(walletId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", walletId.toString()));
    }

    private Category validateAndGetCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId.toString()));
    }

    private Transaction buildTransaction(
        UUID userId, CreateTransactionRequest request,
        Wallet wallet, Category category
    ) {
        Date now = new Date();
        return Transaction.builder()
            .id(UUID.randomUUID())
            .user(User.builder().id(userId).build())
            .wallet(wallet)
            .category(category)
            .type(request.type())
            .amount(request.amount())
            .note(request.note())
            .date(request.date())
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private void logBusinessEvent(Transaction transaction) {
        String username = getCurrentUsername();
        businessEventLogger.logTransactionCreated(
            transaction.getId().getMostSignificantBits(),
            username,
            transaction.getType(),
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
