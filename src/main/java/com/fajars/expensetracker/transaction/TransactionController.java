package com.fajars.expensetracker.transaction;

import com.fajars.expensetracker.transaction.usecase.*;
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
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction management APIs - Manage income and expense transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final FindAllTransactions findAllTransactions;
    private final FindTransactionById findTransactionById;
    private final CreateTransaction createTransaction;
    private final UpdateTransaction updateTransaction;
    private final DeleteTransaction deleteTransaction;
    private final UserService userService;

    @Operation(
            summary = "List all transactions",
            description = "Get all transactions with optional filters (date range, wallet, category, type) and pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> listTransactions(
            @Parameter(description = "Filter by wallet ID", required = false)
            @RequestParam(required = false) UUID walletId,

            @Parameter(description = "Filter by category ID", required = false)
            @RequestParam(required = false) UUID categoryId,

            @Parameter(description = "Filter by transaction type (INCOME or EXPENSE)", required = false)
            @RequestParam(required = false) TransactionType type,

            @Parameter(description = "Filter transactions from this date (inclusive)", required = false)
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,

            @Parameter(description = "Filter transactions to this date (inclusive)", required = false)
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,

            @Parameter(description = "Page number (0-based)", required = false)
            @RequestParam(required = false, defaultValue = "0") Integer page,

            @Parameter(description = "Page size (max 100)", required = false)
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        UUID userId = getCurrentUserId();
        TransactionFilter filter = new TransactionFilter(walletId, categoryId, type, from, to, page, size);

        log.debug("Listing transactions for user {} with filter: {}", userId, filter);
        Page<TransactionResponse> transactions = findAllTransactions.findByUserIdWithFilters(userId, filter);

        return ResponseEntity.ok(transactions);
    }

    @Operation(
            summary = "Get transaction by ID",
            description = "Get a specific transaction by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @Parameter(description = "Transaction ID", required = true) @PathVariable UUID id
    ) {
        UUID userId = getCurrentUserId();
        TransactionResponse transaction = findTransactionById.findByIdAndUserId(id, userId);
        return ResponseEntity.ok(transaction);
    }

    @Operation(
            summary = "Create new transaction",
            description = "Create a new transaction (income or expense)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed or invalid references", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Transaction creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateTransactionRequest.class))
            )
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        UUID userId = getCurrentUserId();
        TransactionResponse transaction = createTransaction.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @Operation(
            summary = "Update transaction",
            description = "Update an existing transaction"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed or invalid references", content = @Content),
            @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @Parameter(description = "Transaction ID", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Transaction update request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateTransactionRequest.class))
            )
            @Valid @RequestBody UpdateTransactionRequest request
    ) {
        UUID userId = getCurrentUserId();
        TransactionResponse transaction = updateTransaction.update(userId, id, request);
        return ResponseEntity.ok(transaction);
    }

    @Operation(
            summary = "Delete transaction",
            description = "Delete a transaction"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Transaction ID", required = true) @PathVariable UUID id
    ) {
        UUID userId = getCurrentUserId();
        deleteTransaction.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

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
