package com.fajars.expensetracker.payment.api;

import com.fajars.expensetracker.payment.midtrans.MidtransWebhookPayload;
import com.fajars.expensetracker.payment.usecase.createpayment.CreatePaymentCmd;
import com.fajars.expensetracker.payment.usecase.createpayment.CreateSubscriptionPayment;
import com.fajars.expensetracker.payment.usecase.ProcessPaymentWebhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for payment operations. Handles payment creation and webhook notifications.
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
     * Create subscription payment. Generates Midtrans Snap token and redirect URL for payment.
     * <p>
     * POST /payments/subscription
     * <p>
     * Request Headers: - Authorization: Bearer {JWT_TOKEN} - X-Idempotency-Key: {UNIQUE_KEY}
     * (optional)
     * <p>
     * Response: { "paymentId": "uuid", "orderId": "ORDER-12345678-1234567890", "amount": 25000.00,
     * "currency": "IDR", "status": "PENDING", "snapToken": "...", "snapRedirectUrl":
     * "https://app.sandbox.midtrans.com/snap/v3/..." }
     */
    @PostMapping("/subscription")
    @Operation(
        summary = "Create subscription payment",
        description = "Create a new payment transaction for premium subscription (IDR 25,000/month)",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<CreatePaymentResponse> createSubscriptionPayment(
        @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        log.info("Creating subscription payment for idempotencyKey: {}", idempotencyKey);

        CreatePaymentResponse response = createSubscriptionPayment
            .createPayment(new CreatePaymentCmd(idempotencyKey));

        return ResponseEntity.ok(response);
    }

    /**
     * Webhook endpoint for Midtrans payment notifications. This endpoint is called by Midtrans when
     * payment status changes.
     * <p>
     * POST /payments/webhook/midtrans
     * <p>
     * Security: No JWT auth (webhook from Midtrans) Validation: SHA-512 signature verification
     * <p>
     * Request Body (from Midtrans): { "transaction_status": "settlement", "status_code": "200",
     * "signature_key": "...", "order_id": "ORDER-12345678-1234567890", "transaction_id": "...",
     * "gross_amount": "25000.00", "payment_type": "credit_card", ... }
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
