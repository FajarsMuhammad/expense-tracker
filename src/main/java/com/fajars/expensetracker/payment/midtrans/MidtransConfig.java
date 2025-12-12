package com.fajars.expensetracker.payment.midtrans;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Midtrans configuration properties.
 * Binds to application.yaml midtrans.* properties.
 */
@Configuration
@ConfigurationProperties(prefix = "midtrans")
@Getter
@Setter
public class MidtransConfig {

    /**
     * Midtrans Server Key for authentication.
     * Keep this secret and never expose to client.
     */
    private String serverKey;

    /**
     * Midtrans Client Key for frontend integration.
     * Can be safely exposed to client.
     */
    private String clientKey;

    /**
     * Midtrans API base URL.
     * Sandbox: https://app.sandbox.midtrans.com
     * Production: https://app.midtrans.com
     */
    private String apiUrl;

    /**
     * Environment flag (sandbox or production).
     */
    private boolean isProduction;

    /**
     * Get Basic Auth header value for API calls.
     * Format: "Basic {base64(serverKey:)}"
     *
     * @return Basic auth header value
     */
    public String getAuthHeader() {
        String credentials = serverKey + ":";
        String encodedCredentials = java.util.Base64.getEncoder()
            .encodeToString(credentials.getBytes());
        return "Basic " + encodedCredentials;
    }

    /**
     * Get Snap API URL for creating payment.
     *
     * @return Snap API URL
     */
    public String getSnapUrl() {
        return apiUrl + "/snap/v1/transactions";
    }

    /**
     * Validate configuration on startup.
     */
    public void validate() {
        if (serverKey == null || serverKey.isBlank()) {
            throw new IllegalStateException("Midtrans server key is required");
        }
        if (clientKey == null || clientKey.isBlank()) {
            throw new IllegalStateException("Midtrans client key is required");
        }
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("Midtrans API URL is required");
        }
    }
}
