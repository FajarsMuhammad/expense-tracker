package com.fajars.expensetracker.report;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Current wallet balance information.
 */
@Schema(description = "Wallet balance information")
public record WalletBalanceResponse(

    @Schema(description = "Wallet ID")
    UUID walletId,

    @Schema(description = "Wallet name", example = "Main Wallet")
    String walletName,

    @Schema(description = "Currency code", example = "IDR")
    String currency,

    @Schema(description = "Current balance", example = "5000000.00")
    Double currentBalance
) {
}
