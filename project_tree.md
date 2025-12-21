# project_tree — com.fajars.expensetracker

Improved, developer-friendly view of the Java package `src/main/java/com/fajars/expensetracker`.
Generated: 2025-12-20

Summary:
- Package root: `src/main/java/com/fajars/expensetracker`
- Total production source files scanned: 207

Tree (folder-first, alphabetical):

```text
src/main/java/com/fajars/expensetracker
├─ ExpenseTrackerApplication.java
├─ auth
│  ├─ AuthController.java
│  ├─ AuthResponse.java
│  ├─ AuthService.java
│  ├─ CustomUserDetailsService.java
│  ├─ LoginRequest.java
│  ├─ RefreshRequest.java
│  ├─ RegisterRequest.java
│  ├─ UserIdentity.java
│  ├─ WalletInfo.java
│  └─ SubscriptionInfo.java
├─ category
│  ├─ Category.java
│  ├─ CategoryController.java
│  ├─ CategoryRepository.java
│  ├─ CategoryResponse.java
│  ├─ CategoryType.java
│  ├─ CreateCategoryRequest.java
│  ├─ UpdateCategoryRequest.java
│  └─ usecase
│     ├─ CreateCategory.java
│     ├─ CreateCategoryUseCase.java
│     ├─ DeleteCategory.java
│     ├─ DeleteCategoryUseCase.java
│     ├─ FindAllCategories.java
│     ├─ FindAllCategoriesUseCase.java
│     ├─ FindCategoryById.java
│     ├─ FindCategoryByIdUseCase.java
│     ├─ FindCategoriesByType.java
│     ├─ FindCategoriesByTypeUseCase.java
│     ├─ UpdateCategory.java
│     └─ UpdateCategoryUseCase.java
├─ common
│  ├─ converter
│  │  └─ JsonbConverter.java
│  ├─ config
│  │  ├─ DataSeeder.java
│  │  ├─ JacksonConfig.java
│  │  ├─ OpenApiConfig.java
│  │  └─ LocaleConfiguration.java
│  ├─ exception
│  │  ├─ BusinessException.java
│  │  ├─ ErrorResponse.java
│  │  ├─ ExternalServiceException.java
│  │  ├─ GlobalExceptionHandler.java
│  │  ├─ RateLimitExceededException.java
│  │  └─ ResourceNotFoundException.java
│  ├─ i18n
│  │  └─ MessageHelper.java
│  ├─ logging
│  │  ├─ BusinessEventLogger.java
│  │  ├─ CorrelationContext.java
│  │  ├─ CorrelationIdFilter.java
│  │  ├─ LoggingAspect.java
│  │  ├─ SecurityLoggingFilter.java
│  │  └─ SensitiveDataFilter.java
│  ├─ metrics
│  │  └─ MetricsService.java
│  ├─ ratelimit
│  │  ├─ RateLimiter.java
│  │  └─ ReportFrequencyLimiter.java
│  ├─ security
│  │  ├─ PremiumFeatureAspect.java
│  │  ├─ RequiresPremium.java
│  │  └─ SecurityConfig.java
│  ├─ util
│  │  ├─ JwtAuthenticationFilter.java
│  │  └─ JwtUtil.java
│  └─ validation
│     └─ DateRangeValidator.java
├─ config
│  └─ CacheConfig.java
├─ dashboard
│  ├─ DashboardController.java
│  ├─ DashboardSummaryResponse.java
│  ├─ WeeklyTrendResponse.java
│  └─ usecase
│     ├─ GetDashboardSummary.java
│     └─ GetDashboardSummaryUseCase.java
├─ debt
│  ├─ AddDebtPaymentRequest.java
│  ├─ CreateDebtRequest.java
│  ├─ Debt.java
│  ├─ DebtController.java
│  ├─ DebtDetailResponse.java
│  ├─ DebtFilter.java
│  ├─ DebtPayment.java
│  ├─ DebtPaymentRepository.java
│  ├─ DebtRepository.java
│  ├─ DebtResponse.java
│  ├─ DebtStatus.java
│  ├─ DebtType.java
│  ├─ UpdateDebtRequest.java
│  └─ usecase
│     ├─ AddDebtPayment.java
│     ├─ AddDebtPaymentUseCase.java
│     ├─ CreateDebt.java
│     ├─ CreateDebtUseCase.java
│     ├─ GetDebtDetail.java
│     ├─ GetDebtDetailUseCase.java
│     ├─ ListDebts.java
│     ├─ ListDebtsUseCase.java
│     ├─ MarkDebtAsPaid.java
│     ├─ MarkDebtAsPaidUseCase.java
│     ├─ UpdateDebt.java
│     └─ UpdateDebtUseCase.java
├─ payment
│  ├─ MidtransClient.java
│  ├─ MidtransConfig.java
│  ├─ MidtransSnapRequest.java
│  ├─ MidtransSnapResponse.java
│  ├─ MidtransWebhookPayload.java
│  ├─ PaymentController.java
│  ├─ PaymentMethod.java
│  ├─ PaymentProvider.java
│  ├─ PaymentRepository.java
│  ├─ PaymentStatus.java
│  ├─ PaymentTransaction.java
│  ├─ WebhookVerifier.java
│  └─ usecase
│     ├─ CreatePaymentResponse.java
│     ├─ CreateSubscriptionPayment.java
│     ├─ CreateSubscriptionPaymentUseCase.java
│     ├─ ProcessPaymentWebhook.java
│     └─ ProcessPaymentWebhookUseCase.java
├─ report
│  ├─ CsvExporter.java
│  ├─ ExcelExporter.java
│  ├─ ExportController.java
│  ├─ ExportFilter.java
│  ├─ ExportFormat.java
│  ├─ ExportRequest.java
│  ├─ ExportResponse.java
│  ├─ ExportType.java
│  ├─ FinancialSummaryResponse.java
│  ├─ GenerateFinancialSummary.java
│  ├─ GenerateFinancialSummaryUseCase.java
│  ├─ GetCategoryBreakdown.java
│  ├─ GetCategoryBreakdownUseCase.java
│  ├─ GetIncomeExpenseTrend.java
│  ├─ GetIncomeExpenseTrendUseCase.java
│  ├─ Granularity.java
│  ├─ ReportController.java
│  ├─ ReportFilter.java
│  ├─ TrendDataResponse.java
│  ├─ WalletBalanceResponse.java
│  ├─ CategoryBreakdownResponse.java
│  └─ usecase
│     ├─ ExportTransactions.java
│     └─ ExportTransactionsUseCase.java
├─ subscription
│  ├─ ActivateSubscription.java
│  ├─ ActivateSubscriptionUseCase.java
│  ├─ CancelSubscription.java
│  ├─ CancelSubscriptionUseCase.java
│  ├─ CheckTrialEligibility.java
│  ├─ CheckTrialEligibilityUseCase.java
│  ├─ CreateFreeSubscription.java
│  ├─ CreateFreeSubscriptionUseCase.java
│  ├─ CreateTrialSubscription.java
│  ├─ CreateTrialSubscriptionUseCase.java
│  ├─ ProcessExpiredTrialsScheduler.java
│  ├─ Subscription.java
│  ├─ SubscriptionController.java
│  ├─ SubscriptionHelper.java
│  ├─ SubscriptionRepository.java
│  ├─ SubscriptionService.java
│  ├─ SubscriptionStatus.java
│  ├─ SubscriptionStatusResponse.java
│  ├─ SubscriptionTier.java
│  ├─ UpgradeInfoResponse.java
│  └─ usecase
│     ├─ UpgradeSubscription.java
│     └─ UpgradeSubscriptionUseCase.java
├─ transaction
│  ├─ CategoryBreakdown.java
│  ├─ CreateTransaction.java
│  ├─ CreateTransactionRequest.java
│  ├─ CreateTransactionUseCase.java
│  ├─ DeleteTransaction.java
│  ├─ DeleteTransactionUseCase.java
│  ├─ FindAllTransactions.java
│  ├─ FindAllTransactionsUseCase.java
│  ├─ FindTransactionById.java
│  ├─ FindTransactionByIdUseCase.java
│  ├─ Transaction.java
│  ├─ TransactionController.java
│  ├─ TransactionExportRow.java
│  ├─ TransactionExportRepository.java
│  ├─ TransactionFilter.java
│  ├─ TransactionPageResponse.java
│  ├─ TransactionRepository.java
│  ├─ TransactionResponse.java
│  ├─ TransactionSummary.java
│  ├─ TransactionSummaryResponse.java
│  ├─ TransactionType.java
│  ├─ TrendData.java
│  ├─ UpdateTransaction.java
│  └─ UpdateTransactionUseCase.java
├─ user
│  ├─ GetUserProfile.java
│  ├─ GetUserProfileUseCase.java
│  ├─ ProfileResponse.java
│  ├─ UpdateProfileRequest.java
│  └─ User.java
└─ (end of package)

---

If you'd like this tree saved with file counts, as a full path list (`FULL_TREE.txt`), or want any subtree expanded to include tests or source code snippets, tell me which option and I'll do the next step.
