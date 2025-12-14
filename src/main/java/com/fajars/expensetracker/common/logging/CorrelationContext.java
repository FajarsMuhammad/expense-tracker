package com.fajars.expensetracker.common.logging;

import java.util.UUID;

/**
 * Modern correlation ID context using Java 25 ScopedValue API.
 *
 * <p>ScopedValue provides better performance than ThreadLocal by:
 * <ul>
 *   <li>Immutable bindings that can't be modified during execution</li>
 *   <li>Automatic cleanup without explicit removal</li>
 *   <li>Better memory efficiency and lower overhead</li>
 *   <li>Improved security through immutability</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * String correlationId = generateCorrelationId();
 * CorrelationContext.runWithCorrelationId(correlationId, () -> {
 *     // Inside this scope, correlation ID is available
 *     String id = CorrelationContext.get(); // Returns the correlation ID
 *     // Process request...
 * });
 * // Outside the scope, correlation ID is automatically cleaned up
 * </pre>
 *
 * @since Java 25 LTS
 * @see ScopedValue
 */
public class CorrelationContext {

    /**
     * Scoped value holding the correlation ID for the current request.
     * This is more efficient than ThreadLocal and automatically cleaned up.
     */
    private static final ScopedValue<String> CORRELATION_ID = ScopedValue.newInstance();

    /**
     * Private constructor to prevent instantiation.
     */
    private CorrelationContext() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Execute a task with a correlation ID in scope.
     *
     * @param correlationId the correlation ID to bind
     * @param task the task to execute
     */
    public static void runWithCorrelationId(String correlationId, Runnable task) {
        ScopedValue.where(CORRELATION_ID, correlationId).run(task);
    }

    /**
     * Get the current correlation ID from the scoped context.
     *
     * @return the correlation ID, or null if not set
     */
    public static String get() {
        return CORRELATION_ID.isBound() ? CORRELATION_ID.get() : null;
    }

    /**
     * Get the current correlation ID, or generate a new one if not set.
     *
     * @return the correlation ID
     */
    public static String getOrGenerate() {
        return CORRELATION_ID.isBound() ? CORRELATION_ID.get() : generateCorrelationId();
    }

    /**
     * Check if a correlation ID is currently bound.
     *
     * @return true if a correlation ID is bound, false otherwise
     */
    public static boolean isBound() {
        return CORRELATION_ID.isBound();
    }

    /**
     * Generate a new correlation ID using UUID.
     *
     * @return a new correlation ID
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
