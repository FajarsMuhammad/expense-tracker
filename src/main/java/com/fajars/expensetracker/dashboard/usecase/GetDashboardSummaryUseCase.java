package com.fajars.expensetracker.dashboard.usecase;

import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.dashboard.DashboardSummaryDto;
import com.fajars.expensetracker.dashboard.WeeklyTrendDto;
import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.transaction.TransactionRepository;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetDashboardSummaryUseCase implements GetDashboardSummary {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final MetricsService metricsService;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary(UUID userId, UUID walletId) {
        long startTime = System.currentTimeMillis();

        // Get wallet balance
        double walletBalance = calculateWalletBalance(userId, walletId);

        // Get today's transactions
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endOfDay = cal.getTime();

        List<Transaction> todayTransactions;
        if (walletId != null) {
            todayTransactions = transactionRepository.findByUserIdAndWalletIdAndDateBetween(
                    userId, walletId, startOfDay, endOfDay);
        } else {
            todayTransactions = transactionRepository.findByUserIdAndDateBetween(
                    userId, startOfDay, endOfDay);
        }

        double todayIncome = todayTransactions.stream()
                .filter(t -> "INCOME".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double todayExpense = todayTransactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Get weekly trend (last 7 days)
        List<WeeklyTrendDto> weeklyTrend = calculateWeeklyTrend(userId, walletId);

        // Get recent transactions (top 5)
        List<Transaction> recentTransactions;
        if (walletId != null) {
            recentTransactions = transactionRepository.findTop5ByUserIdAndWalletId(userId, walletId);
        } else {
            recentTransactions = transactionRepository.findTop5ByUserId(userId);
        }

        // Limit to 5
        if (recentTransactions.size() > 5) {
            recentTransactions = recentTransactions.subList(0, 5);
        }

        DashboardSummaryDto result = new DashboardSummaryDto(
                walletBalance,
                todayIncome,
                todayExpense,
                weeklyTrend,
                recentTransactions
        );

        // Record timing metric
        metricsService.recordTimer("dashboard.summary.generation.duration", startTime);

        return result;
    }

    private double calculateWalletBalance(UUID userId, UUID walletId) {
        if (walletId != null) {
            Optional<Wallet> walletOpt = walletRepository.findByIdAndUserId(walletId, userId);
            if (walletOpt.isEmpty()) {
                return 0.0;
            }
            Wallet wallet = walletOpt.get();
            double balance = wallet.getInitialBalance();

            if (wallet.getTransactions() != null) {
                for (Transaction t : wallet.getTransactions()) {
                    if ("INCOME".equals(t.getType())) {
                        balance += t.getAmount();
                    } else if ("EXPENSE".equals(t.getType())) {
                        balance -= t.getAmount();
                    }
                }
            }
            return balance;
        } else {
            // Sum all wallets
            List<Wallet> wallets = walletRepository.findByUserId(userId);
            double totalBalance = 0.0;
            for (Wallet wallet : wallets) {
                double balance = wallet.getInitialBalance();
                if (wallet.getTransactions() != null) {
                    for (Transaction t : wallet.getTransactions()) {
                        if ("INCOME".equals(t.getType())) {
                            balance += t.getAmount();
                        } else if ("EXPENSE".equals(t.getType())) {
                            balance -= t.getAmount();
                        }
                    }
                }
                totalBalance += balance;
            }
            return totalBalance;
        }
    }

    private List<WeeklyTrendDto> calculateWeeklyTrend(UUID userId, UUID walletId) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -6); // Last 7 days including today
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endDate = cal.getTime();

        List<Transaction> weekTransactions;
        if (walletId != null) {
            weekTransactions = transactionRepository.findByUserIdAndWalletIdAndDateBetween(
                    userId, walletId, startDate, endDate);
        } else {
            weekTransactions = transactionRepository.findByUserIdAndDateBetween(
                    userId, startDate, endDate);
        }

        // Group by date
        Map<Date, List<Transaction>> groupedByDate = new HashMap<>();
        Calendar tempCal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            tempCal.setTime(startDate);
            tempCal.add(Calendar.DAY_OF_MONTH, i);
            tempCal.set(Calendar.HOUR_OF_DAY, 0);
            tempCal.set(Calendar.MINUTE, 0);
            tempCal.set(Calendar.SECOND, 0);
            tempCal.set(Calendar.MILLISECOND, 0);
            groupedByDate.put(tempCal.getTime(), new ArrayList<>());
        }

        for (Transaction t : weekTransactions) {
            Calendar tCal = Calendar.getInstance();
            tCal.setTime(t.getDate());
            tCal.set(Calendar.HOUR_OF_DAY, 0);
            tCal.set(Calendar.MINUTE, 0);
            tCal.set(Calendar.SECOND, 0);
            tCal.set(Calendar.MILLISECOND, 0);
            Date dayDate = tCal.getTime();
            groupedByDate.computeIfAbsent(dayDate, k -> new ArrayList<>()).add(t);
        }

        return groupedByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    double income = entry.getValue().stream()
                            .filter(t -> "INCOME".equals(t.getType()))
                            .mapToDouble(Transaction::getAmount)
                            .sum();
                    double expense = entry.getValue().stream()
                            .filter(t -> "EXPENSE".equals(t.getType()))
                            .mapToDouble(Transaction::getAmount)
                            .sum();
                    return new WeeklyTrendDto(entry.getKey(), income, expense);
                })
                .collect(Collectors.toList());
    }
}
