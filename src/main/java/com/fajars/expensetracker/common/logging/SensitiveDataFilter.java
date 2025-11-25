package com.fajars.expensetracker.common.logging;

import java.util.regex.Pattern;

/**
 * Utility class to mask sensitive data in logs
 */
public class SensitiveDataFilter {

    // Regex patterns for sensitive data
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "(password|pwd|passwd)\\s*[:=]\\s*['\"]?([^'\"\\s,}]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})\\b"
    );

    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "\\b(?:\\d{4}[\\s-]?){3}\\d{4}\\b"
    );

    private static final Pattern JWT_TOKEN_PATTERN = Pattern.compile(
            "\\beyJ[a-zA-Z0-9_-]*\\.[a-zA-Z0-9_-]*\\.[a-zA-Z0-9_-]*\\b"
    );

    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
            "(Bearer|Token)\\s+([a-zA-Z0-9_\\-\\.]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern API_KEY_PATTERN = Pattern.compile(
            "(api[_-]?key|apikey|access[_-]?key)\\s*[:=]\\s*['\"]?([^'\"\\s,}]+)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Mask all sensitive data in the input string
     */
    public static String maskSensitiveData(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;

        // Mask passwords
        result = PASSWORD_PATTERN.matcher(result).replaceAll("$1=***");

        // Mask email addresses (show first 2 chars + domain)
        result = EMAIL_PATTERN.matcher(result).replaceAll(matchResult -> {
            String localPart = matchResult.group(1);
            String domain = matchResult.group(2);
            String maskedLocal = localPart.length() > 2
                    ? localPart.substring(0, 2) + "***"
                    : "***";
            return maskedLocal + "@" + domain;
        });

        // Mask credit card numbers (show last 4 digits)
        result = CREDIT_CARD_PATTERN.matcher(result).replaceAll(matchResult -> {
            String card = matchResult.group().replaceAll("[\\s-]", "");
            return "****-****-****-" + card.substring(card.length() - 4);
        });

        // Mask JWT tokens
        result = JWT_TOKEN_PATTERN.matcher(result).replaceAll("eyJ***[JWT_TOKEN]");

        // Mask Bearer tokens
        result = BEARER_TOKEN_PATTERN.matcher(result).replaceAll("$1 ***");

        // Mask API keys
        result = API_KEY_PATTERN.matcher(result).replaceAll("$1=***");

        return result;
    }

    /**
     * Mask password field specifically
     */
    public static String maskPassword(String password) {
        return password != null && !password.isEmpty() ? "***" : null;
    }

    /**
     * Mask email partially (keep first 2 chars and domain)
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        String maskedLocal = localPart.length() > 2
                ? localPart.substring(0, 2) + "***"
                : "***";

        return maskedLocal + "@" + domain;
    }

    /**
     * Mask credit card number (show last 4 digits only)
     */
    public static String maskCreditCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        String digits = cardNumber.replaceAll("[\\s-]", "");
        if (digits.length() < 12) {
            return "****";
        }

        return "****-****-****-" + digits.substring(digits.length() - 4);
    }

    /**
     * Mask JWT or Bearer token
     */
    public static String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        if (token.length() <= 10) {
            return "***";
        }

        return token.substring(0, 10) + "***";
    }

    /**
     * Check if string contains sensitive data keywords
     */
    public static boolean containsSensitiveData(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        String lowerInput = input.toLowerCase();
        String[] sensitiveKeywords = {
                "password", "passwd", "pwd",
                "token", "bearer",
                "api_key", "apikey", "api-key",
                "secret", "private_key", "private-key",
                "credit_card", "creditcard", "card_number",
                "ssn", "social_security"
        };

        for (String keyword : sensitiveKeywords) {
            if (lowerInput.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}
