package com.fajars.expensetracker.transaction.usecase.findtransactionbyid;

import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.transaction.domain.TransactionRepository;
import com.fajars.expensetracker.transaction.api.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FindTransactionByIdUseCase implements FindTransactionById {

    private final TransactionRepository transactionRepository;

    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse findByIdAndUserId(UUID transactionId) {
        UUID userId = currentUserProvider.getUserId();

        log.debug("Finding transaction {} for user {}", transactionId, userId);

        return transactionRepository.findByIdAndUserId(transactionId, userId)
            .map(TransactionResponse::from)
            .orElseThrow(
                () -> new ResourceNotFoundException("Transaction", transactionId.toString()));
    }
}
