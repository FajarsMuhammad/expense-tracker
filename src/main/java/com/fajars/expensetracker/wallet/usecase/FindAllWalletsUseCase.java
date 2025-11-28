package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletResponse;
import com.fajars.expensetracker.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FindAllWalletsUseCase implements FindAllWallets {

    private final WalletRepository walletRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponse> findAllByUserId(UUID userId) {
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        return wallets.stream()
                .map(WalletResponse::from)
                .collect(Collectors.toList());
    }
}
