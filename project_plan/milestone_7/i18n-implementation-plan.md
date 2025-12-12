# Multi-Language Support Implementation Plan
**Milestone 7: Internationalization (i18n)**

**Languages**: Indonesian (Default) & English
**Library**: vue-i18n v9
**Status**: Phase 1-3 COMPLETED ✅ | Phase 4 Pending (Backend Integration)

## 🎉 Implementation Status Summary

### ✅ Completed (Phase 1-3)
- **Infrastructure**: vue-i18n setup, locale store, language switcher
- **Core Components**: AppHeader, AppSidebar, ThemeToggle, AppModal
- **Authentication**: Login, Register pages
- **Dashboard Module**: Main view, Recent Transactions, Wallet Filter
- **Wallets Module**: List, Detail, Create, Edit, Form, Card
- **Transactions Module**: List, Create, Edit, Form, Filters
- **Categories Module**: List, Form
- **Debts Module**: List, Detail, Form, Card, Filters, Summary, Payment History
- **Reports Module**: Main page, Summary cards, Category breakdown, Trend charts
- **Profile Module**: Profile view, Profile form, Subscription details
- **Premium/Subscription Module**: PremiumView, TrialBanner, UpgradeModal

### ⏳ Pending
- **Backend Integration**: Accept-Language header support, localized API responses

### 📊 Coverage
- **Frontend i18n**: 100% ✅ (39 components translated)
- **Backend i18n**: 0% (not yet implemented)
- **Total Translation Keys**: 500+ (Indonesian & English)

---

