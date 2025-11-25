package com.fajars.expensetracker.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception for business logic violations
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, Object> details;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.details = null;
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.details = null;
    }

    public BusinessException(String message, HttpStatus status, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.details = details;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(message, HttpStatus.CONFLICT);
    }

    public static BusinessException unprocessable(String message) {
        return new BusinessException(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
