package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletDto;
import com.fajars.expensetracker.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindWalletByIdUseCase implements FindWalletById {

    private final WalletRepository walletRepository;

    @Override
    @Transactional(readOnly = true)
    public WalletDto findByIdAndUserId(UUID walletId, UUID userId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));
        return WalletDto.from(wallet);
    }
}
