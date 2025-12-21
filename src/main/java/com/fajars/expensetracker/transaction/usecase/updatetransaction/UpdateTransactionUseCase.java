package com.fajars.expensetracker.transaction.usecase.updatetransaction;

import com.fajars.expensetracker.category.domain.Category;
import com.fajars.expensetracker.category.domain.CategoryRepository;
import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.transaction.api.TransactionResponse;
import com.fajars.expensetracker.transaction.api.TransactionType;
import com.fajars.expensetracker.transaction.api.UpdateTransactionRequest;
import com.fajars.expensetracker.transaction.domain.Transaction;
import com.fajars.expensetracker.transaction.domain.TransactionRepository;
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
public class UpdateTransactionUseCase implements UpdateTransaction {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider currentUserProvider;

    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    @Override
    @Transactional
    public TransactionResponse update(UUID transactionId, UpdateTransactionRequest request) {
        UUID userId = currentUserProvider.getUserId();

        log.debug("Updating transaction {} for user {}: {}", transactionId, userId, request);

        Transaction transaction = validateAndGetTransaction(transactionId, userId);

        Wallet wallet = validateAndGetWallet(userId, request.walletId());
        Category category = validateAndGetCategory(userId, request.categoryId());

        TransactionSnapshot snapshot = captureSnapshot(transaction);
        updateTransactionFields(transaction, request, wallet, category);
        transaction = transactionRepository.save(transaction);

        logChanges(transaction, snapshot);

        log.info("Transaction {} updated successfully for user {}", transactionId, userId);
        return TransactionResponse.from(transaction);
    }

    private Transaction validateAndGetTransaction(UUID transactionId, UUID userId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId.toString()));
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
        // Convert transaction date to Jakarta timezone
        transaction.setDate(convertToJakartaTime(request.date()));
        // Use Jakarta timezone for audit timestamp
        transaction.setUpdatedAt(ZonedDateTime.now(JAKARTA_ZONE).toLocalDateTime());
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

    private void logChanges(Transaction transaction, TransactionSnapshot snapshot) {
        String username = currentUserProvider.getEmail();
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
                snapshot.type().name() + "/" + snapshot.amount(),
                transaction.getType().name() + "/" + transaction.getAmount()
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

    private record TransactionSnapshot(
            TransactionType type,
            Double amount,
            UUID walletId,
            UUID categoryId
    ) {
    }
}
