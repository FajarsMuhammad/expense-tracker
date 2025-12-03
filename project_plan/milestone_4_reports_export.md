# Milestone 4: Reports & Export

## 📋 Overview

**Goal:** Implement comprehensive reporting and export functionality for transactions, debts, and financial summaries.

**Scope:**
- Summary reports (daily, weekly, monthly, custom period)
- Transaction reports with filtering and aggregation
- Debt reports and analytics
- Export functionality (CSV, Excel, PDF)
- Chart data endpoints for visualization

**Success Criteria:**
- Users can generate financial summaries for any time period
- Reports include income/expense breakdown by category
- Users can export data in multiple formats
- Free users have limited exports, premium users unlimited

---

## 🎯 Business Requirements

### Free Tier
- ✅ View summary reports (7 days, 30 days)
- ✅ Export CSV (max 100 transactions per export)
- ✅ Basic charts (income vs expense)
- ✅ Debt summary view

### Premium Tier
- ✅ Unlimited time range reports
- ✅ Export Excel with formatting
- ✅ Export PDF with branding
- ✅ Advanced analytics (trends, forecasts)
- ✅ Unlimited exports
- ✅ Category breakdown charts
- ✅ Multi-wallet consolidated reports

---

## 🏗️ Technical Architecture

### Backend Components
```
Controller Layer:
├── ReportController (summary reports)
├── ExportController (file exports)
└── AnalyticsController (chart data)

Use Case Layer:
├── GenerateFinancialSummary (interface + implementation)
├── GenerateTransactionReport (interface + implementation)
├── GenerateDebtReport (interface + implementation)
├── ExportTransactions (interface + implementation)
├── ExportDebts (interface + implementation)
├── ExportSummary (interface + implementation)
├── GetIncomeExpenseTrend (interface + implementation)
├── GetCategoryBreakdown (interface + implementation)
└── GetAnalyticsInsights (interface + implementation)

Repository Layer:
├── Custom queries with aggregation
├── Optimized date range queries
└── Category grouping queries

Export Formats:
├── CSV (OpenCSV - better annotation support)
├── Excel (Apache POI)
└── PDF (OpenPDF - LGPL license, free for commercial)
```

### Frontend Components
```
Pages:
├── ReportsPage.vue (main reports dashboard)
├── TransactionReportPage.vue
├── DebtReportPage.vue
└── AnalyticsPage.vue

Components:
├── SummaryCard.vue
├── DateRangePicker.vue
├── ReportFilter.vue
├── ExportButton.vue
├── ChartContainer.vue
├── IncomeExpenseChart.vue
├── CategoryBreakdownChart.vue
├── TrendChart.vue
└── DebtStatusChart.vue

Composables:
├── useReports.js
├── useExport.js
├── useAnalytics.js
└── useCharts.js
```

---

## 📦 Backend Implementation Checklist

### 1. Domain Layer - DTOs & Models

#### Report DTOs
- [x] Create `FinancialSummaryResponse` record
  - `totalIncome`, `totalExpense`, `netBalance`
  - `transactionCount`, `categoryBreakdown`
  - `walletBalances`, `period` (from/to dates)
- [x] Create `CategoryBreakdownDto` record
  - `categoryId`, `categoryName`, `categoryType`
  - `totalAmount`, `transactionCount`, `percentage`
- [ ] Create `TransactionReportResponse` record
  - List of transactions with aggregation data
  - Summary totals, averages
- [ ] Create `DebtReportResponse` record
  - Total payable/receivable
  - Overdue amounts
  - Payment history summary
- [x] Create `TrendDataDto` record
  - `date`, `income`, `expense`, `balance`
  - For time series charts
- [x] Create `ReportFilter` record
  - `startDate`, `endDate`
  - `walletIds`, `categoryIds`, `type`
  - Pagination parameters
- [x] Create `WalletBalanceDto` record
  - `walletId`, `walletName`, `currency`, `currentBalance`

#### Export DTOs
- [x] Create `ExportRequest` record
  - `format` (CSV/EXCEL/PDF)
  - `type` (TRANSACTIONS/DEBTS/SUMMARY)
  - `filter` (date range, categories, etc.)
- [x] Create `ExportResponse` record
  - `fileUrl` or `base64Data`
  - `fileName`, `fileSize`
  - `expiresAt` (for temporary download links)

