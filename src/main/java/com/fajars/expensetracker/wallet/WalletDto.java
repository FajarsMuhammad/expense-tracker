package com.fajars.expensetracker.wallet;

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
) {}
