package com.fajars.expensetracker.payment.usecase;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.payment.domain.PaymentMethod;
import com.fajars.expensetracker.payment.domain.PaymentRepository;
import com.fajars.expensetracker.payment.domain.PaymentStatus;
import com.fajars.expensetracker.payment.domain.PaymentTransaction;
import com.fajars.expensetracker.payment.midtrans.MidtransWebhookPayload;
import com.fajars.expensetracker.payment.midtrans.WebhookVerifier;
import com.fajars.expensetracker.subscription.domain.SubscriptionRepository;
import com.fajars.expensetracker.subscription.usecase.activesubcription.ActivateSubscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case implementation for processing payment webhooks. Verifies signature, updates payment
 * status, and activates subscription.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentWebhookUseCase implements ProcessPaymentWebhook {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ActivateSubscription activateSubscription;
    private final WebhookVerifier webhookVerifier;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void processWebhook(MidtransWebhookPayload payload) {
        long startTime = System.currentTimeMillis();
        log.info("Processing webhook for order: {}", payload.orderId());

        try {
            // Verify signature
            if (!webhookVerifier.verifySignature(payload)) {
                log.warn("Invalid webhook signature for order: {}", payload.orderId());
                metricsService.incrementCounter("webhook.invalid_signature");
                throw BusinessException.forbidden("Invalid webhook signature");
            }

            // Find payment by order ID
            PaymentTransaction payment = paymentRepository.findByOrderId(payload.orderId())
                .orElseThrow(() -> BusinessException.notFound(
                    "Payment not found for order: " + payload.orderId()
                ));

            // Skip if payment already in final state
            if (payment.isFinalState() && payment.getStatus() != PaymentStatus.PENDING) {
                log.info("Payment {} already in final state: {}", payment.getId(),
                         payment.getStatus());
                return;
            }

            // Store webhook payload for audit
            payment.setWebhookPayload(convertPayloadToMap(payload));

            // Update payment based on transaction status
            if (payload.isSuccess()) {
                handleSuccessfulPayment(payment, payload);
            } else if (payload.isPending()) {
                handlePendingPayment(payment, payload);
            } else if (payload.isFailed()) {
                handleFailedPayment(payment, payload);
            }

            paymentRepository.save(payment);

            // Log and metrics
            logBusinessEvent(payment, payload);
            recordMetrics(startTime, "success", payload.transactionStatus());

            log.info("Webhook processed successfully for order: {}", payload.orderId());

        } catch (Exception e) {
            log.error("Failed to process webhook for order: {}", payload.orderId(), e);
            recordMetrics(startTime, "failed", "error");
            throw e;
        }
    }

    private void handleSuccessfulPayment(
        PaymentTransaction payment, MidtransWebhookPayload payload) {
        log.info("Processing successful payment: {}", payment.getOrderId());

        // Determine payment method
        PaymentMethod paymentMethod = mapPaymentType(payload.paymentType());

        // Mark payment as successful
        payment.markAsSuccess(payload.transactionId(), paymentMethod);

        // Activate or extend subscription
        try {
            activateSubscription.activateOrExtend(
                payment.getUser().getId(),
                payment.getId(),
                30 // 30 days for monthly subscription
            );
            log.info("Subscription activated for user: {}", payment.getUser().getId());
        } catch (Exception e) {
            log.error("Failed to activate subscription for payment: {}", payment.getId(), e);
            // Don't fail the webhook - we can retry subscription activation manually
            metricsService.incrementCounter("subscription.activation.failed");
        }
    }

    private void handlePendingPayment(PaymentTransaction payment, MidtransWebhookPayload payload) {
        log.info("Payment still pending: {}", payment.getOrderId());
        // Payment already in PENDING status, just update metadata
        payment.setTransactionId(payload.transactionId());
    }

    private void handleFailedPayment(PaymentTransaction payment, MidtransWebhookPayload payload) {
        log.info("Processing failed payment: {}", payment.getOrderId());

        String status = payload.transactionStatus();
        if ("expire".equalsIgnoreCase(status)) {
            payment.markAsExpired();
        } else if ("cancel".equalsIgnoreCase(status)) {
            payment.markAsCancelled();
        } else {
            payment.markAsFailed();
        }
    }

    private PaymentMethod mapPaymentType(String paymentType) {
        if (paymentType == null) {
            return PaymentMethod.OTHER;
        }

        return switch (paymentType.toLowerCase()) {
            case "credit_card" -> PaymentMethod.CREDIT_CARD;
            case "bank_transfer", "echannel" -> PaymentMethod.BANK_TRANSFER;
            case "gopay", "shopeepay", "qris" -> PaymentMethod.EWALLET;
            case "cstore" -> PaymentMethod.CONVENIENCE_STORE;
            case "kredivo" -> PaymentMethod.KREDIVO;
            case "akulaku" -> PaymentMethod.AKULAKU;
            default -> PaymentMethod.OTHER;
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertPayloadToMap(MidtransWebhookPayload payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("Failed to convert webhook payload to map", e);
            return Map.of(
                "order_id", payload.orderId(),
                "transaction_status", payload.transactionStatus(),
                "error", "Failed to parse full payload"
            );
        }
    }

    private void logBusinessEvent(PaymentTransaction payment, MidtransWebhookPayload payload) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("paymentId", payment.getId());
        attributes.put("orderId", payment.getOrderId());
        attributes.put("transactionId", payload.transactionId());
        attributes.put("status", payment.getStatus());
        attributes.put("transactionStatus", payload.transactionStatus());
        attributes.put("amount", payment.getAmount());

        businessEventLogger.logBusinessEvent(
            "PAYMENT_WEBHOOK_PROCESSED",
            payment.getUser().getEmail(),
            attributes
        );
    }

    private void recordMetrics(long startTime, String result, String transactionStatus) {
        metricsService.incrementCounter(
            "webhook.processed.total",
            "result", result,
            "transaction_status", transactionStatus
        );
        metricsService.recordTimer("webhook.processing.duration", startTime);
    }
}