#### Enums
- [x] Create `ExportFormat` enum (CSV, EXCEL, PDF)
- [x] Create `ExportType` enum (TRANSACTIONS, DEBTS, SUMMARY)
- [x] Create `Granularity` enum (DAILY, WEEKLY, MONTHLY)

### 2. Repository Layer - Custom Queries

#### TransactionRepository Extensions
- [x] Add `getSummaryByDateRange(userId, startDate, endDate, walletIds)`
  - Aggregate total income/expense
- [x] Add `getCategoryBreakdown(userId, startDate, endDate, type)`
  - GROUP BY category
- [x] Add `getTrendData(userId, startDate, endDate, granularity)`
  - Daily/weekly/monthly aggregation
- [ ] Add `getTopCategories(userId, startDate, endDate, type, limit)`
  - ORDER BY amount DESC
- [x] Add optimized indexes for report queries (V3 migration)
  - Index on (user_id, date DESC)
  - Index on (user_id, date DESC, type)
  - Index on (user_id, category_id, date DESC)
  - Index on (user_id, wallet_id, date DESC)
  - Composite index for complex filters
  - 13 total indexes created

#### DebtRepository Extensions
- [ ] Add `getDebtSummary(userId, type)`
  - Total open, partial, paid amounts
- [ ] Add `getOverdueSummary(userId)`
  - Total overdue amounts
- [ ] Add `getPaymentHistory(userId, startDate, endDate)`
  - All payments in date range

#### WalletRepository Extensions
- [ ] Add `getWalletBalanceSummary(userId)`
  - Current balance per wallet
- [ ] Add `getWalletBalanceHistory(userId, walletId, startDate, endDate)`
  - Balance over time

### 3. Use Case Layer - Business Logic

#### Report Use Cases

**GenerateFinancialSummary**
- [x] Create `GenerateFinancialSummary` interface
  - Method: `FinancialSummaryResponse generate(UUID userId, ReportFilter filter)`
- [x] Implement `GenerateFinancialSummaryUseCase`
  - Aggregate transactions by date range
  - Calculate income, expense, net balance
  - Group by category with percentages
  - Include wallet balances
  - Record metrics and log events
- [x] Add caching with Spring Cache (TTL: 5 minutes)

**GenerateTransactionReport**
- [ ] Create `GenerateTransactionReport` interface
  - Method: `Page<TransactionReportResponse> generate(UUID userId, ReportFilter filter)`
- [ ] Implement `GenerateTransactionReportUseCase`
  - List transactions with filtering
  - Include aggregation metrics
  - Support pagination

**GenerateDebtReport**
- [ ] Create `GenerateDebtReport` interface
  - Method: `DebtReportResponse generate(UUID userId, ReportFilter filter)`
- [ ] Implement `GenerateDebtReportUseCase`
  - Summary of debts by status
  - Overdue analysis
  - Payment history

**GetCategoryBreakdown**
- [ ] Create `GetCategoryBreakdown` interface
  - Method: `List<CategoryBreakdownDto> get(UUID userId, ReportFilter filter)`
- [ ] Implement `GetCategoryBreakdownUseCase`
  - Group by category with percentages
  - Return top N categories by amount
  - Calculate percentage of total

#### Export Use Cases

**ExportTransactions**
- [x] Create `ExportTransactions` interface
  - Method: `ExportResponse export(UUID userId, ExportRequest request)`
- [x] Implement `ExportTransactionsUseCase`
  - Validate export quota (free vs premium)
  - Delegate to format-specific exporters
  - Return file or download URL
  - Record export metrics

**ExportDebts**
- [ ] Create `ExportDebts` interface
  - Method: `ExportResponse export(UUID userId, ExportRequest request)`
- [ ] Implement `ExportDebtsUseCase`
  - Similar to ExportTransactions
  - Handle debt-specific formatting

**ExportSummary**
- [ ] Create `ExportSummary` interface
  - Method: `ExportResponse export(UUID userId, ExportRequest request)`
- [ ] Implement `ExportSummaryUseCase`
  - Export financial summary as PDF/Excel
  - Include charts and graphs
  - Premium feature check

#### Export Helper Services (Not Use Cases)

