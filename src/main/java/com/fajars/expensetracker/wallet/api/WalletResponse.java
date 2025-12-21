package com.fajars.expensetracker.wallet.api;

import com.fajars.expensetracker.transaction.domain.Transaction;

import com.fajars.expensetracker.wallet.domain.Currency;
import com.fajars.expensetracker.wallet.domain.Wallet;
import java.util.Date;
import java.util.UUID;

public record WalletResponse(
    UUID id,
    String name,
    Currency currency,
    Double initialBalance,
    Double currentBalance,
    Date createdAt,
    Date updatedAt
) {

    public static WalletResponse from(Wallet wallet) {
        double currentBalance = wallet.getInitialBalance();
        if (wallet.getTransactions() != null) {
            for (Transaction transaction : wallet.getTransactions()) {
                if ("INCOME".equals(transaction.getType().name())) {
                    currentBalance += transaction.getAmount();
                } else if ("EXPENSE".equals(transaction.getType().name())) {
                    currentBalance -= transaction.getAmount();
                }
            }
        }

        return new WalletResponse(
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
