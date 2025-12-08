package com.fajars.expensetracker.subscription;

import com.fajars.expensetracker.auth.UserIdentity;
import com.fajars.expensetracker.subscription.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for subscription management. Handles subscription status, trial activation, and
 * cancellation.
 */
@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription", description = "Subscription management endpoints")
public class SubscriptionController {

    // Use cases
    private final GetUserSubscription getUserSubscription;
    private final CreateTrialSubscription createTrialSubscription;
    private final CancelSubscription cancelSubscription;
    private final UpgradeSubscription upgradeSubscription;

    /**
     * Get current subscription status.
     * <p>
     * GET /subscriptions/status
     * <p>
     * Response: { "tier": "FREE", "status": "ACTIVE", "isPremium": false, "isTrial": false,
     * "startedAt": "2025-12-06T10:00:00", "endedAt": null }
     */
    @GetMapping("/status")
    @Operation(
        summary = "Get subscription status",
        description = "Get current subscription tier and status for authenticated user",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<SubscriptionStatusResponse> getStatus(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        log.debug("Getting subscription status for user: {}", userId);

        Subscription subscription = getUserSubscription.getSubscription(userId);

        SubscriptionStatusResponse response = SubscriptionStatusResponse.builder()
            .tier(subscription.getPlan())
            .status(subscription.getStatus())
            .isPremium(subscription.isPremium())
            .isTrial(subscription.isTrial())
            .startedAt(subscription.getStartedAt())
            .endedAt(subscription.getEndedAt())
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Start trial subscription. User must be eligible (never had premium before).
     * <p>
     * POST /subscriptions/trial
     * <p>
     * Response: { "tier": "PREMIUM", "status": "TRIAL", "isPremium": true, "isTrial": true,
     * "startedAt": "2025-12-06T10:00:00", "endedAt": "2025-12-20T10:00:00" }
     */
    @PostMapping("/trial")
    @Operation(
        summary = "Start trial subscription",
        description = "Activate 14-day free trial for eligible users (never had premium before)",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<SubscriptionStatusResponse> startTrial(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        log.info("Starting trial subscription for user: {}", userId);

        Subscription subscription = createTrialSubscription.createTrial(userId);

        SubscriptionStatusResponse response = SubscriptionStatusResponse.builder()
            .tier(subscription.getPlan())
            .status(subscription.getStatus())
            .isPremium(subscription.isPremium())
            .isTrial(subscription.isTrial())
            .startedAt(subscription.getStartedAt())
            .endedAt(subscription.getEndedAt())
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Upgrade to premium (initiates payment flow). Redirects to payment creation endpoint.
     * <p>
     * POST /subscriptions/upgrade
     * <p>
     * This is a convenience endpoint that redirects to payment creation. User should call POST
     * /payments/subscription to get Snap token.
     */
    @PostMapping("/upgrade")
    @Operation(
        summary = "Upgrade to premium",
        description = "Initiate premium subscription upgrade. Returns information about how to proceed with payment.",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<UpgradeInfoResponse> upgrade(
        @AuthenticationPrincipal UserIdentity userIdentity
    ) {
        UUID userId = userIdentity.getUserId();
        log.info("User {} requesting premium upgrade", userId);

        UpgradeInfoResponse response = upgradeSubscription.upgrade(userId);

        if (response.premium()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel premium subscription. Subscription will remain active until end date, then downgrade
     * to FREE.
     * <p>
     * POST /subscriptions/cancel
     */
    @PostMapping("/cancel")
    @Operation(
        summary = "Cancel subscription",
        description = "Cancel premium subscription (remains active until end date)",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<SubscriptionStatusResponse> cancel(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        log.info("Cancelling subscription for user: {}", userId);

        cancelSubscription.cancel(userId);

        Subscription subscription = getUserSubscription.getSubscription(userId);

        SubscriptionStatusResponse response = SubscriptionStatusResponse.builder()
            .tier(subscription.getPlan())
            .status(subscription.getStatus())
            .isPremium(subscription.isPremium())
            .isTrial(subscription.isTrial())
            .startedAt(subscription.getStartedAt())
            .endedAt(subscription.getEndedAt())
            .build();

        return ResponseEntity.ok(response);
    }
}
