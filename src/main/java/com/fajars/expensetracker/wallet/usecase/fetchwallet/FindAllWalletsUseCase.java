package com.fajars.expensetracker.wallet.usecase.fetchwallet;

import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.wallet.domain.Wallet;
import com.fajars.expensetracker.wallet.api.WalletResponse;
import com.fajars.expensetracker.wallet.domain.WalletRepository;
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
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponse> findAllByUserId() {
        UUID userId = currentUserProvider.getUserId();
        List<Wallet> wallets = walletRepository.findByUserId(userId);

        return wallets.stream()
            .map(WalletResponse::from)
            .collect(Collectors.toList());
    }
}
