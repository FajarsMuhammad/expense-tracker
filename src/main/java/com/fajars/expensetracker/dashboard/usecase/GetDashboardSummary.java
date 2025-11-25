package com.fajars.expensetracker.dashboard.usecase;

import com.fajars.expensetracker.dashboard.DashboardSummaryDto;

import java.util.UUID;

/**
 * Use Case: Get dashboard summary with wallet balance, today's transactions and weekly trend
 */
public interface GetDashboardSummary {
    DashboardSummaryDto getSummary(UUID userId, UUID walletId);
}
