package com.fajars.expensetracker.dashboard;

import com.fajars.expensetracker.transaction.TransactionSummaryResponse;
import java.util.List;

public record DashboardSummaryResponse(
        Double walletBalance,
        Double todayIncome,
        Double todayExpense,
        List<WeeklyTrendResponse> weeklyTrend,
        List<TransactionSummaryResponse> recentTransactions
) {}
