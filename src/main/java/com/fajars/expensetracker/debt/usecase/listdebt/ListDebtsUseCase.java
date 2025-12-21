package com.fajars.expensetracker.debt.usecase.listdebt;

import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.debt.api.DebtFilter;
import com.fajars.expensetracker.debt.api.DebtResponse;
import com.fajars.expensetracker.debt.domain.Debt;
import com.fajars.expensetracker.debt.domain.DebtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use case implementation for listing debts with filters.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ListDebtsUseCase implements ListDebts {

    private final DebtRepository debtRepository;

    private final CurrentUserProvider userProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<DebtResponse> list(DebtFilter filter) {
        UUID userId = userProvider.getUserId();

        log.debug("Listing debts for user {} with filter: {}", userId, filter);

        Pageable pageable = PageRequest.of(filter.page(), filter.size());

        Page<Debt> debts;

        // Apply filters
        if (filter.overdue() != null && filter.overdue()) {
            debts = debtRepository.findOverdueDebts(userId, LocalDateTime.now(), pageable);
        } else if (filter.type() != null && filter.status() != null) {
            debts = debtRepository.findByUserIdAndTypeAndStatus(userId, filter.type(), filter.status(), pageable);
        } else if (filter.type() != null) {
            debts = debtRepository.findByUserIdAndType(userId, filter.type(), pageable);
        } else if (filter.status() != null) {
            debts = debtRepository.findByUserIdAndStatus(userId, filter.status(), pageable);
        } else {
            debts = debtRepository.findByUserId(userId, pageable);
        }

        log.debug("Found {} debts for user {}", debts.getTotalElements(), userId);
        return debts.map(DebtResponse::from);
    }
}
