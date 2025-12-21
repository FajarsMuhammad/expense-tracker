package com.fajars.expensetracker.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public UnauthorizedException() {
        this("UNAUTHORIZED", "Unauthorized access");
    }

    public UnauthorizedException(String message) {
        this("UNAUTHORIZED", message);
    }

    public UnauthorizedException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = HttpStatus.UNAUTHORIZED;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
