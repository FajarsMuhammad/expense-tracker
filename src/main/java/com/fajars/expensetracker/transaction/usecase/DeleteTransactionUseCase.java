package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteTransactionUseCase implements DeleteTransaction {

    private final TransactionRepository transactionRepository;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public void delete(UUID userId, UUID transactionId) {
        log.debug("Deleting transaction {} for user {}", transactionId, userId);

        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId.toString()));

        transactionRepository.delete(transaction);

        String username = getCurrentUsername();
        businessEventLogger.logTransactionDeleted(transaction.getId().getMostSignificantBits(), username);

        log.info("Transaction {} deleted successfully for user {}", transactionId, userId);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
