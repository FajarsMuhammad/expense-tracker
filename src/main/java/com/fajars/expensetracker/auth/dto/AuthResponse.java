package com.fajars.expensetracker.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Authentication response with user info, JWT token, and optional subscription/wallet details")
public record AuthResponse(
    @Schema(description = "JWT access token")
    String token,

    @Schema(description = "User ID")
    UUID userId,

    @Schema(description = "User email")
    String email,

    @Schema(description = "User name")
    String name,

    @Schema(description = "Subscription information (included in registration response)")
    SubscriptionInfo subscription,

    @Schema(description = "Default wallet information (included in registration response)")
    WalletInfo defaultWallet
) {
    // Backward-compatible constructor for login (no subscription/wallet info)
    public AuthResponse(String token, UUID userId, String email, String name) {
        this(token, userId, email, name, null, null);
    }
}
