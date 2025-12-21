package com.fajars.expensetracker.payment.usecase.createpayment;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.payment.api.CreatePaymentResponse;
import com.fajars.expensetracker.payment.domain.PaymentProvider;
import com.fajars.expensetracker.payment.domain.PaymentRepository;
import com.fajars.expensetracker.payment.domain.PaymentStatus;
import com.fajars.expensetracker.payment.domain.PaymentTransaction;
import com.fajars.expensetracker.payment.midtrans.MidtransClient;
import com.fajars.expensetracker.payment.midtrans.MidtransSnapRequest;
import com.fajars.expensetracker.payment.midtrans.MidtransSnapResponse;
import com.fajars.expensetracker.user.domain.User;
import com.fajars.expensetracker.user.domain.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case implementation for creating subscription payment. Handles payment creation with Midtrans
 * Snap API integration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateSubscriptionPaymentUseCase implements CreateSubscriptionPayment {

    private static final BigDecimal MONTHLY_PRICE = new BigDecimal("25000.00");
    private static final String CURRENCY = "IDR";
    private static final String PRODUCT_NAME = "Premium Subscription - 1 Month";
    private static final String PRODUCT_ID = "PREMIUM_MONTHLY";

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final MidtransClient midtransClient;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentCmd cmd) {
        long startTime = System.currentTimeMillis();
        UUID userId = currentUserProvider.getUserId();
        log.info("Creating subscription payment for user: {}", userId);

        // Check for duplicate payment using idempotency key
        if (cmd.idempotencyKey() != null) {
            var existing = paymentRepository.findByIdempotencyKey(cmd.idempotencyKey());

            if (existing.isPresent()) {
                log.info("Payment already exists for idempotency key: {}", cmd.idempotencyKey());
                return CreatePaymentResponse.from(existing.get());
            }
        }

        // Validate user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("User not found"));

        // Create order ID
        String orderId = generateOrderId(userId);

        // Create payment transaction entity
        PaymentTransaction payment = buildPaymentTransaction(user, orderId, cmd.idempotencyKey());
        payment = paymentRepository.save(payment);

        // Call Midtrans Snap API
        try {
            MidtransSnapRequest snapRequest = buildSnapRequest(user, orderId, MONTHLY_PRICE);
            MidtransSnapResponse snapResponse = midtransClient.createTransaction(snapRequest);

            // Update payment with Snap token and URL
            payment.setSnapToken(snapResponse.token());
            payment.setSnapRedirectUrl(snapResponse.redirectUrl());
            payment = paymentRepository.save(payment);

            // Log and metrics
            logBusinessEvent(payment, user);
            recordMetrics(startTime, "success");

            log.info("Payment {} created successfully for user {}", payment.getId(), userId);
            return CreatePaymentResponse.from(payment);

        } catch (Exception e) {
            log.error("Failed to create Midtrans transaction for user {}", userId, e);

            // Mark payment as failed
            payment.markAsFailed();
            paymentRepository.save(payment);

            recordMetrics(startTime, "failed");
            throw e;
        }
    }

    private PaymentTransaction buildPaymentTransaction(
        User user,
        String orderId,
        String idempotencyKey
    ) {
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("product", PRODUCT_NAME);
        metadata.put("product_id", PRODUCT_ID);
        metadata.put("created_from", "web");

        return PaymentTransaction.builder()
            .id(UUID.randomUUID())
            .user(user)
            .orderId(orderId)
            .amount(MONTHLY_PRICE)
            .currency(CURRENCY)
            .status(PaymentStatus.PENDING)
            .provider(PaymentProvider.MIDTRANS)
            .idempotencyKey(idempotencyKey)
            .metadata(metadata)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private MidtransSnapRequest buildSnapRequest(
        User user,
        String orderId,
        BigDecimal amount
    ) {
        // Transaction details
        MidtransSnapRequest.TransactionDetails transactionDetails =
            MidtransSnapRequest.TransactionDetails.builder()
                .orderId(orderId)
                .grossAmount(amount)
                .build();

        // Customer details
        MidtransSnapRequest.CustomerDetails customerDetails =
            MidtransSnapRequest.CustomerDetails.builder()
                .firstName(user.getName())
                .lastName("")
                .email(user.getEmail())
                .phone("") // Phone field is optional in Midtrans
                .build();

        // Item details
        List<MidtransSnapRequest.ItemDetails> itemDetails = List.of(
            MidtransSnapRequest.ItemDetails.builder()
                .id(PRODUCT_ID)
                .price(amount)
                .quantity(1)
                .name(PRODUCT_NAME)
                .build()
        );

        // Enabled payment methods (all available methods)
        List<String> enabledPayments = Arrays.asList(
            "credit_card", "bca_va", "bni_va", "bri_va", "mandiri_va",
            "permata_va", "other_va", "gopay", "shopeepay", "qris",
            "cimb_clicks", "bca_klikbca", "bca_klikpay", "bri_epay",
            "echannel", "mandiri_clickpay", "indomaret", "alfamart",
            "akulaku", "kredivo"
        );

        // Expiry: 24 hours
        MidtransSnapRequest.Expiry expiry = MidtransSnapRequest.Expiry.builder()
            .unit("hours")
            .duration(24)
            .build();

        return MidtransSnapRequest.builder()
            .transactionDetails(transactionDetails)
            .customerDetails(customerDetails)
            .itemDetails(itemDetails)
            .enabledPayments(enabledPayments)
            .expiry(expiry)
            .build();
    }

    private String generateOrderId(UUID userId) {
        long timestamp = System.currentTimeMillis();
        return String.format("ORDER-%s-%d", userId.toString().substring(0, 8), timestamp);
    }

    private void logBusinessEvent(PaymentTransaction payment, User user) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("paymentId", payment.getId());
        attributes.put("orderId", payment.getOrderId());
        attributes.put("amount", payment.getAmount());
        attributes.put("currency", payment.getCurrency());
        attributes.put("provider", payment.getProvider());
        businessEventLogger.logBusinessEvent(
            "PAYMENT_CREATED",
            user.getEmail(),
            attributes
        );
    }

    private void recordMetrics(long startTime, String result) {
        metricsService.incrementCounter("payment.created.total", "result", result);
        metricsService.recordTimer("payment.creation.duration", startTime);
    }
}
