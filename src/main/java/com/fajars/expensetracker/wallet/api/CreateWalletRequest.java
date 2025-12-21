package com.fajars.expensetracker.wallet.api;

import com.fajars.expensetracker.wallet.domain.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateWalletRequest(
        @NotBlank(message = "Wallet name is required")
        String name,

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotNull(message = "Initial balance is required")
        @PositiveOrZero(message = "Initial balance must be greater than or equal to 0")
        Double initialBalance
) {}
