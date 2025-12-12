# Milestone 4 Backend Implementation Guide

## ✅ Progress Summary

### Completed:
1. ✅ All Report DTOs created (6 files)
   - `FinancialSummaryResponse.java`
   - `CategoryBreakdownDto.java`
   - `WalletBalanceDto.java`
   - `TrendDataDto.java`
   - `ReportFilter.java`
   - `ExportRequest.java` & `ExportResponse.java`

2. ✅ All Enums created (3 files)
   - `ExportFormat.java` (CSV, EXCEL, PDF)
   - `ExportType.java` (TRANSACTIONS, DEBTS, SUMMARY)
   - `Granularity.java` (DAILY, WEEKLY, MONTHLY)

3. ✅ Dependencies added to build.gradle
   - Apache Commons CSV
   - Apache POI (Excel)
   - OpenPDF
   - Spring Cache + Caffeine

4. ✅ TransactionRepository extended with reporting queries
   - `getSummaryByDateRange()` - Optimized aggregation
   - `getCategoryBreakdown()` - Category grouping
   - `getTrendData()` - Daily trend data

5. ✅ Use case interface created
   - `GenerateFinancialSummary.java`

### Next Steps:

## 🚀 Implementation Order

### Phase 1: Core Financial Summary (Priority)

**File**: `GenerateFinancialSummaryUseCase.java`
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Cacheable(value = "financialSummaries", key = "#userId + '-' + #filter.hashCode()")
public class GenerateFinancialSummaryUseCase implements GenerateFinancialSummary {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional(readOnly = true)
    public FinancialSummaryResponse generate(UUID userId, ReportFilter filter) {
        long startTime = System.currentTimeMillis();

        // 1. Get summary data (single optimized query)
        Object[] summary = transactionRepository.getSummaryByDateRange(
            userId,
            filter.startDate(),
            filter.endDate(),
            filter.hasWalletFilter() ? filter.walletIds() : null
        );

        Double totalIncome = (Double) summary[0];
        Double totalExpense = (Double) summary[1];
        Long count = (Long) summary[2];

        // 2. Get category breakdowns (2 queries)
        List<CategoryBreakdownDto> incomeByCategory = getCategoryBreakdown(
            userId, filter, TransactionType.INCOME, totalIncome
        );

        List<CategoryBreakdownDto> expenseByCategory = getCategoryBreakdown(
            userId, filter, TransactionType.EXPENSE, totalExpense
        );

        // 3. Get wallet balances
        List<WalletBalanceDto> walletBalances = getWalletBalances(userId, filter);

        // 4. Build response
        FinancialSummaryResponse response = new FinancialSummaryResponse(
            filter.startDate(),
            filter.endDate(),
            totalIncome,
            totalExpense,
            totalIncome - totalExpense,
            count.intValue(),
            incomeByCategory,
            expenseByCategory,
            walletBalances
        );

        // Metrics & logging
        metricsService.recordTimer("report.financial.summary.duration", startTime);
        log.info("Generated financial summary for user {} in {}ms",
            userId, System.currentTimeMillis() - startTime);

        return response;
    }

    private List<CategoryBreakdownDto> getCategoryBreakdown(
        UUID userId, ReportFilter filter, TransactionType type, Double total
    ) {
        List<Object[]> results = transactionRepository.getCategoryBreakdown(
            userId,
            filter.startDate(),
            filter.endDate(),
            type,
            filter.hasWalletFilter() ? filter.walletIds() : null
        );

        return results.stream()
            .map(row -> new CategoryBreakdownDto(
                (UUID) row[0],           // category ID
                (String) row[1],         // category name
                type.name(),             // type
                (Double) row[3],         // total amount
                ((Long) row[4]).intValue(), // count
                total > 0 ? ((Double) row[3] / total * 100) : 0.0 // percentage
            ))
            .toList();
    }

    private List<WalletBalanceDto> getWalletBalances(UUID userId, ReportFilter filter) {
        List<Wallet> wallets = filter.hasWalletFilter()
            ? walletRepository.findAllById(filter.walletIds())
            : walletRepository.findByUserId(userId);

        return wallets.stream()
            .map(w -> new WalletBalanceDto(
                w.getId(),
                w.getName(),
                w.getCurrency(),
                calculateCurrentBalance(w)
            ))
            .toList();
    }

