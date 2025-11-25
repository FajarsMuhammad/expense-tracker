package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteTransactionUseCase implements DeleteTransaction {

    private final TransactionRepository transactionRepository;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public void delete(UUID userId, UUID transactionId) {
        // Find transaction and verify ownership
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId.toString()));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Transaction not found or access denied");
        }

        transactionRepository.delete(transaction);

        // Log business event
        String username = getCurrentUsername();
        businessEventLogger.logTransactionDeleted(transaction.getId().getMostSignificantBits(), username);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
