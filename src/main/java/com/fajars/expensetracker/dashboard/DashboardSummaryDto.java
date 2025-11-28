package com.fajars.expensetracker.dashboard;

import com.fajars.expensetracker.transaction.Transaction;

import com.fajars.expensetracker.transaction.TransactionSummaryDto;
import java.util.List;

public record DashboardSummaryDto(
        Double walletBalance,
        Double todayIncome,
        Double todayExpense,
        List<WeeklyTrendDto> weeklyTrend,
        List<TransactionSummaryDto> recentTransactions
) {}
