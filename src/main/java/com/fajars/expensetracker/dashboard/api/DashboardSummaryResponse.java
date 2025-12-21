package com.fajars.expensetracker.dashboard.api;

import com.fajars.expensetracker.transaction.api.TransactionSummaryResponse;
import java.util.List;

public record DashboardSummaryResponse(
        Double walletBalance,
        Double todayIncome,
        Double todayExpense,
        List<WeeklyTrendResponse> weeklyTrend,
        List<TransactionSummaryResponse> recentTransactions
) {}
