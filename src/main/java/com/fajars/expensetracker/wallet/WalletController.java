package com.fajars.expensetracker.wallet;

import com.fajars.expensetracker.user.UserResponse;
import com.fajars.expensetracker.user.UserService;
import com.fajars.expensetracker.wallet.usecase.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Wallet management APIs - Create, read, update and delete wallets")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final FindAllWallets findAllWallets;
    private final FindWalletById findWalletById;
    private final CreateWallet createWallet;
    private final UpdateWallet updateWallet;
    private final DeleteWallet deleteWallet;
    private final UserService userService;

    @Operation(summary = "List all wallets", description = "Get all wallets belonging to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved wallets list",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<WalletResponse>> listWallets() {
        UUID userId = getCurrentUserId();
        List<WalletResponse> wallets = findAllWallets.findAllByUserId(userId);
        return ResponseEntity.ok(wallets);
    }

    @Operation(summary = "Get wallet by ID", description = "Get a specific wallet by its ID. User must own the wallet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved wallet",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Wallet not found or access denied", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(
            @Parameter(description = "Wallet ID", required = true) @PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        WalletResponse wallet = findWalletById.findByIdAndUserId(id, userId);
        return ResponseEntity.ok(wallet);
    }

    @Operation(summary = "Create new wallet", description = "Create a new wallet. Free users are limited to 1 wallet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Wallet created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed or wallet limit exceeded", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Wallet creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateWalletRequest.class)))
            @Valid @RequestBody CreateWalletRequest request) {
        UUID userId = getCurrentUserId();
        WalletResponse wallet = createWallet.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @Operation(summary = "Update wallet", description = "Update an existing wallet. User must own the wallet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Wallet not found or access denied", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<WalletResponse> updateWallet(
            @Parameter(description = "Wallet ID", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Wallet update request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateWalletRequest.class)))
            @Valid @RequestBody UpdateWalletRequest request) {
        UUID userId = getCurrentUserId();
        WalletResponse wallet = updateWallet.update(id, userId, request);
        return ResponseEntity.ok(wallet);
    }

    @Operation(summary = "Delete wallet", description = "Delete a wallet. User must own the wallet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Wallet deleted successfully", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Wallet not found or access denied", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(
            @Parameter(description = "Wallet ID", required = true) @PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        deleteWallet.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        // Email is used as username in JWT
        String email = auth.getName();
        UserResponse user = userService.getByEmail(email);
        return user.id();
    }
}
