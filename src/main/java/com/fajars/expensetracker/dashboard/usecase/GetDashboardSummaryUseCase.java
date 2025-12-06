package com.fajars.expensetracker.dashboard.usecase;

import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.dashboard.DashboardSummaryDto;
import com.fajars.expensetracker.dashboard.WeeklyTrendDto;
import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.transaction.TransactionRepository;
import com.fajars.expensetracker.transaction.TransactionSummaryDto;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zoneId);

        LocalDateTime startOfDay = today.atStartOfDay().minusDays(6);
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Transaction> todayTransactions;
        if (walletId != null) {
            todayTransactions = transactionRepository
                .findByUserIdAndWalletIdAndDateBetween(userId, walletId, startOfDay, endOfDay);
        } else {
            todayTransactions = transactionRepository
                .findByUserIdAndDateBetween(userId, startOfDay, endOfDay);
        }

        double todayIncome = todayTransactions.stream()
            .filter(t -> "INCOME".equals(t.getType().name()))
            .mapToDouble(Transaction::getAmount).sum();

        double todayExpense = todayTransactions.stream()
            .filter(t -> "EXPENSE".equals(t.getType().name()))
            .mapToDouble(Transaction::getAmount).sum();

        // Get weekly trend (last 7 days)
        List<WeeklyTrendDto> weeklyTrend = calculateWeeklyTrend(userId, walletId);

        // Get recent transactions (top 5)
        List<Transaction> recentTransactions;
        if (walletId != null) {
            recentTransactions = transactionRepository
                .findTop5ByUserIdAndWalletId(userId, walletId);
        } else {
            recentTransactions = transactionRepository.findTop5ByUserId(userId);
        }

        // Limit to 5
        if (recentTransactions.size() > 5) {
            recentTransactions = recentTransactions.subList(0, 5);
        }

        List<TransactionSummaryDto> recentTransactionDtos = recentTransactions.stream().limit(5)
            .map(TransactionSummaryDto::from).toList();

        DashboardSummaryDto result = new DashboardSummaryDto(walletBalance, todayIncome,
                                                             todayExpense, weeklyTrend,
                                                             recentTransactionDtos);

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
                    if ("INCOME".equals(t.getType().name())) {
                        balance += t.getAmount();
                    } else if ("EXPENSE".equals(t.getType().name())) {
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
                        if ("INCOME".equals(t.getType().name())) {
                            balance += t.getAmount();
                        } else if ("EXPENSE".equals(t.getType().name())) {
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
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zoneId);

        LocalDateTime startDate = today.atStartOfDay().minusDays(6);
        LocalDateTime endDate = today.atTime(LocalTime.MAX);

        List<Transaction> weekTransactions;
        if (walletId != null) {
            weekTransactions = transactionRepository
                .findByUserIdAndWalletIdAndDateBetween(userId, walletId, startDate, endDate);
        } else {
            weekTransactions = transactionRepository
                .findByUserIdAndDateBetween(userId, startDate, endDate);
        }

        Map<LocalDate, List<Transaction>> groupedByDate = new HashMap<>();

        LocalDate startDateMinus6Days = LocalDate.now().minusDays(6);

        // Initialize last 7 days (including today)
        for (int i = 0; i < 7; i++) {
            groupedByDate.put(startDateMinus6Days.plusDays(i), new ArrayList<>());
        }

        // Group transactions by LocalDate
        for (Transaction t : weekTransactions) {
            LocalDate transactionDate = t.getDate().toLocalDate();
            groupedByDate.computeIfAbsent(transactionDate, k -> new ArrayList<>()).add(t);
        }

        return groupedByDate.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                double income = entry.getValue().stream()
                    .filter(t -> "INCOME".equals(t.getType().name()))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
                double expense = entry.getValue().stream()
                    .filter(t -> "EXPENSE".equals(t.getType().name()))
                    .mapToDouble(Transaction::getAmount)
                    .sum();

                return new WeeklyTrendDto(entry.getKey(), income, expense);
            }).collect(Collectors.toList());
    }
}
