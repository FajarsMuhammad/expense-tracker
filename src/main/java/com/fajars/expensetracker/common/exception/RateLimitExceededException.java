package com.fajars.expensetracker.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when rate limit is exceeded.
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String operation, int limit) {
        super(String.format("Rate limit exceeded for %s. Maximum %d requests per minute allowed.",
            operation, limit));
    }
}
