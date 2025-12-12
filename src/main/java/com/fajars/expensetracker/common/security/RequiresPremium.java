package com.fajars.expensetracker.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a controller method requires PREMIUM or TRIAL subscription tier.
 * FREE users will receive 403 Forbidden response.
 *
 * <p>This annotation is processed by {@link PremiumFeatureAspect} using AOP.
 * The aspect intercepts methods annotated with @RequiresPremium and validates
 * the user's subscription tier before allowing method execution.
 *
 * <p>Performance: AOP overhead is negligible (~1-2ms per request).
 * Tier check is cached in subscription service for optimal performance.
 *
 * <p>Usage example:
 * <pre>
 * &#64;RequiresPremium(
 *     feature = "financial_reports",
 *     message = "Reports are available for PREMIUM users only"
 * )
 * &#64;GetMapping("/reports/summary")
 * public ResponseEntity&lt;?&gt; getFinancialSummary(
 *     &#64;AuthenticationPrincipal UserIdentity userIdentity
 * ) {
 *     // Only reached if user is PREMIUM or TRIAL
 * }
 * </pre>
 *
 * @see PremiumFeatureAspect
 * @since Milestone 6
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPremium {

    /**
     * Optional custom message for upgrade prompt.
     * If not specified, a default message will be generated.
     *
     * @return the custom message for upgrade prompt
     */
    String message() default "";

    /**
     * Feature name for metrics tracking and logging.
     * If not specified, the method name will be used.
     *
     * <p>This value is used for:
     * <ul>
     *   <li>Metrics counter: premium.access.denied (tagged with feature name)</li>
     *   <li>Log messages for access denial tracking</li>
     *   <li>Analytics and business intelligence</li>
     * </ul>
     *
     * @return the feature name
     */
    String feature() default "";
}
