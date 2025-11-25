package com.fajars.expensetracker.wallet;

import com.fajars.expensetracker.transaction.Transaction;

import java.util.Date;
import java.util.UUID;

public record WalletDto(
        UUID id,
        String name,
        Currency currency,
        Double initialBalance,
        Double currentBalance,
        Date createdAt,
        Date updatedAt
) {
    public static WalletDto from(Wallet wallet) {
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
