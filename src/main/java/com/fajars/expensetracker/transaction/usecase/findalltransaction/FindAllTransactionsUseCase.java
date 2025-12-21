package com.fajars.expensetracker.transaction.usecase.findalltransaction;

import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.transaction.api.TransactionFilter;
import com.fajars.expensetracker.transaction.api.TransactionPageResponse;
import com.fajars.expensetracker.transaction.domain.TransactionRepository;
import com.fajars.expensetracker.transaction.api.TransactionResponse;
import com.fajars.expensetracker.transaction.projection.TransactionSummary;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FindAllTransactionsUseCase implements FindAllTransactions {

    private final TransactionRepository transactionRepository;

    private final CurrentUserProvider currentUserProvider;

    /**
     * Find transactions with totals (incomeTotal and expenseTotal).
     * This method runs two optimized queries:
     * 1. Paginated transaction list
     * 2. Aggregated totals (without fetching all entities)
     */
    @Transactional(readOnly = true)
    public TransactionPageResponse findWithFilters(TransactionFilter filter) {
        UUID userId = currentUserProvider.getUserId();

        log.debug("Finding transactions with totals for user {} with filters: {}", userId, filter);

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
            toDateTime = filter.to().atTime(LocalTime.MAX);
        }

        Page<TransactionResponse> transactionPage = transactionRepository.findByUserIdWithFilters(
            userId,
            filter.walletId(),
            filter.categoryId(),
            filter.type(),
            fromDateTime,
            toDateTime,
            pageable
        ).map(TransactionResponse::from);

        TransactionSummary summary = transactionRepository.getTotalsByFilters(
            userId,
            filter.walletId(),
            filter.categoryId(),
            filter.type(),
            fromDateTime,
            toDateTime
        );

        log.debug("Found {} transactions with incomeTotal={}, expenseTotal={} for user {}",
            transactionPage.getTotalElements(),
            summary.getTotalIncome(),
            summary.getTotalExpense(),
            userId);

        return TransactionPageResponse.from(
            transactionPage,
            summary.getTotalIncome(),
            summary.getTotalExpense()
        );
    }
}
