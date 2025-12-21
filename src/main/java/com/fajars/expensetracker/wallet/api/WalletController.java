package com.fajars.expensetracker.wallet.api;

import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.wallet.usecase.createwallet.CreateWallet;
import com.fajars.expensetracker.wallet.usecase.deletewallet.DeleteWallet;
import com.fajars.expensetracker.wallet.usecase.fetchwallet.FindAllWallets;
import com.fajars.expensetracker.wallet.usecase.retrievewallet.FindWalletById;
import com.fajars.expensetracker.wallet.usecase.update.UpdateWallet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "List all wallets", description = "Get all wallets belonging to the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved wallets list",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<WalletResponse>> listWallets() {
        List<WalletResponse> wallets = findAllWallets.findAllByUserId();
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
        @Parameter(description = "Wallet ID", required = true) @PathVariable UUID id
    ) {
        UUID userId = currentUserProvider.getUserId();
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
        @Valid @RequestBody CreateWalletRequest request
    ) {
        UUID userId = currentUserProvider.getUserId();
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
        @Valid @RequestBody UpdateWalletRequest request
    ) {
        WalletResponse wallet = updateWallet.update(id, request);
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
        @Parameter(description = "Wallet ID", required = true) @PathVariable UUID id
    ) {
        deleteWallet.delete(id);
        return ResponseEntity.noContent().build();
    }
}