**CsvExporter** (Internal Service)
- [x] Implement `exportTransactionsToCsv(transactions)`
  - Use OpenCSV with clean API
  - Indonesian localization
  - UTF-8 encoding
  - Safe null handling

**ExcelExporter** (Internal Service)
- [x] Implement `exportTransactionsToExcel(transactions)`
  - Use Apache POI
  - Professional styling (bold headers, borders)
  - Currency formatting
  - Auto-sized columns
  - Summary row with totals

**PdfExporter** (Internal Service)
- [x] Implement `exportTransactionsToPdf(transactions)`
  - Use OpenPDF (LGPL license)
  - Professional layout with title and date
  - Summary statistics section
  - Styled table with data
  - Indonesian localization

**ExportCleanupService** (Scheduled Service)
- [ ] Implement temporary file cleanup
  - Store exports in temp directory
  - Clean up files after 1 hour
  - Scheduled task with @Scheduled

#### Analytics Use Cases

**GetIncomeExpenseTrend**
- [x] Create `GetIncomeExpenseTrend` interface
  - Method: `List<TrendDataDto> get(UUID userId, ReportFilter filter, Granularity granularity)`
- [x] Implement `GetIncomeExpenseTrendUseCase`
  - Daily/weekly/monthly data points
  - Fill gaps with zero values for smooth charts
  - Apply granularity aggregation
  - Cached with 5-minute TTL

**GetCategoryDistribution**
- [ ] Create `GetCategoryDistribution` interface
  - Method: `List<CategoryBreakdownDto> get(UUID userId, ReportFilter filter)`
- [ ] Implement `GetCategoryDistributionUseCase`
  - Percentage breakdown by category
  - For pie/donut charts

**GetWalletComparison**
- [ ] Create `GetWalletComparison` interface
  - Method: `List<WalletComparisonDto> get(UUID userId, ReportFilter filter)`
- [ ] Implement `GetWalletComparisonUseCase`
  - Compare performance across wallets

**GetAnalyticsInsights** (Premium)
- [ ] Create `GetAnalyticsInsights` interface
  - Method: `List<InsightDto> get(UUID userId, ReportFilter filter)`
- [ ] Implement `GetAnalyticsInsightsUseCase`
  - Calculate month-over-month growth
  - Identify spending trends
  - Simple forecast projection

### 4. Controller Layer - REST Endpoints

#### ReportController (`/api/v1/reports`)
- [x] `GET /summary` - Get financial summary
  - Query params: `startDate`, `endDate`, `walletIds`, `categoryIds`
  - Returns: `FinancialSummaryResponse`
  - Defaults to last 30 days if no dates provided
- [ ] `GET /transactions` - Get transaction report
  - Query params: full filter support + pagination
  - Returns: `Page<TransactionReportResponse>`
- [ ] `GET /debts` - Get debt report
  - Query params: `type`, `status`, `includePayments`
  - Returns: `DebtReportResponse`
- [ ] `GET /category-breakdown` - Category analysis
  - Query params: `startDate`, `endDate`, `type`
  - Returns: `List<CategoryBreakdownDto>`
- [x] `GET /trend` - Trend data for charts
  - Query params: `startDate`, `endDate`, `granularity`, `walletIds`
  - Returns: `List<TrendDataDto>`
  - Defaults to last 30 days, DAILY granularity
- [x] Add Swagger documentation for all endpoints
- [x] Use UserContext utility for authentication (eliminates boilerplate)
- [x] Add validation for date ranges (DateRangeValidator)
- [x] Add subscription tier checks (SubscriptionService)

#### ExportController (`/api/v1/export`)
- [x] `POST /transactions` - Export transactions
  - Body: `ExportRequest` with format and filters
  - Returns: `ExportResponse` with base64 content
  - Supports CSV, Excel, PDF formats
- [ ] `POST /debts` - Export debts
  - Body: `ExportRequest`
  - Returns: File download
- [ ] `POST /summary` - Export summary report
  - Body: `ExportRequest`
  - Returns: PDF or Excel file
- [x] Implement export quota checking (free tier: 100 records)
- [x] Use UserContext utility for authentication
- [x] Add rate limiting (max 10 exports per minute via RateLimiter)
- [x] Premium format restrictions (PDF/Excel require premium)
- [ ] Add correlation ID for tracking

