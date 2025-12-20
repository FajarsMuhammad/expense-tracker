
package com.fajars.expensetracker.voice.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryRepository;
import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.transaction.TransactionRepository;
import com.fajars.expensetracker.transaction.TransactionType;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.voice.parse.ParseVoiceExpenseResult;
import com.fajars.expensetracker.voice.parse.ParsingConfidence;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateTransactionFromVoiceUseCase {

    private final TransactionRepository repository;

    private final WalletRepository walletRepository;

    private final CategoryRepository categoryRepository;

    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    public void execute(UUID userId, ParseVoiceExpenseResult result, String text) {
        if (result.confidence() != ParsingConfidence.HIGH) {
            throw new IllegalStateException("Confirmation required");
        }

        Wallet wallet = validateAndGetWallet(userId, result.walletId());

        Category category = validateAndGetCategory(userId, result.categoryId());

        LocalDateTime now = ZonedDateTime.now(JAKARTA_ZONE).toLocalDateTime();

        LocalDateTime transactionDate = convertToJakartaTime(result.date());

        Transaction transaction = Transaction.builder()
            .id(UUID.randomUUID())
            .user(User.builder().id(userId).build())
            .wallet(wallet)
            .category(category)
            .type(TransactionType.EXPENSE)
            .amount(result.amount())
            .note(text)
            .date(transactionDate)
            .createdAt(now)
            .updatedAt(now)
            .build();

        repository.save(transaction);
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

    private LocalDateTime convertToJakartaTime(LocalDateTime utcDateTime) {
        return utcDateTime.atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(JAKARTA_ZONE)
            .toLocalDateTime();
    }
}
