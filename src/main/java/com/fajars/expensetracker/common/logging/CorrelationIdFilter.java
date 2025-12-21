package com.fajars.expensetracker.common.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to add correlation ID to every request for tracking across logs.
 *
 * <p>Uses Java 25's ScopedValue API for efficient correlation ID management.
 * Also maintains MDC compatibility for SLF4J logging framework integration.
 *
 * <p><b>Migration to Java 25 ScopedValue:</b>
 * <ul>
 *   <li>Primary storage: {@link CorrelationContext} (ScopedValue-based)</li>
 *   <li>Secondary storage: SLF4J MDC (for logging framework compatibility)</li>
 *   <li>Benefits: Better performance, automatic cleanup, immutability</li>
 * </ul>
 *
 * @since Java 25 LTS
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get correlation ID from header or generate new one
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = CorrelationContext.generateCorrelationId();
        }

        // Add to response header
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

        // Execute request with correlation ID in scope (Java 25 ScopedValue)
        // Use a wrapper to handle checked exceptions properly
        final String finalCorrelationId = correlationId;
        ServletExceptionWrapper wrapper = new ServletExceptionWrapper();

        CorrelationContext.runWithCorrelationId(correlationId, () -> {
            try {
                // Also add to MDC for SLF4J logging compatibility
                MDC.put(CORRELATION_ID_MDC_KEY, finalCorrelationId);

                try {
                    chain.doFilter(request, response);
                } finally {
                    // Clean up MDC
                    MDC.remove(CORRELATION_ID_MDC_KEY);
                }
            } catch (IOException | ServletException e) {
                wrapper.exception = e;
            }
        });

        // Re-throw any exception that occurred
        if (wrapper.exception != null) {
            if (wrapper.exception instanceof IOException) {
                throw (IOException) wrapper.exception;
            } else if (wrapper.exception instanceof ServletException) {
                throw (ServletException) wrapper.exception;
            }
        }
    }

    /**
     * Wrapper to capture checked exceptions from lambda.
     */
    private static class ServletExceptionWrapper {
        Exception exception;
    }
}
