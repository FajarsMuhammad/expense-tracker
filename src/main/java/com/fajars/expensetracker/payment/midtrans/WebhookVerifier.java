package com.fajars.expensetracker.payment.midtrans;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Webhook signature verifier for Midtrans notifications.
 * Validates webhook authenticity using SHA-512 signature.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookVerifier {

    private final MidtransConfig midtransConfig;

    /**
     * Verify webhook signature using SHA-512.
     *
     * Signature formula: SHA512(order_id + status_code + gross_amount + server_key)
     *
     * @param payload the webhook payload
     * @return true if signature is valid
     */
    public boolean verifySignature(MidtransWebhookPayload payload) {
        try {
            String expectedSignature = generateSignature(
                payload.orderId(),
                payload.statusCode(),
                payload.grossAmount().toString(),
                midtransConfig.getServerKey()
            );

            boolean isValid = expectedSignature.equals(payload.signatureKey());

            if (!isValid) {
                log.warn("Invalid webhook signature for order: {}", payload.orderId());
            }

            return isValid;
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to verify webhook signature", e);
            return false;
        }
    }

    /**
     * Generate SHA-512 signature for webhook verification.
     *
     * @param orderId     the order ID
     * @param statusCode  the status code
     * @param grossAmount the gross amount
     * @param serverKey   the Midtrans server key
     * @return SHA-512 hash as hex string
     * @throws NoSuchAlgorithmException if SHA-512 is not available
     */
    private String generateSignature(
        String orderId,
        String statusCode,
        String grossAmount,
        String serverKey
    ) throws NoSuchAlgorithmException {
        String signatureString = orderId + statusCode + grossAmount + serverKey;

        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] hashBytes = digest.digest(signatureString.getBytes(StandardCharsets.UTF_8));

        return bytesToHex(hashBytes);
    }

    /**
     * Convert byte array to hexadecimal string.
     *
     * @param bytes the byte array
     * @return hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