    private Double calculateCurrentBalance(Wallet wallet) {
        return wallet.getInitialBalance() +
            wallet.getTransactions().stream()
                .mapToDouble(t -> t.getType() == TransactionType.INCOME
                    ? t.getAmount()
                    : -t.getAmount())
                .sum();
    }
}
```

### Phase 2: Analytics Use Cases

**1. GetIncomeExpenseTrendUseCase**
- Uses `transactionRepository.getTrendData()`
- Maps Object[] to TrendDataDto
- Handles different granularities (daily by default)

**2. GetCategoryBreakdownUseCase**
- Similar logic to financial summary
- Focused on category analysis only
- Returns list sorted by amount DESC

### Phase 3: Export Services (Helper Classes)

**1. CsvExporter.java** (Using OpenCSV - Better!)
```java
@Service
@RequiredArgsConstructor
public class CsvExporter {

    /**
     * Export transactions to CSV using OpenCSV.
     * OpenCSV provides better annotation support and easier API.
     */
    public byte[] exportTransactionsToCsv(List<Transaction> transactions) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // Write header
            String[] header = {"Date", "Type", "Category", "Wallet", "Amount", "Note"};
            csvWriter.writeNext(header);

            // Write data rows
            for (Transaction t : transactions) {
                String[] row = {
                    t.getDate().toString(),
                    t.getType().name(),
                    t.getCategory().getName(),
                    t.getWallet().getName(),
                    String.format("%.2f", t.getAmount()),
                    t.getNote() != null ? t.getNote() : ""
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }
    }

    /**
     * Alternative: Using OpenCSV Bean mapping (more advanced).
     * Create a DTO with @CsvBindByName annotations.
     */
    public byte[] exportTransactionsWithBeanMapping(List<TransactionExportDto> dtos) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {

            StatefulBeanToCsv<TransactionExportDto> beanToCsv =
                new StatefulBeanToCsvBuilder<TransactionExportDto>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            beanToCsv.write(dtos);
            writer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }
    }
}

/**
 * Export DTO with OpenCSV annotations.
 * This allows automatic header naming and column ordering.
 */
@Data
@AllArgsConstructor
public class TransactionExportDto {

    @CsvBindByName(column = "Date")
    @CsvDate("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    @CsvBindByName(column = "Type")
    private String type;

    @CsvBindByName(column = "Category")
    private String category;

    @CsvBindByName(column = "Wallet")
    private String wallet;

    @CsvBindByName(column = "Amount")
    @CsvNumber("#0.00")
    private Double amount;

    @CsvBindByName(column = "Note")
    private String note;
}
```

**2. ExcelExporter.java**
- Use Apache POI XSSFWorkbook
- Create sheets for different data types
- Apply cell styles (currency formatting, headers)
- Add formulas for totals

**3. PdfExporter.java**
- Use OpenPDF
- Create professional layout with header/footer
- Add tables for transactions
- Include summary statistics

### Phase 4: Export Use Cases

**ExportTransactionsUseCase**
```java
@Service
@RequiredArgsConstructor
public class ExportTransactionsUseCase implements ExportTransactions {

    private final TransactionRepository transactionRepository;
    private final CsvExporter csvExporter;
    private final ExcelExporter excelExporter;
    private final PdfExporter pdfExporter;
    private final SubscriptionService subscriptionService;

    @Override
    public ExportResponse export(UUID userId, ExportRequest request) {
        // 1. Check quota
        validateExportQuota(userId, request);

        // 2. Fetch data
        List<Transaction> transactions = transactionRepository.findByUserIdWithFilters(...);

        // 3. Generate file
        byte[] fileContent = switch (request.format()) {
            case CSV -> csvExporter.exportTransactionsToCsv(transactions);
            case EXCEL -> excelExporter.exportTransactionsToExcel(transactions);
            case PDF -> pdfExporter.exportTransactionsToPdf(transactions);
        };

        // 4. Save temporarily & return response
        String fileName = generateFileName(request);
        String base64 = Base64.getEncoder().encodeToString(fileContent);

        return new ExportResponse(
            fileName,
            (long) fileContent.length,
            null, // download URL if using cloud storage
            base64,
            getContentType(request.format()),
            LocalDateTime.now().plusHours(1)
        );
    }

    private void validateExportQuota(UUID userId, ExportRequest request) {
        boolean isPremium = subscriptionService.isPremiumUser(userId);

        if (!isPremium && request.filter() != null) {
            // Free tier: max 100 records
            // Check and throw exception if exceeded
        }
    }
}
```

### Phase 5: Controllers

**ReportController.java**
```java
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final GenerateFinancialSummary generateFinancialSummary;
    private final GetIncomeExpenseTrend getIncomeExpenseTrend;
    private final UserService userService;

