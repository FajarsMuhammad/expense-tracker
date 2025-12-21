# Expense Tracker - Project Structure Documentation

**Last Updated:** December 17, 2025
**Version:** 0.0.1-SNAPSHOT
**Java Version:** 25 (LTS)
**Spring Boot Version:** 3.5.7

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Directory Structure](#directory-structure)
3. [Source Code Organization](#source-code-organization)
4. [Architectural Patterns](#architectural-patterns)
5. [Module Details](#module-details)
6. [Configuration Files](#configuration-files)
7. [Database Migrations](#database-migrations)
8. [Testing Structure](#testing-structure)
9. [Infrastructure & DevOps](#infrastructure--devops)
10. [Build & Dependencies](#build--dependencies)

---

## Project Overview

The Expense Tracker is a modern Spring Boot application built using Clean Architecture principles with a focus on:

- **Domain-Driven Design (DDD)**: Business logic encapsulated in domain entities
- **Use Case Pattern**: Application logic separated into focused use cases
- **Repository Pattern**: Data access abstraction
- **RESTful API**: JSON-based API with OpenAPI/Swagger documentation
- **Security**: JWT-based authentication and authorization
- **Premium Features**: Subscription-based access control
- **Payment Integration**: Midtrans payment gateway for subscription upgrades
- **Observability**: Prometheus metrics, structured logging, distributed tracing

---

## Directory Structure

```
expense-tracker/
├── .claude/                          # Claude AI configuration
├── .idea/                            # IntelliJ IDEA project files
├── documentations/                   # Project documentation
│   ├── docker/                       # Docker-related documentation
│   ├── SPRING_BOOT_4_UPGRADE.md     # Spring Boot 4.0 upgrade guide
│   └── project_structure.md         # This file
├── logs/                             # Application logs (runtime)
├── monitoring/                       # Monitoring infrastructure
│   ├── grafana/                      # Grafana dashboards
│   ├── loki/                         # Loki log aggregation
│   ├── prometheus/                   # Prometheus metrics
│   └── promtail/                     # Promtail log shipping
├── performance-tests/                # Performance testing scripts
│   └── reports/                      # Test reports
├── project_plan/                     # Project planning documents
│   ├── milestone_1/                  # Authentication & Authorization
│   ├── milestone_2/                  # Core Transaction Features
│   ├── milestone_3/                  # Categories & Wallets
│   ├── milestone_4/                  # Reporting & Analytics
│   ├── milestone_5/                  # Debt Management
│   ├── milestone_6/                  # Export Features
│   ├── milestone_7/                  # Premium Features & Payments
│   └── milestone_8/                  # Java 25 & Spring Boot Upgrades
├── src/
│   ├── main/
│   │   ├── java/                     # Java source code
│   │   └── resources/                # Configuration files
│   └── test/
│       └── java/                     # Test source code
├── build.gradle                      # Gradle build configuration
├── settings.gradle                   # Gradle settings
├── Dockerfile                        # Docker image definition
└── docker-compose.yml               # Docker Compose orchestration
```

---

## Source Code Organization

### Main Application Structure

```
src/main/java/com/fajars/expensetracker/
├── ExpenseTrackerApplication.java   # Spring Boot main class
│
├── auth/                             # Authentication & Authorization
│   ├── AuthController.java          # Login, Register, Refresh endpoints
│   ├── AuthService.java             # Authentication business logic
│   ├── CustomUserDetailsService.java # Spring Security UserDetailsService
│   └── [DTOs]                        # Request/Response models
│
├── category/                         # Category Management
│   ├── Category.java                 # Domain entity
│   ├── CategoryController.java      # REST endpoints
│   ├── CategoryRepository.java      # Data access
│   └── usecase/                      # Business use cases
│       ├── CreateCategoryUseCase.java
│       ├── UpdateCategoryUseCase.java
│       ├── DeleteCategoryUseCase.java
│       └── Find*.java
│
├── common/                           # Shared components
│   ├── config/                       # Configuration classes
│   │   ├── DataSeeder.java          # Database seeding
│   │   ├── JacksonConfig.java       # JSON serialization
│   │   ├── LocaleConfiguration.java # Internationalization
│   │   └── OpenApiConfig.java       # Swagger/OpenAPI
│   │
│   ├── converter/                    # Custom converters
│   │   └── JsonbConverter.java      # PostgreSQL JSONB converter
│   │
│   ├── exception/                    # Exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   └── [Other exceptions]
│   │
│   ├── i18n/                         # Internationalization
│   │   └── MessageHelper.java       # i18n message resolution
│   │
│   ├── logging/                      # Logging infrastructure
│   │   ├── LoggingAspect.java       # AOP-based logging
│   │   ├── CorrelationIdFilter.java # Request correlation
│   │   ├── SecurityLoggingFilter.java
│   │   └── SensitiveDataFilter.java # PII masking
│   │
│   ├── metrics/                      # Observability
│   │   └── MetricsService.java      # Custom metrics
│   │
│   ├── ratelimit/                    # Rate limiting
│   │   ├── RateLimiter.java
│   │   └── ReportFrequencyLimiter.java
│   │
│   ├── security/                     # Security infrastructure
│   │   ├── SecurityConfig.java      # Spring Security config
│   │   ├── PremiumFeatureAspect.java # Premium access control
│   │   └── RequiresPremium.java     # Annotation
│   │
│   ├── util/                         # Utilities
│   │   ├── JwtUtil.java             # JWT token handling
│   │   └── JwtAuthenticationFilter.java
│   │
│   └── validation/                   # Custom validators
│       └── DateRangeValidator.java
│
├── config/                           # Additional configuration
│   └── CacheConfig.java             # Caffeine cache setup
│
├── dashboard/                        # Dashboard Features
│   ├── DashboardController.java
│   └── usecase/
│       └── GetDashboardSummaryUseCase.java
│
├── debt/                             # Debt Management
│   ├── Debt.java                    # Domain entity
│   ├── DebtPayment.java             # Payment tracking
│   ├── DebtController.java
│   ├── DebtRepository.java
│   └── usecase/                     # Business operations
│       ├── CreateDebtUseCase.java
│       ├── AddDebtPaymentUseCase.java
│       ├── MarkDebtAsPaidUseCase.java
│       └── [Other use cases]
│
├── payment/                          # Payment Processing
│   ├── PaymentTransaction.java      # Domain entity
│   ├── PaymentController.java
│   ├── PaymentRepository.java
│   ├── midtrans/                     # Midtrans integration
│   │   ├── MidtransClient.java      # API client
│   │   ├── MidtransConfig.java
│   │   ├── WebhookVerifier.java     # Signature verification
│   │   └── [DTOs]
│   └── usecase/
│       ├── CreateSubscriptionPaymentUseCase.java
│       └── ProcessPaymentWebhookUseCase.java
│
├── report/                           # Reporting & Analytics
│   ├── ReportController.java
│   ├── ExportController.java
│   ├── export/                       # Export implementations
│   │   ├── CsvExporter.java         # CSV export
│   │   ├── ExcelExporter.java       # Excel export
│   │   └── PdfExporter.java         # PDF export
│   └── usecase/
│       ├── GenerateFinancialSummaryUseCase.java
│       ├── GetCategoryBreakdownUseCase.java
│       ├── GetIncomeExpenseTrendUseCase.java
│       └── ExportTransactionsUseCase.java
│
├── subscription/                     # Subscription Management
│   ├── Subscription.java            # Domain entity
│   ├── SubscriptionController.java
│   ├── SubscriptionRepository.java
│   ├── SubscriptionService.java
│   ├── SubscriptionHelper.java      # Business rules
│   ├── scheduler/
│   │   └── ProcessExpiredTrialsScheduler.java
│   └── usecase/
│       ├── CreateFreeSubscriptionUseCase.java
│       ├── CreateTrialSubscriptionUseCase.java
│       ├── UpgradeSubscriptionUseCase.java
│       ├── ActivateSubscriptionUseCase.java
│       └── [Other use cases]
│
├── transaction/                      # Transaction Management
│   ├── Transaction.java             # Domain entity
│   ├── TransactionController.java
│   ├── TransactionRepository.java
│   ├── TransactionExportRepository.java # Optimized for exports
│   ├── projection/                   # Database projections
│   │   ├── CategoryBreakdown.java
│   │   ├── TransactionSummary.java
│   │   ├── TransactionExportRow.java
│   │   └── TrendData.java
│   └── usecase/
│       ├── CreateTransactionUseCase.java
│       ├── UpdateTransactionUseCase.java
│       ├── DeleteTransactionUseCase.java
│       └── Find*.java
│
├── user/                             # User Management
│   ├── User.java                    # Domain entity
│   ├── UserController.java
│   ├── UserRepository.java
│   ├── UserService.java
│   └── usecase/
│       ├── GetUserProfileUseCase.java
│       └── UpdateUserProfileUseCase.java
│
└── wallet/                           # Wallet Management
    ├── Wallet.java                  # Domain entity
    ├── WalletController.java
    ├── WalletRepository.java
    └── usecase/
        ├── CreateWalletUseCase.java
        ├── UpdateWalletUseCase.java
        ├── DeleteWalletUseCase.java
        └── Find*.java
```

---

## Architectural Patterns

### 1. Clean Architecture (Hexagonal Architecture)

**Layers:**
```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Controllers, DTOs, Exception Handlers)│
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Application Layer               │
│  (Use Cases, Business Orchestration)    │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Domain Layer                   │
│  (Entities, Business Rules, Validators) │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│      Infrastructure Layer               │
│  (Repositories, External Services)      │
└─────────────────────────────────────────┘
```

### 2. Use Case Pattern

Each business operation is encapsulated in a dedicated use case class:

```java
// Interface defines the contract
public interface CreateTransactionUseCase {
    TransactionResponse execute(CreateTransactionRequest request, UUID userId);
}

// Implementation contains business logic
@Service
@RequiredArgsConstructor
public class CreateTransaction implements CreateTransactionUseCase {
    private final TransactionRepository repository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public TransactionResponse execute(CreateTransactionRequest request, UUID userId) {
        // Business logic here
    }
}
```

**Benefits:**
- Single Responsibility Principle
- Easy to test
- Clear business intent
- Reusable across different entry points

### 3. Repository Pattern

Data access is abstracted through Spring Data JPA repositories:

```java
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUserIdAndWalletIdOrderByDateDesc(UUID userId, UUID walletId);

    @Query("SELECT new com.fajars.expensetracker.transaction.projection.TransactionSummary(...)")
    TransactionSummary getSummary(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
}
```

### 4. Domain-Driven Design (DDD)

**Rich Domain Models:**
```java
@Entity
public class PaymentTransaction {
    // Domain logic encapsulated in entity
    public void markAsSuccess(String transactionId, PaymentMethod paymentMethod) {
        validateStatusTransition(PaymentStatus.SUCCESS);
        this.status = PaymentStatus.SUCCESS;
        this.transactionId = transactionId;
        this.paidAt = LocalDateTime.now();
    }

    private void validateStatusTransition(PaymentStatus newStatus) {
        if (this.status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot change status from SUCCESS");
        }
    }
}
```

### 5. Aspect-Oriented Programming (AOP)

**Cross-cutting Concerns:**
- **Logging**: `@LoggingAspect` - Method entry/exit logging
- **Security**: `@PremiumFeatureAspect` - Premium access control
- **Metrics**: Method execution time tracking

```java
@Aspect
@Component
public class PremiumFeatureAspect {
    @Around("@annotation(RequiresPremium)")
    public Object checkPremiumAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        // Verify subscription status before method execution
    }
}
```

---

## Module Details

### Authentication (`auth/`)

**Purpose:** User authentication and JWT token management

**Key Components:**
- `AuthController`: Login, register, token refresh endpoints
- `AuthService`: Authentication orchestration
- `CustomUserDetailsService`: Spring Security integration
- JWT-based stateless authentication (JJWT 0.12.5)

**Security Features:**
- BCrypt password hashing
- JWT access tokens (1 hour expiry)
- JWT refresh tokens (7 days expiry)
- Correlation ID tracking

### Category Management (`category/`)

**Purpose:** Transaction categorization (Income/Expense)

**Use Cases:**
- Create category (validated name, type, icon)
- Update category
- Delete category (validates no active transactions)
- List categories by type
- Find category by ID

**Business Rules:**
- Category names must be unique per user
- Cannot delete category with existing transactions
- Icons are optional but recommended

### Common Infrastructure (`common/`)

**Configuration:**
- `JacksonConfig`: JSON serialization (ISO-8601 dates, null handling)
- `LocaleConfiguration`: i18n support (English, Indonesian)
- `OpenApiConfig`: Swagger UI configuration
- `DataSeeder`: Development data seeding

**Security:**
- Spring Security with JWT filters
- CORS configuration (Spring Framework 7.0 compatible)
- Premium feature access control via AOP

**Observability:**
- Structured JSON logging (Logstash format)
- Correlation ID propagation
- PII masking in logs
- Prometheus metrics
- Custom business event logging

**Exception Handling:**
- Global exception handler
- Standardized error responses
- HTTP status code mapping
- i18n error messages

### Dashboard (`dashboard/`)

**Purpose:** Financial overview and insights

**Features:**
- Current month summary (income, expenses, balance)
- Weekly spending trends
- Top spending categories
- Cached for performance (5-minute TTL)

### Debt Management (`debt/`)

**Purpose:** Track money owed/borrowed

**Domain Model:**
- `Debt`: Main entity (amount, debtor/creditor, due date)
- `DebtPayment`: Payment installments
- Status: PENDING, PARTIAL, PAID

**Use Cases:**
- Create debt (OWES or LENT)
- Add payment installment
- Mark as fully paid
- List debts with filters (status, type)
- Get debt detail with payment history

**Business Rules:**
- Payments cannot exceed remaining balance
- Status auto-updates when fully paid
- Cannot add payment to paid debt

### Payment Integration (`payment/`)

**Purpose:** Process subscription payments via Midtrans

**Components:**
- `PaymentTransaction`: Domain entity with state machine
- `MidtransClient`: Snap API integration (WebClient)
- `WebhookVerifier`: SHA-512 signature validation
- Idempotency key support

**Payment Flow:**
1. User initiates upgrade
2. Create PaymentTransaction (PENDING)
3. Call Midtrans Snap API → get token
4. User completes payment
5. Webhook received → verify signature
6. Update transaction status → activate subscription

**Status Transitions:**
```
PENDING → SUCCESS (payment completed)
PENDING → FAILED (payment failed)
PENDING → EXPIRED (24-hour timeout)
PENDING → CANCELLED (user cancelled)
```

**Data Storage:**
- JSONB columns for webhook payload (audit trail)
- Metadata for device info, IP, campaign tracking
- Custom `JsonbConverter` for Jackson 2.x/3.x compatibility

### Reporting & Analytics (`report/`)

**Purpose:** Financial insights and data export

**Analytics Features:**
- Financial summary (income, expenses, savings rate)
- Category breakdown (spending by category)
- Income/Expense trends (daily, weekly, monthly)
- Wallet balances

**Export Formats:**
- **CSV**: OpenCSV with bean mapping
- **Excel**: Apache POI (XLSX format)
- **PDF**: OpenPDF (table layout)

**Premium Features:**
- Custom date ranges (premium)
- Advanced filters (premium)
- Unlimited exports (free: 10/day, premium: unlimited)

**Performance:**
- Rate limiting (prevent abuse)
- Optimized SQL queries with projections
- Streaming for large exports
- Caching for summary data

### Subscription Management (`subscription/`)

**Purpose:** Manage subscription tiers and lifecycle

**Tiers:**
- **FREE**: Basic features, 10 exports/day
- **PREMIUM**: Advanced analytics, unlimited exports, custom reports

**Lifecycle:**
1. **Trial**: 14-day free trial (one-time per user)
2. **Active**: Paid subscription (monthly/yearly)
3. **Expired**: Past due date
4. **Cancelled**: User cancelled

**Business Rules:**
- One active subscription per user
- Trial eligibility checked (no previous trials)
- Grace period: 3 days after expiry
- Auto-downgrade to FREE after grace period

**Scheduler:**
- `ProcessExpiredTrialsScheduler`: Runs daily at 2 AM
- Expires trials past due date
- Logs all state transitions

### Transaction Management (`transaction/`)

**Purpose:** Core financial transaction tracking

**Domain Model:**
- Amount, date, type (INCOME/EXPENSE)
- Category, wallet references
- Optional notes, location

**Use Cases:**
- Create transaction (updates wallet balance)
- Update transaction (recalculates balance)
- Delete transaction (reverses balance change)
- List with pagination and filters
- Find by ID

**Repositories:**
- `TransactionRepository`: CRUD operations
- `TransactionExportRepository`: Optimized projections for exports

**Projections:**
- `TransactionSummary`: Aggregated totals
- `CategoryBreakdown`: Group by category
- `TrendData`: Time-series data
- `TransactionExportRow`: Flattened for CSV/Excel

### User Management (`user/`)

**Purpose:** User profile and account settings

**Features:**
- Profile management (name, email)
- Timezone and locale preferences
- Subscription information in profile

**Security:**
- Password stored as BCrypt hash
- Email uniqueness validation
- Cannot change email after registration

### Wallet Management (`wallet/`)

**Purpose:** Multiple account/wallet support

**Features:**
- Create wallet (name, currency, initial balance)
- Update wallet metadata
- Delete wallet (validates no transactions)
- List all user wallets
- Track current balance (auto-updated)

**Business Rules:**
- Wallet names unique per user
- Cannot delete wallet with transactions
- Balance auto-calculated from transactions
- Supports multiple currencies (USD, EUR, IDR, etc.)

---

## Configuration Files

### `application.yaml`

**Location:** `src/main/resources/application.yaml`

**Configuration Sections:**

```yaml
spring:
  application:
    name: expense-tracker

  datasource:
    # PostgreSQL configuration
    url: ${DB_URL:jdbc:postgresql://localhost:5432/expense_tracker}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}

  jpa:
    # Hibernate configuration
    hibernate:
      ddl-auto: validate  # Use Flyway for migrations
    show-sql: false
    properties:
      hibernate.format_sql: true

  flyway:
    # Database migrations
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  cache:
    # Caffeine cache
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m

# JWT configuration
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 3600000  # 1 hour
  refresh-expiration: 604800000  # 7 days

# Midtrans payment gateway
midtrans:
  server-key: ${MIDTRANS_SERVER_KEY}
  client-key: ${MIDTRANS_CLIENT_KEY}
  api-url: ${MIDTRANS_API_URL:https://app.sandbox.midtrans.com/snap/v1}
  is-production: ${MIDTRANS_IS_PRODUCTION:false}

# Observability
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true
```

### `build.gradle`

**Key Dependencies:**

```gradle
// Core Spring Boot
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-validation'

// Database
implementation 'org.flywaydb:flyway-core:9.22.0'
runtimeOnly 'org.postgresql:postgresql'

// Security & JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'

// API Documentation
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14'

// Observability
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-registry-prometheus'
implementation 'net.logstash.logback:logstash-logback-encoder:7.4'
implementation 'org.springframework.boot:spring-boot-starter-aop'

// Export & Reports
implementation 'com.opencsv:opencsv:5.9'
implementation 'org.apache.poi:poi:5.2.5'
implementation 'org.apache.poi:poi-ooxml:5.2.5'
implementation 'com.github.librepdf:openpdf:2.0.2'

// Caching
implementation 'org.springframework.boot:spring-boot-starter-cache'
implementation 'com.github.ben-manes.caffeine:caffeine'

// WebClient for Midtrans API
implementation 'org.springframework.boot:spring-boot-starter-webflux'

// Utilities
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

### `Dockerfile`

**Multi-stage Build:**

```dockerfile
# Stage 1: Build
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle clean bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar expense-tracker.jar

# Java 25 optimizations
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseZGC", \
  "-Xlog:gc*:file=/app/logs/gc.log:time,uptime:filecount=5,filesize=10M", \
  "-jar", "/app/expense-tracker.jar"]
```

**Optimizations:**
- Multi-stage build (smaller image size)
- Alpine Linux base (~100 MB smaller)
- ZGC garbage collector (low latency)
- Container-aware heap sizing
- GC logging for diagnostics

### `docker-compose.yml`

**Services:**

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: expense_tracker
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8081:8081"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/expense_tracker
      JWT_SECRET: dev-secret-key
    depends_on:
      - postgres

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus:/etc/prometheus

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    volumes:
      - ./monitoring/grafana:/etc/grafana/provisioning
```

---

## Database Migrations

**Location:** `src/main/resources/db/migration/`

**Flyway Naming Convention:** `V{version}__{description}.sql`

### Migration Files:

1. **V1__init.sql**
   - Initial schema creation
   - Tables: users, wallets, categories, transactions, subscriptions
   - Indexes for common queries
   - UUID primary keys

2. **V2__add_debt_type_and_note.sql**
   - Add debt management tables
   - `debts` and `debt_payments` tables
   - Foreign key constraints

3. **V3__add_reporting_indexes.sql**
   - Performance indexes for reporting queries
   - Composite indexes for date + user filters
   - Category breakdown indexes

4. **V4__add_payment_tables.sql**
   - `payment_transactions` table
   - JSONB columns for webhook payloads
   - Idempotency key support
   - Payment status indexes

5. **V5__add_premium_feature_indexes.sql**
   - Indexes for subscription queries
   - Composite indexes for premium feature checks
   - Performance optimization

**Best Practices:**
- Never modify existing migrations
- Always test migrations on copy of production data
- Use transactions for data migrations
- Add rollback scripts for complex changes

---

## Testing Structure

### Test Directory Structure:

```
src/test/java/com/fajars/expensetracker/
├── ExpenseTrackerApplicationTests.java  # Integration test
│
├── category/usecase/
│   ├── CreateCategoryUseCaseTest.java
│   ├── UpdateCategoryUseCaseTest.java
│   ├── DeleteCategoryUseCaseTest.java
│   └── FindAllCategoriesUseCaseTest.java
│
├── dashboard/usecase/
│   └── GetDashboardSummaryUseCaseTest.java
│
├── debt/usecase/
│   ├── CreateDebtUseCaseTest.java
│   ├── AddDebtPaymentUseCaseTest.java
│   ├── MarkDebtAsPaidUseCaseTest.java
│   └── UpdateDebtUseCaseTest.java
│
├── subscription/
│   ├── SubscriptionHelperTest.java
│   └── usecase/
│       ├── CreateFreeSubscriptionUseCaseTest.java
│       ├── CreateTrialSubscriptionUseCaseTest.java
│       ├── UpgradeSubscriptionUseCaseTest.java
│       ├── ActivateSubscriptionUseCaseTest.java
│       ├── CancelSubscriptionUseCaseTest.java
│       └── CheckTrialEligibilityUseCaseTest.java
│
├── util/
│   └── JwtUtilTest.java
│
└── wallet/usecase/
    ├── CreateWalletUseCaseTest.java
    ├── UpdateWalletUseCaseTest.java
    ├── DeleteWalletUseCaseTest.java
    └── FindAllWalletsUseCaseTest.java
```

### Test Coverage:

**Current Status:** 21 tests (all passing)

**Test Categories:**
- **Unit Tests**: Use case logic with mocked dependencies
- **Integration Tests**: Spring Boot application context
- **Security Tests**: JWT token generation/validation

**Testing Tools:**
- JUnit 5 (Jupiter)
- Mockito (mocking framework)
- Spring Boot Test
- AssertJ (fluent assertions)

**Test Execution:**
```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests CreateCategoryUseCaseTest
```

---

## Infrastructure & DevOps

### Monitoring Stack

**Prometheus + Grafana + Loki**

**Metrics Collected:**
- HTTP request rates and latencies
- JVM memory usage (heap, non-heap, GC)
- Database connection pool stats
- Custom business metrics (transactions/min, exports/day)
- Cache hit rates

**Logs Aggregation:**
- Structured JSON logs (Logstash format)
- Correlation ID tracking across requests
- Log levels: DEBUG, INFO, WARN, ERROR
- PII masking (email, phone, card numbers)

**Dashboards:**
- Application health overview
- JVM metrics (memory, GC, threads)
- API endpoint performance
- Database query performance

### Configuration Files:

**Prometheus** (`monitoring/prometheus/prometheus.yml`):
```yaml
scrape_configs:
  - job_name: 'expense-tracker'
    metrics_path: '/api/v1/actuator/prometheus'
    static_configs:
      - targets: ['app:8081']
```

**Loki** (`monitoring/loki/loki-config.yaml`):
- Log retention: 30 days
- Compression: gzip
- Query timeout: 5 minutes

**Promtail** (`monitoring/promtail/promtail-config.yaml`):
- Scrape application logs from `/app/logs`
- Parse JSON log format
- Extract labels (level, logger, correlation_id)

### Performance Testing

**Location:** `performance-tests/`

**Tool:** Apache JMeter (assumed)

**Test Scenarios:**
- Load testing (100-1000 concurrent users)
- Stress testing (find breaking point)
- Endurance testing (sustained load)
- Spike testing (sudden traffic increase)

**Reports:** `performance-tests/reports/`

---

## Build & Dependencies

### Build Configuration

**Gradle Version:** 9.2.1
**Java Toolchain:** Java 25

**Build Commands:**
```bash
# Clean build
./gradlew clean build

# Compile only
./gradlew compileJava compileTestJava

# Run tests
./gradlew test

# Build JAR
./gradlew bootJar

# Run application
./gradlew bootRun

# Build Docker image
docker build -t expense-tracker:latest .
```

### Key Dependencies by Category:

**Spring Boot Ecosystem:**
- Spring Boot 3.5.7
- Spring Data JPA
- Spring Security
- Spring Web (RESTful)
- Spring Validation
- Spring AOP
- Spring Cache
- Spring Actuator

**Database:**
- PostgreSQL JDBC Driver
- Flyway 9.22.0 (migrations)

**Security:**
- JJWT 0.12.5 (JWT tokens)
- BCrypt (password hashing)

**Observability:**
- Micrometer + Prometheus
- Logstash Logback Encoder

**Documentation:**
- SpringDoc OpenAPI 2.8.14 (Swagger UI)

**Export Libraries:**
- OpenCSV 5.9
- Apache POI 5.2.5 (Excel)
- OpenPDF 2.0.2

**Caching:**
- Caffeine Cache

**HTTP Client:**
- Spring WebFlux WebClient (Midtrans API)

**Utilities:**
- Lombok (reduce boilerplate)
- Jackson (JSON serialization)

---

## API Documentation

### OpenAPI/Swagger

**Access URL:** `http://localhost:8081/api/v1/swagger-ui.html`

**Features:**
- Interactive API testing
- Request/response schemas
- Authentication flow (Bearer tokens)
- Example requests

### API Endpoints by Module:

**Authentication:**
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - Login (get JWT tokens)
- `POST /api/v1/auth/refresh` - Refresh access token

**Categories:**
- `GET /api/v1/categories` - List all categories
- `GET /api/v1/categories/{id}` - Get category by ID
- `POST /api/v1/categories` - Create category
- `PUT /api/v1/categories/{id}` - Update category
- `DELETE /api/v1/categories/{id}` - Delete category

**Transactions:**
- `GET /api/v1/transactions` - List with pagination & filters
- `GET /api/v1/transactions/{id}` - Get transaction by ID
- `POST /api/v1/transactions` - Create transaction
- `PUT /api/v1/transactions/{id}` - Update transaction
- `DELETE /api/v1/transactions/{id}` - Delete transaction

**Wallets:**
- `GET /api/v1/wallets` - List all wallets
- `GET /api/v1/wallets/{id}` - Get wallet by ID
- `POST /api/v1/wallets` - Create wallet
- `PUT /api/v1/wallets/{id}` - Update wallet
- `DELETE /api/v1/wallets/{id}` - Delete wallet

**Debts:**
- `GET /api/v1/debts` - List debts with filters
- `GET /api/v1/debts/{id}` - Get debt detail
- `POST /api/v1/debts` - Create debt
- `PUT /api/v1/debts/{id}` - Update debt
- `POST /api/v1/debts/{id}/payments` - Add payment
- `POST /api/v1/debts/{id}/mark-paid` - Mark as paid

**Reports:**
- `GET /api/v1/reports/summary` - Financial summary
- `GET /api/v1/reports/category-breakdown` - Spending by category
- `GET /api/v1/reports/trends` - Income/expense trends

**Export:**
- `POST /api/v1/exports/transactions` - Export transactions (CSV/Excel/PDF)

**Dashboard:**
- `GET /api/v1/dashboard/summary` - Dashboard overview

**Subscriptions:**
- `GET /api/v1/subscriptions/status` - Current subscription
- `POST /api/v1/subscriptions/trial` - Start trial
- `POST /api/v1/subscriptions/upgrade` - Upgrade to premium
- `POST /api/v1/subscriptions/cancel` - Cancel subscription

**Payments:**
- `POST /api/v1/payments/subscription` - Create payment
- `POST /api/v1/payments/webhook/midtrans` - Midtrans webhook

**Users:**
- `GET /api/v1/users/profile` - Get profile
- `PUT /api/v1/users/profile` - Update profile

---

## Security Considerations

### Authentication & Authorization

**JWT Token Flow:**
1. User login → receive access token (1h) + refresh token (7d)
2. Access token in `Authorization: Bearer {token}` header
3. Refresh token when access token expires
4. Logout by removing tokens (client-side)

**Password Security:**
- BCrypt hashing (strength: 10 rounds)
- No password reset implemented (future feature)

**Premium Access Control:**
- AOP-based `@RequiresPremium` annotation
- Validates active subscription before method execution
- Returns 403 Forbidden for free users

### Input Validation

**Jakarta Validation:**
- `@NotNull`, `@NotBlank` for required fields
- `@Email` for email format
- `@Positive` for amounts
- `@Size` for string lengths
- Custom validators for date ranges

**SQL Injection Prevention:**
- JPA/Hibernate parameterized queries
- Never concatenate user input in SQL

**XSS Prevention:**
- JSON responses (no HTML rendering)
- Content-Type: application/json

### CORS Configuration

**Allowed Origins:** All (`*`) - Configure for production!
**Allowed Methods:** All
**Allowed Headers:** All

**Production Recommendation:**
```java
config.addAllowedOriginPattern("https://yourdomain.com");
config.addAllowedOriginPattern("https://app.yourdomain.com");
```

### Rate Limiting

**Export Rate Limits:**
- Free users: 10 exports/day
- Premium users: Unlimited

**Implementation:** In-memory tracking with Caffeine cache

**Future Improvements:**
- Redis-based distributed rate limiting
- Per-endpoint rate limits
- Configurable limits via application.yaml

---

## Internationalization (i18n)

**Supported Locales:**
- English (en)
- Indonesian (id)

**Message Files:**
- `src/main/resources/i18n/messages_en.properties`
- `src/main/resources/i18n/messages_id.properties`

**Usage:**
```java
@Autowired
private MessageHelper messageHelper;

String message = messageHelper.getMessage("error.user.not_found");
```

**API Behavior:**
- Auto-detect `Accept-Language` header
- Default: English
- Error messages localized

---

## Environment Variables

**Required:**
- `DB_URL` - PostgreSQL connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - Secret key for JWT signing (min 32 chars)
- `MIDTRANS_SERVER_KEY` - Midtrans API server key
- `MIDTRANS_CLIENT_KEY` - Midtrans API client key

**Optional:**
- `MIDTRANS_API_URL` - Midtrans API URL (default: sandbox)
- `MIDTRANS_IS_PRODUCTION` - Production mode (default: false)
- `SERVER_PORT` - Application port (default: 8081)

**Development Defaults:**
See `application.yaml` for default values (suitable for local development).

---

## Deployment

### Local Development

```bash
# Start PostgreSQL
docker compose up postgres -d

# Run application
./gradlew bootRun

# Access
http://localhost:8081/api/v1/swagger-ui.html
```

### Docker Deployment

```bash
# Build image
docker build -t expense-tracker:latest .

# Run with Docker Compose
docker compose up -d

# View logs
docker compose logs -f app

# Stop
docker compose down
```

### Production Considerations

**Database:**
- Use managed PostgreSQL (AWS RDS, Azure Database, etc.)
- Enable SSL/TLS connections
- Regular backups (daily minimum)
- Point-in-time recovery enabled

**Application:**
- Set production JWT_SECRET (32+ random chars)
- Configure CORS for specific origins
- Enable HTTPS (reverse proxy)
- Use environment-specific profiles
- Monitor JVM heap usage
- Set up log rotation

**Observability:**
- Centralized logging (CloudWatch, ELK, etc.)
- Distributed tracing (Zipkin, Jaeger)
- Alerting on critical metrics
- Error tracking (Sentry, Rollbar)

**Scaling:**
- Horizontal scaling (multiple app instances)
- Load balancer (ALB, NGINX)
- Database connection pooling (HikariCP)
- Redis for distributed cache

---

## Development Workflow

### Adding a New Feature

1. **Domain Entity** (if needed)
   - Create entity in appropriate package
   - Add JPA annotations
   - Define business rules in entity methods

2. **Repository** (if needed)
   - Extend `JpaRepository<Entity, UUID>`
   - Add custom query methods if needed

3. **Use Case**
   - Create interface in `{module}/usecase/`
   - Implement business logic
   - Add `@Service` annotation

4. **Controller**
   - Add REST endpoints
   - Map DTOs to domain objects
   - Handle exceptions

5. **DTOs**
   - Create request/response models
   - Add validation annotations
   - Document with `@Schema` for OpenAPI

6. **Test**
   - Write unit tests for use case
   - Mock repository dependencies
   - Test business rules

7. **Migration** (if database changes)
   - Create new Flyway migration
   - Test rollback scenario

### Code Style

**Conventions:**
- Use Lombok to reduce boilerplate (`@Data`, `@Builder`)
- Constructor injection (not `@Autowired`)
- Package-private classes unless API
- Immutable DTOs (records in Java 25)
- Descriptive variable names

**Naming:**
- Use Cases: `{Verb}{Noun}UseCase` (e.g., `CreateTransactionUseCase`)
- Controllers: `{Noun}Controller` (e.g., `TransactionController`)
- Repositories: `{Entity}Repository` (e.g., `TransactionRepository`)
- DTOs: `{Noun}Request/Response` (e.g., `CreateTransactionRequest`)

---

## Future Enhancements

**Planned Features:**
- Recurring transactions (subscriptions, bills)
- Budget tracking and alerts
- Multi-currency support with exchange rates
- Receipt OCR scanning
- Mobile app (React Native)
- Bank account integration (Plaid)
- Tax report generation

**Technical Improvements:**
- GraphQL API (alternative to REST)
- Redis caching (distributed)
- Elasticsearch (full-text search)
- Kubernetes deployment
- CI/CD pipeline (GitHub Actions)
- A/B testing framework

---

## Additional Resources

### Documentation:
- `/documentations/SPRING_BOOT_4_UPGRADE.md` - Spring Boot 4.0 upgrade plan
- `/documentations/docker/` - Docker deployment guides
- `/project_plan/` - Feature development milestones

### API Documentation:
- Swagger UI: `http://localhost:8081/api/v1/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api/v1/v3/api-docs`

### Monitoring:
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Actuator: `http://localhost:8081/api/v1/actuator`

### Support:
- GitHub Issues: (to be added)
- Email: (to be added)

---

**Document Version:** 1.0
**Last Updated:** December 17, 2025
**Maintained By:** Development Team
