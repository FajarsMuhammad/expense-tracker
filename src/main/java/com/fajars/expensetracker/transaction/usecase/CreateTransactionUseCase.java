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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateTransactionUseCase implements CreateTransaction {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public TransactionDto create(UUID userId, CreateTransactionRequest request) {
        long startTime = System.currentTimeMillis();

        // Validate wallet ownership
        Wallet wallet = walletRepository.findByIdAndUserId(request.walletId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", request.walletId().toString()));

        // Validate category
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId().toString()));

        // Validate type
        if (!request.type().equals("INCOME") && !request.type().equals("EXPENSE")) {
            throw new IllegalArgumentException("Transaction type must be either INCOME or EXPENSE");
        }

        // Create transaction
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(userId).build())
                .wallet(wallet)
                .category(category)
                .type(request.type())
                .amount(request.amount())
                .note(request.note())
                .date(request.date())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        transaction = transactionRepository.save(transaction);

        // Log business event and metrics
        String username = getCurrentUsername();
        businessEventLogger.logTransactionCreated(
                transaction.getId().getMostSignificantBits(),
                username,
                transaction.getType(),
                transaction.getAmount()
        );
        metricsService.recordTransactionCreated();
        metricsService.recordTransactionCreationTime(startTime);

        return toDto(transaction);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private TransactionDto toDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getWallet().getId(),
                transaction.getWallet().getName(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getNote(),
                transaction.getDate(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
