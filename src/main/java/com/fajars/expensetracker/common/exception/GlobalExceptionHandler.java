package com.fajars.expensetracker.common.exception;

import com.fajars.expensetracker.common.i18n.MessageHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global exception handler for all REST controllers.
 *
 * <p>Supports i18n via Accept-Language header (id, en).
 * All error messages are localized based on user's locale preference.
 *
 * @since Milestone 7
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private static final String CORRELATION_ID_KEY = "correlationId";

    private final MessageHelper messageHelper;

    /**
     * Handle validation errors (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Validation error on {}: {}", request.getRequestURI(), ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();

            validationErrors.add(ErrorResponse.ValidationError.builder()
                    .field(fieldName)
                    .message(errorMessage)
                    .rejectedValue(rejectedValue != null ? rejectedValue.toString() : null)
                    .build());
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageHelper.getMessage("validation.failed"))
                .message(messageHelper.getMessage("validation.failed"))
                .path(request.getRequestURI())
                .correlationId(MDC.get(CORRELATION_ID_KEY))
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle type mismatch errors (e.g., String instead of Integer)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("Type mismatch error on {}: {}", request.getRequestURI(), ex.getMessage());

        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageHelper.getMessage("common.invalid_request"))
                .message(message)
                .path(request.getRequestURI())
                .correlationId(MDC.get(CORRELATION_ID_KEY))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle authentication errors (invalid credentials)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        log.warn("Authentication failed on {}: Bad credentials", request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(messageHelper.getMessage("common.unauthorized"))
                .message(messageHelper.getMessage("auth.login.invalid_credentials"))
                .path(request.getRequestURI())
                .correlationId(MDC.get(CORRELATION_ID_KEY))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle general authentication errors
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.warn("Authentication error on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(messageHelper.getMessage("common.unauthorized"))
                .message(messageHelper.getMessage("common.unauthorized"))
                .path(request.getRequestURI())
                .correlationId(MDC.get(CORRELATION_ID_KEY))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle authorization errors (insufficient permissions)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(HttpStatus.FORBIDDEN.value())
                .error(messageHelper.getMessage("common.forbidden"))
                .message(messageHelper.getMessage("common.forbidden"))
                .path(request.getRequestURI())
                .correlationId(MDC.get(CORRELATION_ID_KEY))
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Illegal argument on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageHelper.getMessage("common.invalid_request"))
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(MDC.get(CORRELATION_ID_KEY))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(HttpStatus.NOT_FOUND.value())
                .error(messageHelper.getMessage("common.not_found"))
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(MDC.get(CORRELATION_ID_KEY))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle business logic exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        log.warn("Business exception on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(MDC.get(CORRELATION_ID_KEY))
                .details(ex.getDetails())
                .build();

        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    /**
     * Handle external service exceptions (payment gateway, etc.)
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(
            ExternalServiceException ex,
            HttpServletRequest request) {

        log.error("External service error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error(messageHelper.getMessage("system.service_unavailable"))
                .message(messageHelper.getMessage("system.service_unavailable"))
                .path(request.getRequestURI())
                .correlationId(MDC.get(CORRELATION_ID_KEY))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Handle rate limit exceeded exceptions
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request) {

        log.warn("Rate limit exceeded on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error(messageHelper.getMessage("system.rate_limit_exceeded"))
                .message(messageHelper.getMessage("system.rate_limit_exceeded"))
                .path(request.getRequestURI())
                .correlationId(MDC.get(CORRELATION_ID_KEY))
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // Log full stack trace for unexpected errors
        log.error("Unexpected error on {} [{}]: {}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage(),
                ex);

        // Don't expose internal error details to client
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(messageHelper.getMessage("common.internal_error"))
                .message(messageHelper.getMessage("common.internal_error"))
                .path(request.getRequestURI())
                .correlationId(MDC.get(CORRELATION_ID_KEY))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