#### AnalyticsController (`/api/v1/analytics`)
- [ ] `GET /overview` - Dashboard overview
  - Quick summary of key metrics
- [ ] `GET /charts/income-expense` - Chart data
  - Returns data formatted for frontend charts
- [ ] `GET /charts/category-distribution` - Category pie chart
- [ ] `GET /charts/wallet-comparison` - Wallet comparison
- [ ] `GET /insights` (Premium) - AI-like insights
  - "You spent 30% more this month"
  - "Top spending category: Food"

### 5. Infrastructure & Configuration

#### Dependencies (build.gradle)
- [x] Add OpenCSV 5.9 (better annotation support than Apache Commons CSV)
  ```gradle
  implementation 'com.opencsv:opencsv:5.9'
  ```
- [x] Add Apache POI 5.2.5 (Excel)
  ```gradle
  implementation 'org.apache.poi:poi:5.2.5'
  implementation 'org.apache.poi:poi-ooxml:5.2.5'
  ```
- [x] Add OpenPDF 2.0.2 (LGPL - free for commercial use)
  ```gradle
  implementation 'com.github.librepdf:openpdf:2.0.2'
  ```
- [x] Add Spring Cache + Caffeine for report caching
  ```gradle
  implementation 'org.springframework.boot:spring-boot-starter-cache'
  implementation 'com.github.ben-manes.caffeine:caffeine'
  ```

#### Configuration
- [ ] Configure export directory in application.yml
  ```yaml
  app:
    export:
      temp-dir: /tmp/exports
      cleanup-after-minutes: 60
      free-tier-limit: 100
  ```
- [x] Enable caching configuration (CacheConfig.java)
  - Caffeine cache manager
  - 5-minute TTL for reports
  - Max 1000 entries per cache
- [ ] Add async configuration for long-running exports

#### Common Utilities
- [x] Create `UserContext` utility component
  - Centralizes authentication lookup
  - Eliminates boilerplate `getCurrentUserId()` in controllers
  - Methods: `getCurrentUserId()`, `getCurrentUser()`, `isAuthenticated()`

### 6. Testing

#### Unit Tests
- [ ] `GenerateFinancialSummaryUseCaseTest` - Test summary calculations
  - Test date range filtering
  - Test category aggregation
  - Test empty data handling
  - Test wallet balance calculations
- [ ] `ExportTransactionsUseCaseTest` - Test export functionality
  - Test quota validation (free vs premium)
  - Test record limit enforcement
  - Test export delegation
  - Test metrics recording
- [ ] `GetIncomeExpenseTrendUseCaseTest` - Test trend calculations
  - Test daily/weekly/monthly granularity
  - Test running balance calculation
  - Test empty data handling
- [ ] `CsvExporterTest` - Test CSV format generation
  - Test header formatting
  - Test data row formatting
  - Test special character handling
- [ ] `ExcelExporterTest` - Test Excel format generation
  - Test multiple sheets
  - Test cell formatting
  - Test formulas
- [ ] `PdfExporterTest` - Test PDF format generation
  - Test layout generation
  - Test chart embedding
  - Test branding (premium)

#### Integration Tests
- [ ] `ReportControllerTest` - Test endpoints
  - Test summary endpoint with various filters
  - Test pagination
  - Test unauthorized access
- [ ] `ExportControllerTest` - Test export endpoints
  - Test file download
  - Test export limits
  - Test format validation
- [ ] Test with large datasets (performance testing)
  - 10,000+ transactions
  - Measure query performance
  - Validate export time

---

## 🎨 Frontend Implementation Checklist

### 1. Setup & Dependencies

#### Package Installation
- [ ] Install chart library
  ```bash
  npm install chart.js vue-chartjs
  # OR
  npm install echarts vue-echarts
  # OR
  npm install apexcharts vue3-apexcharts
  ```
- [ ] Install date picker
  ```bash
  npm install @vuepic/vue-datepicker
  ```
- [ ] Install file-saver for downloads
  ```bash
  npm install file-saver
  ```
- [ ] Install currency formatting
  ```bash
  npm install vue-currency-input
  ```

### 2. Pinia Store - Reports Module

