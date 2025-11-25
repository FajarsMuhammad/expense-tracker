package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryRepository;
import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.transaction.TransactionDto;
import com.fajars.expensetracker.transaction.TransactionRepository;
import com.fajars.expensetracker.transaction.UpdateTransactionRequest;
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
public class UpdateTransactionUseCase implements UpdateTransaction {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public TransactionDto update(UUID userId, UUID transactionId, UpdateTransactionRequest request) {
        // Find transaction and verify ownership
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId.toString()));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Transaction not found or access denied");
        }

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

        // Track changes for logging
        String oldType = transaction.getType();
        Double oldAmount = transaction.getAmount();
        UUID oldWalletId = transaction.getWallet().getId();
        UUID oldCategoryId = transaction.getCategory().getId();

        // Update transaction
        transaction.setWallet(wallet);
        transaction.setCategory(category);
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setNote(request.note());
        transaction.setDate(request.date());
        transaction.setUpdatedAt(new Date());

        transaction = transactionRepository.save(transaction);

        // Log business events for significant changes
        String username = getCurrentUsername();
        if (!oldType.equals(transaction.getType()) || !oldAmount.equals(transaction.getAmount())) {
            businessEventLogger.logTransactionUpdated(
                    transaction.getId().getMostSignificantBits(),
                    username,
                    "type/amount",
                    oldType + "/" + oldAmount,
                    transaction.getType() + "/" + transaction.getAmount()
            );
        }
        if (!oldWalletId.equals(transaction.getWallet().getId())) {
            businessEventLogger.logTransactionUpdated(
                    transaction.getId().getMostSignificantBits(),
                    username,
                    "wallet",
                    oldWalletId,
                    transaction.getWallet().getId()
            );
        }
        if (!oldCategoryId.equals(transaction.getCategory().getId())) {
            businessEventLogger.logTransactionUpdated(
                    transaction.getId().getMostSignificantBits(),
                    username,
                    "category",
                    oldCategoryId,
                    transaction.getCategory().getId()
            );
        }

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
