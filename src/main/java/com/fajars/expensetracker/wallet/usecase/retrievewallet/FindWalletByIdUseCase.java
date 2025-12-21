package com.fajars.expensetracker.wallet.usecase.retrievewallet;

import com.fajars.expensetracker.wallet.domain.Wallet;
import com.fajars.expensetracker.wallet.api.WalletResponse;
import com.fajars.expensetracker.wallet.domain.WalletRepository;
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
    public WalletResponse findByIdAndUserId(UUID walletId, UUID userId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));
        return WalletResponse.from(wallet);
    }
}
