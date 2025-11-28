package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.transaction.TransactionFilter;
import com.fajars.expensetracker.transaction.TransactionRepository;
import com.fajars.expensetracker.transaction.TransactionResponse;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FindAllTransactionsUseCase implements FindAllTransactions {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findByUserIdWithFilters(
        UUID userId, TransactionFilter filter) {
        log.debug("Finding transactions for user {} with filters: {}", userId, filter);

        // Create pageable with sorting by date descending
        Pageable pageable = PageRequest.of(
            filter.page(),
            filter.size(),
            Sort.by(Sort.Direction.DESC, "date")
        );

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (filter.from() != null) {
            fromDateTime = filter.from().atStartOfDay();
        }
        if (filter.to() != null) {
            toDateTime = filter.to()
                .atTime(LocalTime.MAX);
        }

        Page<TransactionResponse> result = transactionRepository.findByUserIdWithFilters(
            userId,
            filter.walletId(),
            filter.categoryId(),
            filter.type(),
            fromDateTime,
            toDateTime,
            pageable
        ).map(TransactionResponse::from);

        log.debug("Found {} transactions for user {}", result.getTotalElements(), userId);
        return result;
    }
}
