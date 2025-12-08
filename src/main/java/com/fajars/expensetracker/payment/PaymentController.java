package com.fajars.expensetracker.payment;

import com.fajars.expensetracker.payment.midtrans.MidtransWebhookPayload;
import com.fajars.expensetracker.payment.usecase.CreatePaymentResponse;
import com.fajars.expensetracker.payment.usecase.CreateSubscriptionPayment;
import com.fajars.expensetracker.payment.usecase.ProcessPaymentWebhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for payment operations.
 * Handles payment creation and webhook notifications.
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Payment and subscription endpoints")
public class PaymentController {

    private final CreateSubscriptionPayment createSubscriptionPayment;
    private final ProcessPaymentWebhook processPaymentWebhook;

    /**
     * Create subscription payment.
     * Generates Midtrans Snap token and redirect URL for payment.
     *
     * POST /payments/subscription
     *
     * Request Headers:
     * - Authorization: Bearer {JWT_TOKEN}
     * - X-Idempotency-Key: {UNIQUE_KEY} (optional)
     *
     * Response:
     * {
     *   "paymentId": "uuid",
     *   "orderId": "ORDER-12345678-1234567890",
     *   "amount": 25000.00,
     *   "currency": "IDR",
     *   "status": "PENDING",
     *   "snapToken": "...",
     *   "snapRedirectUrl": "https://app.sandbox.midtrans.com/snap/v3/..."
     * }
     */
    @PostMapping("/subscription")
    @Operation(
        summary = "Create subscription payment",
        description = "Create a new payment transaction for premium subscription (IDR 25,000/month)",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<CreatePaymentResponse> createSubscriptionPayment(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        log.info("Creating subscription payment for user: {}", userId);

        CreatePaymentResponse response = createSubscriptionPayment.createPayment(userId, idempotencyKey);

        return ResponseEntity.ok(response);
    }

    /**
     * Webhook endpoint for Midtrans payment notifications.
     * This endpoint is called by Midtrans when payment status changes.
     *
     * POST /payments/webhook/midtrans
     *
     * Security: No JWT auth (webhook from Midtrans)
     * Validation: SHA-512 signature verification
     *
     * Request Body (from Midtrans):
     * {
     *   "transaction_status": "settlement",
     *   "status_code": "200",
     *   "signature_key": "...",
     *   "order_id": "ORDER-12345678-1234567890",
     *   "transaction_id": "...",
     *   "gross_amount": "25000.00",
     *   "payment_type": "credit_card",
     *   ...
     * }
     */
    @PostMapping("/webhook/midtrans")
    @Operation(
        summary = "Midtrans webhook",
        description = "Webhook endpoint for Midtrans payment notifications (public endpoint, no auth required)"
    )
    public ResponseEntity<Void> handleMidtransWebhook(
        @RequestBody MidtransWebhookPayload payload
    ) {
        log.info("Received Midtrans webhook for order: {}", payload.orderId());

        processPaymentWebhook.processWebhook(payload);

        return ResponseEntity.ok().build();
    }
}