#### `stores/reports.js`
- [ ] Create reports store
  ```javascript
  const useReportsStore = defineStore('reports', {
    state: () => ({
      summary: null,
      transactionReport: null,
      debtReport: null,
      categoryBreakdown: [],
      trendData: [],
      loading: false,
      error: null,
      filters: {
        startDate: null,
        endDate: null,
        walletIds: [],
        categoryIds: [],
        type: null
      }
    })
  })
  ```
- [ ] Add actions:
  - `fetchSummary(filters)`
  - `fetchTransactionReport(filters)`
  - `fetchDebtReport(filters)`
  - `fetchCategoryBreakdown(filters)`
  - `fetchTrendData(filters)`
  - `updateFilters(newFilters)`
  - `resetFilters()`
- [ ] Add getters:
  - `hasData`
  - `totalIncome`
  - `totalExpense`
  - `netBalance`
  - `topCategories`

#### `stores/export.js`
- [ ] Create export store
  ```javascript
  const useExportStore = defineStore('export', {
    state: () => ({
      exporting: false,
      exportProgress: 0,
      lastExport: null
    })
  })
  ```
- [ ] Add actions:
  - `exportTransactions(format, filters)`
  - `exportDebts(format, filters)`
  - `exportSummary(format, filters)`
  - `checkExportQuota()`
- [ ] Handle file download
- [ ] Handle export errors

### 3. Composables

#### `composables/useReports.js`
- [ ] Create composable
  ```javascript
  export function useReports() {
    const store = useReportsStore()

    const loadSummary = async (filters) => {
      // Load summary data
    }

    const exportData = async (format) => {
      // Trigger export
    }

    return {
      summary: computed(() => store.summary),
      loading: computed(() => store.loading),
      loadSummary,
      exportData
    }
  }
  ```

#### `composables/useCharts.js`
- [ ] Create chart configuration generator
  ```javascript
  export function useCharts() {
    const generateIncomeExpenseChartConfig = (data) => {
      // Return Chart.js config
    }

    const generateCategoryPieConfig = (data) => {
      // Return pie chart config
    }

    return {
      generateIncomeExpenseChartConfig,
      generateCategoryPieConfig
    }
  }
  ```

#### `composables/useExport.js`
- [ ] Create export utilities
  ```javascript
  export function useExport() {
    const downloadFile = (blob, filename) => {
      // Use FileSaver.js
    }

    const checkQuota = async () => {
      // Check if user can export
    }

    return { downloadFile, checkQuota }
  }
  ```

### 4. Pages

#### `pages/ReportsPage.vue`
- [ ] Create main reports page layout
  - Header with title and date range picker
  - Quick filters (7d, 30d, this month, custom)
  - Summary cards section
  - Charts section
  - Export button
- [ ] Implement date range selection
  - Preset buttons (Today, 7d, 30d, This Month, Last Month)
  - Custom date range picker
  - Update URL query params on change
- [ ] Display financial summary
  - Total income card
  - Total expense card
  - Net balance card
  - Transaction count
- [ ] Add loading states
- [ ] Add empty states
- [ ] Add error handling
- [ ] Responsive design (mobile-first)

#### `pages/TransactionReportPage.vue`
- [ ] Create detailed transaction report view
  - Advanced filters sidebar
  - Transaction list with grouping
  - Export options
  - Pagination
- [ ] Implement filters:
  - Date range
  - Wallet selection (multi-select)
  - Category selection (multi-select)
  - Type (Income/Expense)
  - Amount range
- [ ] Group transactions by date
- [ ] Show daily/weekly totals
- [ ] Add download button for each format

#### `pages/DebtReportPage.vue`
- [ ] Create debt analytics page
  - Summary cards (total payable, receivable, overdue)
  - Debt list with status indicators
  - Payment history timeline
  - Export debt list
- [ ] Filter by debt type (Payable/Receivable)
- [ ] Filter by status (Open/Partial/Paid)
- [ ] Show overdue highlights
- [ ] Payment history chart

#### `pages/AnalyticsPage.vue` (Premium)
- [ ] Create advanced analytics dashboard
  - Multi-chart layout
  - Insights section
  - Forecast section
  - Trend analysis
- [ ] Add premium gate
  - Show preview for free users
  - Upgrade CTA
- [ ] Multiple chart types:
  - Line chart (trend over time)
  - Pie chart (category distribution)
  - Bar chart (monthly comparison)
  - Stacked area chart (income vs expense)

### 5. Components

#### Report Components

