package com.fajars.expensetracker.common.exception;

/**
 * Exception thrown when external service (payment gateway, etc.) fails.
 * Indicates a problem with third-party API integration.
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
