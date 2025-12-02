package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.common.exception.ResourceNotFoundException;
import com.fajars.expensetracker.debt.Debt;
import com.fajars.expensetracker.debt.DebtDetailResponse;
import com.fajars.expensetracker.debt.DebtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case implementation for getting detailed debt information.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetDebtDetailUseCase implements GetDebtDetail {

    private final DebtRepository debtRepository;

    @Override
    @Transactional(readOnly = true)
    public DebtDetailResponse getDetail(UUID userId, UUID debtId) {
        log.debug("Getting debt detail for debt {} and user {}", debtId, userId);

        Debt debt = debtRepository.findByIdAndUserId(debtId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Debt", debtId.toString()));

        log.debug("Found debt {} with {} payments", debtId, debt.getPayments().size());
        return DebtDetailResponse.from(debt);
    }
}