**`SummaryCard.vue`**
- [ ] Props: `title`, `value`, `icon`, `trend`, `loading`
- [ ] Display formatted currency
- [ ] Show trend indicator (up/down arrow)
- [ ] Color coding (green for income, red for expense)
- [ ] Skeleton loading state

**`DateRangePicker.vue`**
- [ ] Props: `modelValue`, `presets`, `maxRange`
- [ ] Emit: `update:modelValue`
- [ ] Quick preset buttons
- [ ] Calendar popup
- [ ] Validation (end date >= start date)
- [ ] Mobile-friendly

**`ReportFilter.vue`**
- [ ] Multi-select for wallets
- [ ] Multi-select for categories
- [ ] Type toggle (Income/Expense/Both)
- [ ] Amount range slider
- [ ] Apply/Reset buttons
- [ ] Collapsible on mobile

**`ExportButton.vue`**
- [ ] Props: `type`, `filters`, `disabled`
- [ ] Dropdown menu for format selection
  - CSV
  - Excel
  - PDF (Premium)
- [ ] Loading state during export
- [ ] Show export quota for free users
- [ ] Upgrade prompt if quota exceeded

#### Chart Components

**`IncomeExpenseChart.vue`**
- [ ] Props: `data`, `loading`, `height`
- [ ] Line chart with two series
- [ ] Responsive
- [ ] Tooltip formatting
- [ ] Legend toggle
- [ ] Empty state

**`CategoryBreakdownChart.vue`**
- [ ] Props: `data`, `type` (income/expense)
- [ ] Pie or donut chart
- [ ] Color coding by category
- [ ] Percentage labels
- [ ] Click to drill down
- [ ] Legend with category names

**`TrendChart.vue`**
- [ ] Props: `data`, `granularity`
- [ ] Area chart or line chart
- [ ] Zoom controls
- [ ] Crosshair tooltip
- [ ] Export chart as image

**`DebtStatusChart.vue`**
- [ ] Props: `debtData`
- [ ] Stacked bar chart
- [ ] Show open/partial/paid
- [ ] Color-coded by status
- [ ] Click for details

**`ChartContainer.vue`**
- [ ] Wrapper component for all charts
- [ ] Props: `title`, `subtitle`, `loading`
- [ ] Header with title and actions
- [ ] Loading skeleton
- [ ] Error state
- [ ] Full-screen toggle

### 6. Utilities & Helpers

#### `utils/reportFormatter.js`
- [ ] `formatCurrency(amount, currency)` - Format numbers
- [ ] `formatPercentage(value)` - Format percentages
- [ ] `formatDateRange(start, end)` - Format date ranges
- [ ] `calculatePercentageChange(current, previous)` - Growth calc
- [ ] `groupTransactionsByDate(transactions)` - Group logic

#### `utils/chartColors.js`
- [ ] Define color palette for charts
- [ ] Category color mapping
- [ ] Income/expense colors
- [ ] Status colors (open/partial/paid)

#### `utils/exportUtils.js`
- [ ] `triggerDownload(url, filename)` - Download helper
- [ ] `generateExportFilename(type, format, date)` - Name generator
- [ ] `checkBrowserSupport()` - Feature detection

### 7. Routing

#### Update `router/index.js`
- [ ] Add reports routes
  ```javascript
  {
    path: '/reports',
    component: () => import('@/layouts/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'reports',
        component: () => import('@/pages/ReportsPage.vue')
      },
      {
        path: 'transactions',
        name: 'transaction-report',
        component: () => import('@/pages/TransactionReportPage.vue')
      },
      {
        path: 'debts',
        name: 'debt-report',
        component: () => import('@/pages/DebtReportPage.vue')
      },
      {
        path: 'analytics',
        name: 'analytics',
        component: () => import('@/pages/AnalyticsPage.vue'),
        meta: { requiresPremium: true }
      }
    ]
  }
  ```

### 8. UI/UX Enhancements

#### Mobile Optimization
- [ ] Responsive charts (scale on mobile)
- [ ] Bottom sheet for filters
- [ ] Horizontal scroll for date presets
- [ ] Simplified export menu
- [ ] Touch-friendly controls

#### Loading States
- [ ] Skeleton screens for reports
- [ ] Chart loading animation
- [ ] Export progress indicator
- [ ] Shimmer effect for cards

