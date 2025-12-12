package com.fajars.expensetracker.payment.midtrans;

import com.fajars.expensetracker.common.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Client for Midtrans Snap API integration.
 * Handles HTTP communication with Midtrans payment gateway.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MidtransClient {

    private final MidtransConfig midtransConfig;
    private final WebClient.Builder webClientBuilder;

    /**
     * Create payment transaction via Midtrans Snap API.
     *
     * @param request the payment request
     * @return Snap response with token and redirect URL
     * @throws ExternalServiceException if API call fails
     */
    public MidtransSnapResponse createTransaction(MidtransSnapRequest request) {
        log.info("Creating Midtrans transaction for order: {}",
            request.transactionDetails().orderId());

        try {
            WebClient webClient = webClientBuilder
                .baseUrl(midtransConfig.getApiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, midtransConfig.getAuthHeader())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

            MidtransSnapResponse response = webClient
                .post()
                .uri("/snap/v1/transactions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MidtransSnapResponse.class)
                .timeout(Duration.ofSeconds(10))
                .block();

            if (response == null) {
                throw new ExternalServiceException(
                    "Midtrans API returned null response"
                );
            }

            log.info("Successfully created Midtrans transaction: {}",
                request.transactionDetails().orderId());

            return response;

        } catch (WebClientResponseException e) {
            log.error("Midtrans API error: {} - {}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException(
                "Failed to create payment: " + e.getMessage(),
                e
            );
        } catch (Exception e) {
            log.error("Unexpected error calling Midtrans API", e);
            throw new ExternalServiceException(
                "Failed to create payment: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Get transaction status from Midtrans (optional feature for manual checks).
     *
     * @param orderId the order ID
     * @return transaction status response
     */
    public Mono<String> getTransactionStatus(String orderId) {
        log.info("Fetching transaction status for order: {}", orderId);

        WebClient webClient = webClientBuilder
            .baseUrl(midtransConfig.getApiUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, midtransConfig.getAuthHeader())
            .build();

        return webClient
            .get()
            .uri("/v2/{orderId}/status", orderId)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(5))
            .doOnSuccess(response -> log.info("Transaction status retrieved: {}", orderId))
            .doOnError(error -> log.error("Failed to get transaction status", error));
    }
}
