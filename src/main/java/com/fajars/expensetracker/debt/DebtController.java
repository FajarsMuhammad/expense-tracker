package com.fajars.expensetracker.debt;

import com.fajars.expensetracker.debt.usecase.*;
import com.fajars.expensetracker.user.UserDto;
import com.fajars.expensetracker.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Debt Management.
 * Provides endpoints for creating, tracking, and managing debts and payments.
 *
 * This controller follows Clean Architecture:
 * - No business logic (delegated to use cases)
 * - Only HTTP concerns (request/response mapping)
 * - Ownership validation through use cases
 */
@RestController
@RequestMapping("/debts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Debts", description = "Debt & Receivables Management APIs - Track debts, payments, and balances")
@SecurityRequirement(name = "bearerAuth")
public class DebtController {

    private final CreateDebt createDebt;
    private final AddDebtPayment addDebtPayment;
    private final MarkDebtAsPaid markDebtAsPaid;
    private final GetDebtDetail getDebtDetail;
    private final ListDebts listDebts;
    private final UserService userService;

    @Operation(
        summary = "Create a new debt",
        description = "Create a new debt record for tracking money owed or receivable"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Debt created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DebtResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
    })
    @PostMapping
    public ResponseEntity<DebtResponse> createDebt(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Debt creation request",
            required = true,
            content = @Content(schema = @Schema(implementation = CreateDebtRequest.class))
        )
        @Valid @RequestBody CreateDebtRequest request
    ) {
        UUID userId = getCurrentUserId();
        log.debug("Creating debt for user {}: {}", userId, request);

        DebtResponse debt = createDebt.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(debt);
    }

    @Operation(
        summary = "List all debts",
        description = "Get all debts with optional filters (status, overdue) and pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved debts",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<DebtResponse>> listDebts(
        @Parameter(description = "Filter by debt status (OPEN, PARTIAL, PAID)", required = false)
        @RequestParam(required = false) DebtStatus status,

        @Parameter(description = "Filter only overdue debts", required = false)
        @RequestParam(required = false) Boolean overdue,

        @Parameter(description = "Page number (0-based)", required = false)
        @RequestParam(required = false, defaultValue = "0") Integer page,

        @Parameter(description = "Page size (max 100)", required = false)
        @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        UUID userId = getCurrentUserId();
        DebtFilter filter = new DebtFilter(status, overdue, page, size);

        log.debug("Listing debts for user {} with filter: {}", userId, filter);
        Page<DebtResponse> debts = listDebts.list(userId, filter);

        return ResponseEntity.ok(debts);
    }

    @Operation(
        summary = "Get debt details",
        description = "Get detailed information about a specific debt including payment history"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved debt details",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DebtDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "Debt not found", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DebtDetailResponse> getDebt(
        @Parameter(description = "Debt ID", required = true) @PathVariable UUID id
    ) {
        UUID userId = getCurrentUserId();
        log.debug("Getting debt {} for user {}", id, userId);

        DebtDetailResponse debt = getDebtDetail.getDetail(userId, id);
        return ResponseEntity.ok(debt);
    }

    @Operation(
        summary = "Add a payment to a debt",
        description = "Record a payment made towards a debt. The payment reduces the remaining amount and updates the debt status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment added successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddDebtPayment.AddDebtPaymentResult.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid payment amount or debt already paid", content = @Content),
        @ApiResponse(responseCode = "404", description = "Debt not found", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping("/{id}/payments")
    public ResponseEntity<AddDebtPayment.AddDebtPaymentResult> addPayment(
        @Parameter(description = "Debt ID", required = true) @PathVariable UUID id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payment request",
            required = true,
            content = @Content(schema = @Schema(implementation = AddDebtPaymentRequest.class))
        )
        @Valid @RequestBody AddDebtPaymentRequest request
    ) {
        UUID userId = getCurrentUserId();
        log.debug("Adding payment to debt {} for user {}: {}", id, userId, request);

        AddDebtPayment.AddDebtPaymentResult result = addDebtPayment.addPayment(userId, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(
        summary = "Mark debt as fully paid",
        description = "Mark a debt as fully paid, setting the remaining amount to zero and status to PAID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Debt marked as paid successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DebtResponse.class))),
        @ApiResponse(responseCode = "404", description = "Debt not found", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<DebtResponse> markAsPaid(
        @Parameter(description = "Debt ID", required = true) @PathVariable UUID id
    ) {
        UUID userId = getCurrentUserId();
        log.debug("Marking debt {} as paid for user {}", id, userId);

        DebtResponse debt = markDebtAsPaid.markAsPaid(userId, id);
        return ResponseEntity.ok(debt);
    }

    /**
     * Get the current authenticated user's ID.
     * Throws IllegalStateException if user is not authenticated.
     */
    private UUID getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        String email = auth.getName();
        UserDto user = userService.getByEmail(email);
        return user.id();
    }
}