#### Empty States
- [ ] "No transactions in this period" message
- [ ] "Add transaction" CTA
- [ ] Illustration
- [ ] Helpful text

#### Error Handling
- [ ] Network error toast
- [ ] Export failed message
- [ ] Retry button
- [ ] Fallback to cached data

### 9. Testing

#### Component Tests (Vitest)
- [ ] `SummaryCard.test.js` - Test rendering and formatting
- [ ] `DateRangePicker.test.js` - Test date selection
- [ ] `ReportFilter.test.js` - Test filter changes
- [ ] `ExportButton.test.js` - Test export trigger
- [ ] `IncomeExpenseChart.test.js` - Test chart rendering

#### E2E Tests (Cypress)
- [ ] Test report page load
- [ ] Test date range selection
- [ ] Test filter application
- [ ] Test export CSV download
- [ ] Test navigation between report pages
- [ ] Test premium gate for analytics

---

## 🔧 Configuration & Setup

### Environment Variables
```env
# Backend
EXPORT_TEMP_DIR=/tmp/exports
EXPORT_CLEANUP_MINUTES=60
FREE_EXPORT_LIMIT=100
PREMIUM_EXPORT_LIMIT=10000

# Frontend
VITE_API_URL=http://localhost:8081/api/v1
VITE_MAX_DATE_RANGE_DAYS=365
```

### Feature Flags
```javascript
// config/features.js
export const features = {
  reports: {
    pdf_export: import.meta.env.VITE_ENABLE_PDF_EXPORT === 'true',
    advanced_analytics: import.meta.env.VITE_ENABLE_ANALYTICS === 'true',
    forecast: import.meta.env.VITE_ENABLE_FORECAST === 'true'
  }
}
```

---

## 📊 Success Metrics

### Backend Performance
- [ ] Report generation < 500ms for 90% of requests
- [ ] CSV export < 2s for 1000 records
- [ ] Excel export < 5s for 1000 records
- [ ] PDF export < 10s for complex reports
- [ ] Database queries optimized (< 100ms)

### Frontend Performance
- [ ] Page load < 1s
- [ ] Chart rendering < 500ms
- [ ] Filter application instant feedback
- [ ] Export download starts < 3s

### User Experience
- [ ] Zero failed exports due to bugs
- [ ] Clear loading feedback
- [ ] Helpful error messages
- [ ] Mobile-responsive on all screen sizes

---

## 🚀 Deployment Checklist

- [ ] Database indexes added for report queries
- [ ] Export directory configured and writable
- [ ] Cleanup cron job scheduled
- [ ] Rate limiting configured
- [ ] Cache configured and tested
- [ ] File size limits set
- [ ] Premium feature gates tested
- [ ] Error tracking configured (Sentry)
- [ ] Performance monitoring enabled
- [ ] API documentation updated
- [ ] User documentation written
- [ ] Export samples tested

---

## 📚 Documentation

### API Documentation
- [ ] Update Swagger/OpenAPI specs
- [ ] Add example requests/responses
- [ ] Document query parameters
- [ ] Document rate limits
- [ ] Document file formats

### User Documentation
- [ ] How to generate reports guide
- [ ] Export formats explained
- [ ] Premium features list
- [ ] Troubleshooting guide
- [ ] FAQ section

---

## 🎯 Next Steps After Milestone 4

After completing reports and export:
1. **Milestone 5:** Subscription & Payment Integration
2. **Milestone 6:** Backup & Cloud Sync
3. **Milestone 7:** QA, Security Audit & Deployment

---

## 📝 Notes

- **Chart Library Recommendation:** ApexCharts for rich features, Chart.js for simplicity
- **CSV Library:** OpenCSV recommended over Apache Commons CSV for better features
- **PDF Library:** OpenPDF (LGPL license - free for commercial use)
- **Excel Library:** Apache POI (Apache 2.0 license - truly free)
- **Caching Strategy:** Cache report data for 5 minutes to reduce database load
- **Export Queue:** Consider implementing job queue for large exports (future enhancement)
- **Localization:** Keep in mind Indonesian number formatting (1.000.000,00)

---

**Estimated Time:** 3-4 weeks
- Backend: 1.5-2 weeks
- Frontend: 1.5-2 weeks
- Testing & Polish: 3-5 days