## 📋 Table of Contents
1. [Overview](#overview)
2. [Phase 1: Infrastructure Setup](#phase-1-infrastructure-setup-completed)
3. [Phase 2: Core Translations](#phase-2-core-translations)
4. [Phase 3: Feature Module Translations](#phase-3-feature-module-translations)
5. [Phase 4: Backend Integration](#phase-4-backend-integration)
6. [Backend Preparation Requirements](#backend-preparation-requirements)
7. [Testing Strategy](#testing-strategy)

---

## Overview

### Objectives
- Support Indonesian (default) and English languages
- Seamless language switching via user dropdown menu
- Hybrid currency format (keep symbols, adapt number formatting)
- LocalStorage persistence for user preference
- Prepare Accept-Language header for backend integration
- Minimal bugs through professional architecture

### Technical Stack
- **Frontend**: Vue 3.5.24 + Composition API
- **i18n Library**: vue-i18n v9
- **State Management**: Pinia
- **Build Tool**: Vite with @intlify/unplugin-vue-i18n

### Key Decisions
- **Default Locale**: Indonesian (id) - No browser detection
- **Language Switcher**: User dropdown menu (between Settings and Logout)
- **Currency Format**: Hybrid - Keep currency symbols (Rp, $), adapt number separators
- **Fallback**: Always fallback to Indonesian for missing keys
- **Organization**: Feature-based translation files

---

## Phase 1: Infrastructure Setup ✅ COMPLETED

### 1.1 Dependencies Installed
```bash
npm install vue-i18n@9
npm install -D @intlify/unplugin-vue-i18n
```

### 1.2 Configuration Files Created

**`/src/i18n/index.js`**
- Main vue-i18n configuration
- Composition API mode enabled
- LocalStorage integration
- Missing key warnings in development
- Fallback to Indonesian

**`/src/i18n/number-formats.js`**
- Indonesian: Currency style for IDR formatting
- English: Decimal style with grouping (hybrid approach)

**`/src/i18n/datetime-formats.js`**
- Multiple format options: short, long, monthYear, time
- Locale-specific date formatting

### 1.3 State Management

**`/src/stores/locale.js`**
- Pinia store for locale state
- Functions: setLocale(), getLocaleLabel(), getLocaleFlag()
- LocalStorage persistence
- Document lang attribute update
- Day.js locale sync (if available)

### 1.4 Translation Structure

```
src/locales/
├── id/
│   ├── index.js          # Aggregator
│   ├── common.json       # Common UI elements ✅
│   └── navigation.json   # Navigation menus ✅
└── en/
    ├── index.js          # Aggregator
    ├── common.json       # Common UI elements ✅
    └── navigation.json   # Navigation menus ✅
```

### 1.5 Build Configuration

**`vite.config.js`**
- VueI18nPlugin configured
- Tree-shaking enabled
- Includes all locale files from src/locales/**

**`src/main.js`**
- i18n plugin registered

### 1.6 UI Implementation

**`src/components/layout/AppHeader.vue`**
- Language switcher added to user dropdown
- Position: Between Settings and Logout
- Two toggle buttons: Bahasa Indonesia | English
- Active locale highlighted with gradient
- Menu items (Profile, Settings, Logout) now use i18n

---

## Phase 2: Core Translations ✅ COMPLETED

### 2.1 Layout Components (Priority: HIGH) ✅

**`src/components/layout/AppSidebar.vue`**
- [x] Sidebar navigation items
- [x] Wallet selector dropdown
- [x] Balance display labels

**Translation Keys Required**:
```json
{
  "navigation.sidebar": {
    "dashboard": "Dashboard / Dasbor",
    "wallets": "Wallets / Dompet",
    "transactions": "Transactions / Transaksi",
    "categories": "Categories / Kategori",
    "debts": "Debts / Utang Piutang",
    "reports": "Reports / Laporan",
    "premium": "Premium"
  }
}
```

### 2.2 Common Components (Priority: HIGH) ✅

**Components to Translate**:
- [x] `ThemeToggle.vue` - Theme labels
- [x] `AppModal.vue` - Modal action buttons
- [x] Common translations (buttons, actions, states, time)

**Translation Keys Required**:
```json
{
  "common.theme": {
    "light": "Light / Terang",
    "dark": "Dark / Gelap",
    "toggle": "Toggle Theme / Ganti Tema"
  },
  "common.modal": {
    "close": "Close / Tutup",
    "confirm": "Confirm / Konfirmasi",
    "cancel": "Cancel / Batal"
  }
}
```

### 2.3 Authentication Pages (Priority: HIGH) ✅

- [x] `LoginView.vue`
- [x] `RegisterView.vue`

**Translation Keys Required**:
```json
{
  "auth.login": {
    "title": "Login / Masuk",
    "email": "Email",
    "password": "Password / Kata Sandi",
    "rememberMe": "Remember Me / Ingat Saya",
    "forgotPassword": "Forgot Password? / Lupa Kata Sandi?",
    "submit": "Login / Masuk",
    "noAccount": "Don't have an account? / Belum punya akun?",
    "register": "Register / Daftar"
  }
}
```

---

## Phase 3: Feature Module Translations ✅ COMPLETED

### 3.1 Dashboard Module (Priority: HIGH) ✅

**Files**:
- [x] `DashboardView.vue`
- [x] `RecentTransactions.vue`
- [x] `WalletFilter.vue`

**Translation Keys**:
```json
{
  "dashboard": {
    "title": "Dashboard / Dasbor",
    "totalBalance": "Total Balance / Total Saldo",
    "income": "Income / Pemasukan",
    "expense": "Expense / Pengeluaran",
    "recentTransactions": "Recent Transactions / Transaksi Terkini",
    "viewAll": "View All / Lihat Semua",
    "thisMonth": "This Month / Bulan Ini"
  }
}
```

### 3.2 Wallet Module (Priority: HIGH) ✅

**Files**:
- [x] `WalletListView.vue`
- [x] `WalletDetailView.vue`
- [x] `WalletCreateView.vue`
- [x] `WalletEditView.vue`
- [x] `WalletCard.vue`
- [x] `WalletForm.vue`

**Translation Keys**:
```json
{
  "wallet": {
    "title": "Wallets / Dompet",
    "addWallet": "Add Wallet / Tambah Dompet",
    "editWallet": "Edit Wallet / Edit Dompet",
    "deleteWallet": "Delete Wallet / Hapus Dompet",
    "walletName": "Wallet Name / Nama Dompet",
    "initialBalance": "Initial Balance / Saldo Awal",
    "currentBalance": "Current Balance / Saldo Saat Ini",
    "currency": "Currency / Mata Uang",
    "color": "Color / Warna",
    "icon": "Icon / Ikon"
  }
}
```

### 3.3 Transaction Module (Priority: HIGH) ✅

**Files**:
- [x] `TransactionListView.vue`
- [x] `TransactionCreateView.vue`
- [x] `TransactionEditView.vue`
- [x] `TransactionList.vue`
- [x] `TransactionFilters.vue`
- [x] `TransactionForm.vue`

**Translation Keys**:
```json
{
  "transaction": {
    "title": "Transactions / Transaksi",
    "addTransaction": "Add Transaction / Tambah Transaksi",
    "editTransaction": "Edit Transaction / Edit Transaksi",
    "deleteTransaction": "Delete Transaction / Hapus Transaksi",
    "type": "Type / Jenis",
    "income": "Income / Pemasukan",
    "expense": "Expense / Pengeluaran",
    "amount": "Amount / Jumlah",
    "category": "Category / Kategori",
    "wallet": "Wallet / Dompet",
    "date": "Date / Tanggal",
    "description": "Description / Keterangan",
    "notes": "Notes / Catatan"
  }
}
```

### 3.4 Category Module (Priority: MEDIUM) ✅

**Files**:
- [x] `CategoryView.vue`
- [x] `CategoryForm.vue`
- [x] `CategoryList.vue`

**Translation Keys**:
```json
{
  "category": {
    "title": "Categories / Kategori",
    "addCategory": "Add Category / Tambah Kategori",
    "editCategory": "Edit Category / Edit Kategori",
    "deleteCategory": "Delete Category / Hapus Kategori",
    "categoryName": "Category Name / Nama Kategori",
    "type": "Type / Jenis",
    "color": "Color / Warna",
    "icon": "Icon / Ikon"
  }
}
```

### 3.5 Debt Module (Priority: MEDIUM) ✅

**Files**:
- [x] `DebtListView.vue`
- [x] `DebtDetailView.vue`
- [x] `DebtForm.vue`
- [x] `PaymentHistory.vue`
- [x] `DebtCard.vue`
- [x] `DebtFilters.vue`
- [x] `DebtSummary.vue`

**Translation Keys**:
```json
{
  "debt": {
    "title": "Debts / Utang Piutang",
    "addDebt": "Add Debt / Tambah Utang Piutang",
    "editDebt": "Edit Debt / Edit Utang Piutang",
    "deleteDebt": "Delete Debt / Hapus Utang Piutang",
    "type": "Type / Jenis",
    "receivable": "Receivable / Piutang",
    "payable": "Payable / Utang",
    "debtor": "Debtor / Peminjam",
    "creditor": "Creditor / Pemberi Pinjaman",
    "totalAmount": "Total Amount / Total Jumlah",
    "paidAmount": "Paid Amount / Jumlah Dibayar",
    "remainingAmount": "Remaining Amount / Sisa",
    "dueDate": "Due Date / Jatuh Tempo",
    "status": "Status",
    "addPayment": "Add Payment / Tambah Pembayaran",
    "paymentHistory": "Payment History / Riwayat Pembayaran"
  }
}
```

### 3.6 Report Module (Priority: MEDIUM) ✅

**Files**:
- [x] `ReportsPage.vue`
- [x] Report components (SummaryCard, DateRangePicker, ExportButton)

**Translation Keys**:
```json
{
  "report": {
    "title": "Reports / Laporan",
    "incomeVsExpense": "Income vs Expense / Pemasukan vs Pengeluaran",
    "categoryBreakdown": "Category Breakdown / Rincian Kategori",
    "period": "Period / Periode",
    "daily": "Daily / Harian",
    "weekly": "Weekly / Mingguan",
    "monthly": "Monthly / Bulanan",
    "yearly": "Yearly / Tahunan",
    "export": "Export / Ekspor"
  }
}
```

### 3.7 Premium/Subscription Module (Priority: MEDIUM) ✅

**Files**:
- [x] `PremiumView.vue` - Full page with pricing, features, comparison table
- [x] `TrialBanner.vue` - Trial notification banner
- [x] `UpgradeModal.vue` - Modal for premium feature prompts

**Translation Keys** (50+ keys added):
```json
{
  "premium": {
    "title": "Premium",
    "status": {
      "premiumActive": "Premium Active / Premium Aktif",
      "trialActive": "Trial Period Active / Periode Uji Coba Aktif",
      "validUntil": "Valid until: {date} / Berlaku hingga: {date}",
      "daysRemaining": "{count} days remaining / {count} hari tersisa",
      "subscribeNow": "Subscribe Now - IDR 25,000/month"
    },
    "hero": {
      "title": "Upgrade to Premium / Upgrade ke Premium",
      "subtitle": "Unlock unlimited features...",
      "price": "IDR 25,000",
      "perMonth": "per month / per bulan"
    },
    "trialBanner": {
      "title": "Premium Trial Active / Uji Coba Premium Aktif",
      "enjoying": "Enjoying unlimited wallets, reports & Excel/PDF export!",
      "progressUsed": "{percent}% of trial used"
    },
    "comparison": {
      "title": "Feature Comparison / Perbandingan Fitur",
      "free": "Free / Gratis",
      "premium": "Premium",
      "wallets": "Wallets / Dompet",
      "unlimited": "Unlimited / Tidak Terbatas"
    },
    "features": {
      "unlimitedWallets": { "title": "...", "description": "..." },
      "advancedAnalytics": { "title": "...", "description": "..." },
      "unlimitedCategories": { "title": "...", "description": "..." },
      "prioritySupport": { "title": "...", "description": "..." },
      "dataExport": { "title": "...", "description": "..." },
      "cloudBackup": { "title": "...", "description": "..." }
    },
    "upgradeModal": {
      "defaultTitle": "Premium Feature / Fitur Premium",
      "defaultMessage": "This feature is available for Premium users only.",
      "upgradeButton": "Upgrade to Premium",
      "maybeLater": "Maybe Later / Nanti Saja"
    }
  }
}
```

### 3.8 Profile Module (Priority: LOW) ✅

**Files**:
- [x] `ProfileView.vue`
- [x] `ProfileForm.vue`
- [x] `SubscriptionBadge.vue`

**Translation Keys**:
```json
{
  "profile": {
    "title": "Profile / Profil",
    "editProfile": "Edit Profile / Edit Profil",
    "name": "Name / Nama",
    "email": "Email",
    "phone": "Phone / Telepon",
    "avatar": "Avatar / Foto Profil",
    "changePassword": "Change Password / Ubah Kata Sandi",
    "currentPassword": "Current Password / Kata Sandi Saat Ini",
    "newPassword": "New Password / Kata Sandi Baru",
    "confirmPassword": "Confirm Password / Konfirmasi Kata Sandi"
  }
}
```

---

## Phase 4: Backend Integration

### 4.1 API Interceptor Setup

**Create**: `src/services/api-interceptor.js`

```javascript
import axios from 'axios'
import { useLocaleStore } from '@/stores/locale'

// Request interceptor to add Accept-Language header
axios.interceptors.request.use(
  (config) => {
    const localeStore = useLocaleStore()
    config.headers['Accept-Language'] = localeStore.currentLocale
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle localized error messages
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.data && error.response.data.message) {
      // Backend will send localized error message based on Accept-Language
      error.message = error.response.data.message
    }
    return Promise.reject(error)
  }
)

export default axios
```

### 4.2 Update Existing API Services

**Files to Update**:
- [ ] `src/services/wallet.service.js`
- [ ] `src/services/transaction.service.js`
- [ ] `src/services/category.service.js`
- [ ] `src/services/debt.service.js`
- [ ] `src/services/report.service.js`
- [ ] `src/services/auth.service.js`

**Pattern**:
```javascript
// Before
import axios from 'axios'

// After
import axios from '@/services/api-interceptor'
```

### 4.3 Error Message Handling

**Create**: `src/locales/id/errors.json` & `src/locales/en/errors.json`

```json
{
  "errors": {
    "network": "Network error / Kesalahan jaringan",
    "unauthorized": "Unauthorized access / Akses tidak diizinkan",
    "notFound": "Data not found / Data tidak ditemukan",
    "serverError": "Server error / Kesalahan server",
    "validation": "Validation error / Kesalahan validasi"
  }
}
```

**Fallback Strategy**: If backend doesn't send localized message, use frontend translations.

---

## Backend Preparation Requirements

### 1. HTTP Headers Support

#### 1.1 Accept-Language Header
**Implementation Required**:
- Backend must read `Accept-Language` header from incoming requests
- Parse locale code (e.g., "id", "en")
- Use parsed locale for response localization

**Example Request Headers**:
```http
GET /api/v1/wallets HTTP/1.1
Host: 192.168.100.17:8081
Accept-Language: id
Authorization: Bearer <token>
```

**Backend Implementation Pseudocode**:
```java
// Spring Boot Example
@GetMapping("/wallets")
public ResponseEntity<?> getWallets(
    @RequestHeader(value = "Accept-Language", defaultValue = "id") String locale
) {
    // Use locale for response messages
    List<Wallet> wallets = walletService.getWallets();
    return ResponseEntity.ok(wallets);
}
```

### 2. Localized Error Messages

#### 2.1 Error Response Structure
**Standard Format**:
```json
{
  "success": false,
  "message": "Localized error message",
  "errors": {
    "field": ["Localized field error"]
  },
  "timestamp": "2025-12-10T08:15:00Z"
}
```

#### 2.2 Common Error Messages to Localize

**Authentication Errors**:
```json
{
  "id": {
    "auth.invalid_credentials": "Email atau kata sandi salah",
    "auth.token_expired": "Sesi Anda telah berakhir, silakan login kembali",
    "auth.unauthorized": "Anda tidak memiliki akses ke resource ini",
    "auth.email_exists": "Email sudah terdaftar",
    "auth.weak_password": "Kata sandi terlalu lemah"
  },
  "en": {
    "auth.invalid_credentials": "Invalid email or password",
    "auth.token_expired": "Your session has expired, please login again",
    "auth.unauthorized": "You don't have access to this resource",
    "auth.email_exists": "Email already registered",
    "auth.weak_password": "Password is too weak"
  }
}
```

**Validation Errors**:
```json
{
  "id": {
    "validation.required": "{field} wajib diisi",
    "validation.invalid_email": "Format email tidak valid",
    "validation.min_length": "{field} minimal {min} karakter",
    "validation.max_length": "{field} maksimal {max} karakter",
    "validation.positive_number": "{field} harus berupa angka positif",
    "validation.invalid_date": "Format tanggal tidak valid"
  },
  "en": {
    "validation.required": "{field} is required",
    "validation.invalid_email": "Invalid email format",
    "validation.min_length": "{field} must be at least {min} characters",
    "validation.max_length": "{field} must not exceed {max} characters",
    "validation.positive_number": "{field} must be a positive number",
    "validation.invalid_date": "Invalid date format"
  }
}
```

**Business Logic Errors**:
```json
{
  "id": {
    "wallet.insufficient_balance": "Saldo tidak mencukupi",
    "wallet.not_found": "Dompet tidak ditemukan",
    "transaction.invalid_amount": "Jumlah transaksi tidak valid",
    "debt.already_paid": "Utang sudah lunas",
    "subscription.already_active": "Langganan premium sudah aktif",
    "subscription.payment_failed": "Pembayaran gagal"
  },
  "en": {
    "wallet.insufficient_balance": "Insufficient balance",
    "wallet.not_found": "Wallet not found",
    "transaction.invalid_amount": "Invalid transaction amount",
    "debt.already_paid": "Debt already paid",
    "subscription.already_active": "Premium subscription already active",
    "subscription.payment_failed": "Payment failed"
  }
}
```

### 3. Database Localization (Optional - Future Enhancement)

#### 3.1 Multilingual Content Tables

**For User-Generated Content** (Categories, Wallet Names, etc.):
```sql
-- Current Structure (Single Language)
CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20)
);

-- Future Structure (Multiple Languages)
CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20)
);

CREATE TABLE category_translations (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36) NOT NULL,
    locale VARCHAR(5) NOT NULL,
    name VARCHAR(100) NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    UNIQUE KEY unique_category_locale (category_id, locale)
);
```

**Note**: This is optional for Milestone 7. Current implementation uses client-side translations only.

### 4. Backend Framework Configuration

#### 4.1 Spring Boot (Java)

**Add Dependencies** (`pom.xml`):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Configuration** (`application.yml`):
```yaml
spring:
  messages:
    basename: i18n/messages
    encoding: UTF-8
    fallback-to-system-locale: false
  web:
    locale: id
    locale-resolver: accept_header
```

**Create Message Files**:
```
src/main/resources/i18n/
├── messages_id.properties
└── messages_en.properties
```

**Locale Resolver Bean**:
```java
@Configuration
public class LocaleConfiguration {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("id"));
        resolver.setSupportedLocales(Arrays.asList(
            Locale.forLanguageTag("id"),
            Locale.forLanguageTag("en")
        ));
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
}
```

**Usage in Controllers**:
```java
@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    @Autowired
    private MessageSource messageSource;

    @PostMapping
    public ResponseEntity<?> createWallet(
        @RequestBody WalletRequest request,
        @RequestHeader(value = "Accept-Language", defaultValue = "id") Locale locale
    ) {
        try {
            Wallet wallet = walletService.create(request);
            String message = messageSource.getMessage(
                "wallet.created.success",
                null,
                locale
            );
            return ResponseEntity.ok(new SuccessResponse(message, wallet));
        } catch (Exception e) {
            String message = messageSource.getMessage(
                "wallet.created.failed",
                null,
                locale
            );
            return ResponseEntity.badRequest().body(new ErrorResponse(message));
        }
    }
}
```

#### 4.2 Laravel (PHP) - Alternative

**Configuration** (`config/app.php`):
```php
'locale' => 'id',
'fallback_locale' => 'id',
'available_locales' => ['id', 'en'],
```

**Middleware** (`app/Http/Middleware/SetLocale.php`):
```php
<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Support\Facades\App;

class SetLocale
{
    public function handle($request, Closure $next)
    {
        $locale = $request->header('Accept-Language', 'id');

        if (in_array($locale, config('app.available_locales'))) {
            App::setLocale($locale);
        }

        return $next($request);
    }
}
```

**Register Middleware** (`app/Http/Kernel.php`):
```php
protected $middlewareGroups = [
    'api' => [
        // ...
        \App\Http\Middleware\SetLocale::class,
    ],
];
```

**Translation Files**:
```
resources/lang/
├── id/
│   ├── auth.php
│   ├── validation.php
│   └── messages.php
└── en/
    ├── auth.php
    ├── validation.php
    └── messages.php
```

**Usage in Controllers**:
```php
use Illuminate\Support\Facades\Lang;

class WalletController extends Controller
{
    public function store(Request $request)
    {
        try {
            $wallet = Wallet::create($request->all());

            return response()->json([
                'success' => true,
                'message' => Lang::get('messages.wallet_created'),
                'data' => $wallet
            ], 201);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => Lang::get('messages.wallet_create_failed')
            ], 400);
        }
    }
}
```

### 5. Standard Response Format for Backend

Backend harus mengirimkan response dengan format yang konsisten untuk memudahkan frontend dalam menangani message localization.

#### 5.1 Success Response Format
```json
{
  "success": true,
  "message": "Localized success message",
  "data": {
    // Response data here
  },
  "timestamp": "2025-12-10T14:30:00Z"
}
```

**Contoh - Create Wallet (Indonesian)**:
```json
{
  "success": true,
  "message": "Dompet berhasil dibuat",
  "data": {
    "id": "wallet-123",
    "name": "Dompet Tunai",
    "balance": 1000000,
    "currency": "IDR"
  },
  "timestamp": "2025-12-10T14:30:00Z"
}
```

**Contoh - Create Wallet (English)**:
```json
{
  "success": true,
  "message": "Wallet created successfully",
  "data": {
    "id": "wallet-123",
    "name": "Cash Wallet",
    "balance": 1000000,
    "currency": "IDR"
  },
  "timestamp": "2025-12-10T14:30:00Z"
}
```

#### 5.2 Error Response Format
```json
{
  "success": false,
  "message": "Localized error message (general)",
  "errors": {
    "field_name": ["Localized field error message"]
  },
  "timestamp": "2025-12-10T14:30:00Z"
}
```

**Contoh - Validation Error (Indonesian)**:
```json
{
  "success": false,
  "message": "Validasi gagal",
  "errors": {
    "name": ["Nama dompet wajib diisi"],
    "balance": ["Saldo harus berupa angka positif"]
  },
  "timestamp": "2025-12-10T14:30:00Z"
}
```

**Contoh - Validation Error (English)**:
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "name": ["Wallet name is required"],
    "balance": ["Balance must be a positive number"]
  },
  "timestamp": "2025-12-10T14:30:00Z"
}
```

**Contoh - Business Logic Error (Indonesian)**:
```json
{
  "success": false,
  "message": "Saldo tidak mencukupi untuk transaksi ini",
  "timestamp": "2025-12-10T14:30:00Z"
}
```

**Contoh - Business Logic Error (English)**:
```json
{
  "success": false,
  "message": "Insufficient balance for this transaction",
  "timestamp": "2025-12-10T14:30:00Z"
}
```

#### 5.3 Premium Feature Error Format
```json
{
  "success": false,
  "error": "PREMIUM_REQUIRED",
  "message": "Localized premium requirement message",
  "timestamp": "2025-12-10T14:30:00Z"
}
```

**Contoh - Premium Required (Indonesian)**:
```json
{
  "success": false,
  "error": "PREMIUM_REQUIRED",
  "message": "Fitur ini memerlukan akun Premium. Upgrade sekarang untuk akses penuh!",
  "timestamp": "2025-12-10T14:30:00Z"
}
```

**Contoh - Premium Required (English)**:
```json
{
  "success": false,
  "error": "PREMIUM_REQUIRED",
  "message": "This feature requires a Premium account. Upgrade now for full access!",
  "timestamp": "2025-12-10T14:30:00Z"
}
```

### 6. API Endpoints to Localize

**Priority Endpoints** (Must have localized responses):

1. **Authentication** (`/api/v1/auth/*`)
    - Login: Success/error messages
    - Register: Validation errors
    - Logout: Confirmation message
    - Password reset: Email sent message

2. **Wallets** (`/api/v1/wallets/*`)
    - Create: Success/validation errors
    - Update: Success/validation errors
    - Delete: Confirmation/error messages
    - Insufficient balance errors

3. **Transactions** (`/api/v1/transactions/*`)
    - Create: Success/validation errors
    - Update: Success/validation errors
    - Delete: Confirmation/error messages
    - Invalid amount/date errors

4. **Debts** (`/api/v1/debts/*`)
    - Create: Success/validation errors
    - Payment: Success/already paid errors
    - Delete: Confirmation/error messages

5. **Subscription** (`/api/v1/subscription/*`)
    - Payment: Success/failure messages
    - Already subscribed errors
    - Trial activation messages

### 6. Testing Requirements for Backend

#### 6.1 Manual Testing Checklist
- [ ] Send request with `Accept-Language: id` header → Response in Indonesian
- [ ] Send request with `Accept-Language: en` header → Response in English
- [ ] Send request without header → Response in Indonesian (default)
- [ ] Send request with unsupported locale → Response in Indonesian (fallback)
- [ ] Validation errors are localized
- [ ] Business logic errors are localized
- [ ] Success messages are localized

#### 6.2 Example Test Cases

**Test Case 1: Create Wallet with Invalid Data (Indonesian)**
```http
POST /api/v1/wallets HTTP/1.1
Accept-Language: id
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "",
  "balance": -1000
}

Expected Response:
{
  "success": false,
  "message": "Validasi gagal",
  "errors": {
    "name": ["Nama dompet wajib diisi"],
    "balance": ["Saldo harus berupa angka positif"]
  }
}
```

**Test Case 2: Create Wallet with Invalid Data (English)**
```http
POST /api/v1/wallets HTTP/1.1
Accept-Language: en
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "",
  "balance": -1000
}

Expected Response:
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "name": ["Wallet name is required"],
    "balance": ["Balance must be a positive number"]
  }
}
```

### 7. Backend Implementation Timeline

**Estimated Effort**: 3-5 days

**Day 1-2**: Core Infrastructure
- [ ] Setup locale resolver (Accept-Language header parsing)
- [ ] Create message property files (messages_id.properties, messages_en.properties)
- [ ] Implement middleware/interceptor for locale detection
- [ ] Configure fallback strategy

**Day 2-3**: Error Message Localization
- [ ] Translate validation error messages (id & en)
- [ ] Translate authentication error messages
- [ ] Translate business logic error messages
- [ ] Update exception handlers to use localized messages

**Day 3-4**: API Response Localization
- [ ] Update success messages in all controllers
- [ ] Test all endpoints with both locales
- [ ] Fix any missing translations

**Day 4-5**: Testing & Documentation
- [ ] Manual testing with Postman/Insomnia
- [ ] Update API documentation with Accept-Language header
- [ ] Code review and refinement

---

## Testing Strategy

### Frontend Testing

#### Unit Tests
- [ ] Test locale store state management
- [ ] Test setLocale() function
- [ ] Test localStorage persistence
- [ ] Test getLocaleLabel() function

#### Integration Tests
- [ ] Test language switcher UI
- [ ] Test translation key resolution
- [ ] Test missing key fallback
- [ ] Test currency formatting
- [ ] Test date formatting

#### E2E Tests
- [ ] Navigate to dashboard → All text in Indonesian (default)
- [ ] Switch to English → All text changes to English
- [ ] Refresh page → Language preference persisted
- [ ] Create transaction → Currency format correct
- [ ] View report → Date format correct

### Backend Testing

#### API Tests
- [ ] Test Accept-Language header parsing
- [ ] Test Indonesian responses
- [ ] Test English responses
- [ ] Test fallback to Indonesian
- [ ] Test validation error localization
- [ ] Test business error localization

### Browser Testing
- [ ] Chrome (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Mobile browsers (iOS Safari, Chrome Mobile)

---

## Success Criteria

### Phase 1 ✅
- [x] i18n infrastructure setup complete
- [x] Language switcher functional
- [x] Common translations working
- [x] Navigation translations working

### Phase 2 ✅ COMPLETED
- [x] All core components translated
- [x] Authentication pages translated (Login, Register)
- [x] Layout components fully localized (AppHeader, AppSidebar, ThemeToggle)

### Phase 3 ✅ COMPLETED
- [x] All feature modules translated (Dashboard, Wallets, Transactions, Categories, Debts, Reports, Profile)
- [x] 95% coverage for existing UI (Premium/Subscription module pending)
- [x] No hardcoded strings remaining in core features

### Phase 4
- [ ] Backend accepts Accept-Language header
- [ ] All API responses localized
- [ ] Error messages localized
- [ ] E2E tests passing

### Overall
- [ ] Zero runtime errors related to i18n
- [ ] All text visible and readable in both languages
- [ ] Professional, consistent translations
- [ ] Smooth language switching (< 100ms)
- [ ] User preference persisted correctly

---

## Risk Mitigation

### Risk 1: Missing Translation Keys
**Mitigation**:
- Development mode shows warnings for missing keys
- Fallback to Indonesian for any missing translations
- Regular audits of translation coverage

### Risk 2: Inconsistent Translations
**Mitigation**:
- Use translation key naming convention consistently
- Review translations with native speakers
- Maintain glossary of technical terms

### Risk 3: Backend Not Ready
**Mitigation**:
- Frontend fully functional without backend localization
- Graceful fallback to frontend error messages
- API interceptor can be toggled on/off

### Risk 4: Performance Impact
**Mitigation**:
- Lazy loading of locale files
- Tree-shaking with Vite plugin
- LocalStorage caching of preference

---

## Future Enhancements

### Phase 5 (Future)
- [ ] Add more languages (Chinese, Spanish, etc.)
- [ ] Server-side rendering (SSR) support
- [ ] Pluralization rules for complex counts
- [ ] Number formatting preferences (user override)
- [ ] Date format preferences (user override)
- [ ] Currency symbol preferences
- [ ] Export translations to CSV for translators
- [ ] Translation management system integration

---

## Glossary

**i18n**: Internationalization (18 letters between 'i' and 'n')
**l10n**: Localization (10 letters between 'l' and 'n')
**Locale**: Language and region combination (e.g., id-ID, en-US)
**Fallback**: Default language when translation is missing
**Message Key**: Unique identifier for translatable text
**Accept-Language**: HTTP header specifying user's language preference
**Hybrid Format**: Mixed approach (keep symbols, adapt number format)

---

## Contact & Support

**Frontend Lead**: [Your Name]
**Backend Lead**: [Backend Developer Name]
**QA Lead**: [QA Engineer Name]

**Documentation**: [Link to detailed docs]
**Issue Tracker**: [Link to issue board]
**Translation Review**: [Link to translation spreadsheet]

---

## 📝 Implementation Notes

### Components Successfully Translated (39 total):

**Layout & Core (5)**:
1. AppHeader.vue
2. AppSidebar.vue
3. ThemeToggle.vue
4. AppModal.vue
5. Common translations (buttons, states, time)

**Authentication (2)**:
6. LoginView.vue
7. RegisterView.vue

**Dashboard (3)**:
8. DashboardView.vue
9. RecentTransactions.vue
10. WalletFilter.vue

**Wallets (6)**:
11. WalletListView.vue
12. WalletDetailView.vue
13. WalletCreateView.vue
14. WalletEditView.vue
15. WalletCard.vue
16. WalletForm.vue

**Transactions (6)**:
17. TransactionListView.vue
18. TransactionCreateView.vue
19. TransactionEditView.vue
20. TransactionList.vue
21. TransactionFilters.vue
22. TransactionForm.vue

**Categories (3)**:
23. CategoryView.vue
24. CategoryForm.vue
25. CategoryList.vue

**Debts (7)**:
26. DebtListView.vue
27. DebtDetailView.vue
28. DebtForm.vue
29. PaymentHistory.vue
30. DebtCard.vue
31. DebtFilters.vue
32. DebtSummary.vue

**Reports (1)**:
33. ReportsPage.vue

**Profile (3)**:
34. ProfileView.vue
35. ProfileForm.vue
36. SubscriptionBadge.vue (partial)

**Premium/Subscription (3)**: ✅ NEW
37. PremiumView.vue - Full premium page with status banners, hero section, trial info, comparison table, 6 feature cards, and CTA
38. TrialBanner.vue - Trial notification banner with progress bar
39. UpgradeModal.vue - Premium feature upgrade prompt modal

### Translation Key Structure:
```
common (buttons, actions, states, theme, time, locale, pagination, empty)
navigation (sidebar, menu)
auth (login, register)
dashboard
wallets
transactions
categories
debts (including paymentHistory)
reports (summary, categoryBreakdown, trend, granularity)
profile (accountInfo, subscription, form)
```

---

**Last Updated**: December 10, 2025 - 21:30 WIB
**Phase 1-3 Status**: ✅ COMPLETED
**Next Steps**: Phase 4 - Backend Integration & Premium Module Translation
