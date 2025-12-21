package com.fajars.expensetracker.dashboard.api;

import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.dashboard.usecase.GetDashboardSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard and analytics APIs - Get financial summaries and trends")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final GetDashboardSummary getDashboardSummary;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "Get dashboard summary",
            description = "Get financial summary including wallet balance, today's income/expense, weekly trends, and recent transactions. Optionally filter by wallet ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard summary",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardSummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
    })
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @Parameter(description = "Optional wallet ID to filter summary by specific wallet", required = false)
            @RequestParam(required = false) UUID walletId) {
        UUID userId = currentUserProvider.getUserId();
        DashboardSummaryResponse summary = getDashboardSummary.getSummary(userId, walletId);
        return ResponseEntity.ok(summary);
    }
}
