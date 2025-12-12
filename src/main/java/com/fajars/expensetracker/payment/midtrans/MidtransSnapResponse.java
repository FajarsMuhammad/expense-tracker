package com.fajars.expensetracker.payment.midtrans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO from Midtrans Snap API.
 * Contains payment token and redirect URL.
 */
public record MidtransSnapResponse(
    @JsonProperty("token")
    String token,

    @JsonProperty("redirect_url")
    String redirectUrl
) {}
