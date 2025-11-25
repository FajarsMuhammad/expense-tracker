package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletDto;
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
    public List<WalletDto> findAllByUserId(UUID userId) {
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        return wallets.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private WalletDto toDto(Wallet wallet) {
        double currentBalance = wallet.getInitialBalance();
        if (wallet.getTransactions() != null) {
            for (Transaction transaction : wallet.getTransactions()) {
                if ("INCOME".equals(transaction.getType())) {
                    currentBalance += transaction.getAmount();
                } else if ("EXPENSE".equals(transaction.getType())) {
                    currentBalance -= transaction.getAmount();
                }
            }
        }

        return new WalletDto(
                wallet.getId(),
                wallet.getName(),
                wallet.getCurrency(),
                wallet.getInitialBalance(),
                currentBalance,
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}
