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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateTransactionUseCase implements UpdateTransaction {

    private static final Set<String> VALID_TRANSACTION_TYPES = Set.of("INCOME", "EXPENSE");

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public TransactionDto update(UUID userId, UUID transactionId, UpdateTransactionRequest request) {
        Transaction transaction = validateAndGetTransaction(transactionId, userId);

        validateTransactionType(request.type());
        Wallet wallet = validateAndGetWallet(userId, request.walletId());
        Category category = validateAndGetCategory(request.categoryId());

        TransactionSnapshot snapshot = captureSnapshot(transaction);
        updateTransactionFields(transaction, request, wallet, category);
        transaction = transactionRepository.save(transaction);

        logChanges(transaction, snapshot);

        return TransactionDto.from(transaction);
    }

    private Transaction validateAndGetTransaction(UUID transactionId, UUID userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId.toString()));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Transaction not found or access denied");
        }

        return transaction;
    }

    private void validateTransactionType(String type) {
        if (!VALID_TRANSACTION_TYPES.contains(type)) {
            throw new IllegalArgumentException("Transaction type must be either INCOME or EXPENSE");
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

    private TransactionSnapshot captureSnapshot(Transaction transaction) {
        return new TransactionSnapshot(
                transaction.getType(),
                transaction.getAmount(),
                transaction.getWallet().getId(),
                transaction.getCategory().getId()
        );
    }

    private void updateTransactionFields(
            Transaction transaction,
            UpdateTransactionRequest request,
            Wallet wallet,
            Category category
    ) {
        transaction.setWallet(wallet);
        transaction.setCategory(category);
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setNote(request.note());
        transaction.setDate(request.date());
        transaction.setUpdatedAt(new Date());
    }

    private void logChanges(Transaction transaction, TransactionSnapshot snapshot) {
        String username = getCurrentUsername();
        long transactionIdBits = transaction.getId().getMostSignificantBits();

        if (hasTypeOrAmountChanged(transaction, snapshot)) {
            logTypeOrAmountChange(transactionIdBits, username, snapshot, transaction);
        }

        if (hasWalletChanged(transaction, snapshot)) {
            logWalletChange(transactionIdBits, username, snapshot, transaction);
        }

        if (hasCategoryChanged(transaction, snapshot)) {
            logCategoryChange(transactionIdBits, username, snapshot, transaction);
        }
    }

    private boolean hasTypeOrAmountChanged(Transaction transaction, TransactionSnapshot snapshot) {
        return !snapshot.type().equals(transaction.getType()) ||
                !snapshot.amount().equals(transaction.getAmount());
    }

    private boolean hasWalletChanged(Transaction transaction, TransactionSnapshot snapshot) {
        return !snapshot.walletId().equals(transaction.getWallet().getId());
    }

    private boolean hasCategoryChanged(Transaction transaction, TransactionSnapshot snapshot) {
        return !snapshot.categoryId().equals(transaction.getCategory().getId());
    }

    private void logTypeOrAmountChange(
            long transactionId,
            String username,
            TransactionSnapshot snapshot,
            Transaction transaction
    ) {
        businessEventLogger.logTransactionUpdated(
                transactionId,
                username,
                "type/amount",
                snapshot.type() + "/" + snapshot.amount(),
                transaction.getType() + "/" + transaction.getAmount()
        );
    }

    private void logWalletChange(
            long transactionId,
            String username,
            TransactionSnapshot snapshot,
            Transaction transaction
    ) {
        businessEventLogger.logTransactionUpdated(
                transactionId,
                username,
                "wallet",
                snapshot.walletId(),
                transaction.getWallet().getId()
        );
    }

    private void logCategoryChange(
            long transactionId,
            String username,
            TransactionSnapshot snapshot,
            Transaction transaction
    ) {
        businessEventLogger.logTransactionUpdated(
                transactionId,
                username,
                "category",
                snapshot.categoryId(),
                transaction.getCategory().getId()
        );
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private record TransactionSnapshot(
            String type,
            Double amount,
            UUID walletId,
            UUID categoryId
    ) {
    }
}
