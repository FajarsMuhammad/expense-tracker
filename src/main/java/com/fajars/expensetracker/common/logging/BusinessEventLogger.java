package com.fajars.expensetracker.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized logger for business events
 * Provides structured logging for important business operations
 */
@Component
@Slf4j
public class BusinessEventLogger {

    // User events
    public void logUserRegistration(String email, String ipAddress) {
        log.info("Business Event: USER_REGISTERED | email={} | ip={}",
                SensitiveDataFilter.maskEmail(email), ipAddress);
    }

    public void logLoginSuccess(String username, String ipAddress, String userAgent) {
        log.info("Business Event: LOGIN_SUCCESS | user={} | ip={} | agent={}",
                username, ipAddress, userAgent);
    }

    public void logLoginFailure(String username, String ipAddress, String reason) {
        log.warn("Business Event: LOGIN_FAILED | user={} | ip={} | reason={}",
                username, ipAddress, reason);
    }

    public void logLogout(String username) {
        log.info("Business Event: LOGOUT | user={}", username);
    }

    // Transaction events
    public void logTransactionCreated(Long transactionId, String username, String type, Double amount) {
        log.info("Business Event: TRANSACTION_CREATED | id={} | user={} | type={} | amount={}",
                transactionId, username, type, amount);
    }

    public void logTransactionUpdated(Long transactionId, String username, String field, Object oldValue, Object newValue) {
        log.info("Business Event: TRANSACTION_UPDATED | id={} | user={} | field={} | old={} | new={}",
                transactionId, username, field, oldValue, newValue);
    }

    public void logTransactionDeleted(Long transactionId, String username) {
        log.info("Business Event: TRANSACTION_DELETED | id={} | user={}",
                transactionId, username);
    }

    // Wallet events
    public void logWalletCreated(Long walletId, String username, String walletName) {
        log.info("Business Event: WALLET_CREATED | id={} | user={} | name={}",
                walletId, username, walletName);
    }

    public void logWalletUpdated(Long walletId, String username, String field, Object oldValue, Object newValue) {
        log.info("Business Event: WALLET_UPDATED | id={} | user={} | field={} | old={} | new={}",
                walletId, username, field, oldValue, newValue);
    }

    public void logWalletDeleted(Long walletId, String username) {
        log.info("Business Event: WALLET_DELETED | id={} | user={}",
                walletId, username);
    }

    // Category events
    public void logCategoryCreated(Long categoryId, String username, String categoryName) {
        log.info("Business Event: CATEGORY_CREATED | id={} | user={} | name={}",
                categoryId, username, categoryName);
    }

    public void logCategoryUpdated(Long categoryId, String username, String field, Object oldValue, Object newValue) {
        log.info("Business Event: CATEGORY_UPDATED | id={} | user={} | field={} | old={} | new={}",
                categoryId, username, field, oldValue, newValue);
    }

    public void logCategoryDeleted(Long categoryId, String username) {
        log.info("Business Event: CATEGORY_DELETED | id={} | user={}",
                categoryId, username);
    }

    // Subscription events
    public void logSubscriptionCreated(Long subscriptionId, String username, String planType) {
        log.info("Business Event: SUBSCRIPTION_CREATED | id={} | user={} | plan={}",
                subscriptionId, username, planType);
    }

    public void logSubscriptionChanged(Long subscriptionId, String username, String oldPlan, String newPlan) {
        log.info("Business Event: SUBSCRIPTION_CHANGED | id={} | user={} | old_plan={} | new_plan={}",
                subscriptionId, username, oldPlan, newPlan);
    }

    public void logSubscriptionCancelled(Long subscriptionId, String username) {
        log.info("Business Event: SUBSCRIPTION_CANCELLED | id={} | user={}",
                subscriptionId, username);
    }

    // Payment events
    public void logPaymentInitiated(String paymentId, String username, Double amount, String currency) {
        log.info("Business Event: PAYMENT_INITIATED | payment_id={} | user={} | amount={} | currency={}",
                paymentId, username, amount, currency);
    }

    public void logPaymentSuccess(String paymentId, String username, Double amount) {
        log.info("Business Event: PAYMENT_SUCCESS | payment_id={} | user={} | amount={}",
                paymentId, username, amount);
    }

    public void logPaymentFailed(String paymentId, String username, String reason) {
        log.warn("Business Event: PAYMENT_FAILED | payment_id={} | user={} | reason={}",
                paymentId, username, reason);
    }

    // Export events
    public void logExportGenerated(String exportType, String username, int recordCount) {
        log.info("Business Event: EXPORT_GENERATED | type={} | user={} | records={}",
                exportType, username, recordCount);
    }

    // Security events
    public void logPasswordChanged(String username, String ipAddress) {
        log.info("Business Event: PASSWORD_CHANGED | user={} | ip={}",
                username, ipAddress);
    }

    public void logEmailChanged(String username, String oldEmail, String newEmail) {
        log.info("Business Event: EMAIL_CHANGED | user={} | old={} | new={}",
                username,
                SensitiveDataFilter.maskEmail(oldEmail),
                SensitiveDataFilter.maskEmail(newEmail));
    }

    public void logAccountDeleted(String username, String reason) {
        log.warn("Business Event: ACCOUNT_DELETED | user={} | reason={}",
                username, reason);
    }

    // Suspicious activity
    public void logSuspiciousActivity(String activityType, String username, String ipAddress, Map<String, Object> details) {
        log.warn("Business Event: SUSPICIOUS_ACTIVITY | type={} | user={} | ip={} | details={}",
                activityType, username, ipAddress, details);
    }

    public void logMultipleFailedAttempts(String username, String ipAddress, int attemptCount) {
        Map<String, Object> details = new HashMap<>();
        details.put("attemptCount", attemptCount);
        logSuspiciousActivity("MULTIPLE_FAILED_LOGIN", username, ipAddress, details);
    }

    // Generic business event
    public void logBusinessEvent(String eventType, String username, Map<String, Object> attributes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Business Event: ").append(eventType);
        sb.append(" | user=").append(username);

        if (attributes != null && !attributes.isEmpty()) {
            attributes.forEach((key, value) ->
                sb.append(" | ").append(key).append("=").append(value)
            );
        }

        log.info(sb.toString());
    }
}
