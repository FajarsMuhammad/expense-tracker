package com.fajars.expensetracker.transaction.usecase.deletetransaction;

import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.transaction.domain.Transaction;
import com.fajars.expensetracker.transaction.domain.TransactionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteTransactionUseCase implements DeleteTransaction {

    private final TransactionRepository transactionRepository;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void delete(UUID transactionId) {
        UUID userId = currentUserProvider.getUserId();

        log.debug("Deleting transaction {} for user {}", transactionId, userId);

        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId.toString()));

        transactionRepository.delete(transaction);

        String username = currentUserProvider.getEmail();
        businessEventLogger.logTransactionDeleted(transaction.getId().getMostSignificantBits(), username);

        log.info("Transaction {} deleted successfully for user {}", transactionId, userId);
    }
}
