package com.fajars.expensetracker.auth;

import com.fajars.expensetracker.wallet.Currency;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Default wallet information included in authentication response")
public record WalletInfo(
    @Schema(description = "Wallet ID")
    UUID id,

    @Schema(description = "Wallet name")
    String name,

    @Schema(description = "Wallet currency")
    Currency currency,

    @Schema(description = "Initial balance")
    Double initialBalance
) {}