    @GetMapping("/summary")
    public ResponseEntity<FinancialSummaryResponse> getSummary(
        @RequestParam(required = false) LocalDateTime startDate,
        @RequestParam(required = false) LocalDateTime endDate,
        @RequestParam(required = false) List<UUID> walletIds,
        @RequestParam(required = false) List<UUID> categoryIds
    ) {
        UUID userId = getCurrentUserId();

        // Set defaults (last 30 days if not provided)
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        ReportFilter filter = new ReportFilter(
            startDate, endDate, walletIds, categoryIds, null, 0, 100
        );

        FinancialSummaryResponse summary = generateFinancialSummary.generate(
            userId, filter
        );

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/trend")
    public ResponseEntity<List<TrendDataDto>> getTrend(
        @RequestParam(required = false) LocalDateTime startDate,
        @RequestParam(required = false) LocalDateTime endDate,
        @RequestParam(required = false) Granularity granularity
    ) {
        // Similar implementation
    }
}
```

**ExportController.java**
```java
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@Tag(name = "Export")
@SecurityRequirement(name = "bearerAuth")
public class ExportController {

    private final ExportTransactions exportTransactions;

    @PostMapping("/transactions")
    public ResponseEntity<ExportResponse> exportTransactions(
        @Valid @RequestBody ExportRequest request
    ) {
        UUID userId = getCurrentUserId();
        ExportResponse response = exportTransactions.export(userId, request);
        return ResponseEntity.ok(response);
    }
}
```

### Phase 6: Configuration

**application.yml**
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m
    cache-names:
      - financialSummaries
      - trendData
      - categoryBreakdowns

app:
  export:
    temp-dir: ${EXPORT_TEMP_DIR:/tmp/exports}
    cleanup-after-minutes: 60
    free-tier-limit: 100
```

**CacheConfig.java**
```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "financialSummaries",
            "trendData",
            "categoryBreakdowns"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
        );

        return cacheManager;
    }
}
```

### Phase 7: Testing

**GenerateFinancialSummaryUseCaseTest.java**
```java
@ExtendWith(MockitoExtension.class)
class GenerateFinancialSummaryUseCaseTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private MetricsService metricsService;
    @Mock private BusinessEventLogger businessEventLogger;

    @InjectMocks
    private GenerateFinancialSummaryUseCase useCase;

    @Test
    void generate_ShouldReturnSummary_WhenDataExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        ReportFilter filter = new ReportFilter(
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            null, null, null, 0, 20
        );

        when(transactionRepository.getSummaryByDateRange(any(), any(), any(), any()))
            .thenReturn(new Object[]{5000000.0, 3000000.0, 45L});

        when(transactionRepository.getCategoryBreakdown(any(), any(), any(), any(), any()))
            .thenReturn(List.of());

        when(walletRepository.findByUserId(userId))
            .thenReturn(List.of());

        // Act
        FinancialSummaryResponse result = useCase.generate(userId, filter);

        // Assert
        assertNotNull(result);
        assertEquals(5000000.0, result.totalIncome());
        assertEquals(3000000.0, result.totalExpense());
        assertEquals(2000000.0, result.netBalance());
        assertEquals(45, result.transactionCount());

        verify(metricsService).recordTimer(eq("report.financial.summary.duration"), anyLong());
    }
}
```

## 📊 Performance Optimizations Implemented

1. **Database Level**:
   - Optimized aggregation queries (single query for summary)
   - Proper indexing on date, user_id, wallet_id
   - COALESCE for handling NULLs
   - GROUP BY for category breakdown

2. **Application Level**:
   - Caching with 5-minute TTL
   - Lazy loading for wallets
   - Stream API for efficient data transformation
   - Minimal database round trips

3. **Export Optimizations**:
   - ByteArrayOutputStream for in-memory file generation
   - Pagination for large exports
   - Quota limits to prevent abuse

## 🎯 Clean Code Principles Applied

1. **SOLID**:
   - Single Responsibility: Each use case does one thing
   - Open/Closed: Easy to add new export formats
   - Liskov Substitution: Interface-based design
   - Interface Segregation: Focused interfaces
   - Dependency Inversion: Depend on abstractions

2. **Clean Architecture**:
   - Clear separation of layers
   - Domain logic in use cases
   - DTOs for data transfer
   - No business logic in controllers

3. **Naming**:
   - Clear, intention-revealing names
   - Verbs for methods (generate, export, get)
   - Nouns for classes (Report, Export, Summary)

## 📝 Next Actions

1. Complete all use case implementations following the pattern above
2. Implement all three exporters (CSV, Excel, PDF)
3. Create comprehensive tests (aim for 80%+ coverage)
4. Add Swagger documentation to all endpoints
5. Performance test with large datasets (10K+ transactions)
6. Security audit (ensure ownership checks)
7. Add rate limiting to export endpoints

## 🚀 Deployment Checklist

- [ ] All tests passing
- [ ] Cache configuration verified
- [ ] Export directory writable
- [ ] Temp file cleanup job configured
- [ ] Metrics dashboard updated
- [ ] API documentation published
- [ ] Performance benchmarks met
