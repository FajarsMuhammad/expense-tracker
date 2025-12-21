# Spring Boot 4.0 Upgrade Implementation Plan

## Executive Summary

Plan komprehensif untuk upgrade aplikasi Expense Tracker dari **Spring Boot 3.5.7** ke **Spring Boot 4.0** dengan **Java 25 LTS**. Berdasarkan analisis mendalam terhadap codebase, migrasi ini memiliki kompleksitas **MEDIUM** dengan effort estimate **5 hari** (conservative approach).

**Timeline:** December 2025 (Spring Boot 4.0 sudah GA sejak November 2025)

**Scope:** Migration only (minimal scope) - tidak include additional test coverage

**Kabar Baik:**
- Testing layer sudah fully compatible (0 file perlu diubah untuk @MockBean/@SpyBean migration)
- No breaking changes untuk API/business logic

**Critical Decision:**
- Replace Hypersistence Utils dengan custom JSONB converter (future-proof, no technical debt)

**Current Stack:**
- Java 25 (virtual threads enabled) ✅
- Spring Boot 3.5.7
- Spring Framework 6.x
- Jackson 2.x
- JJWT 0.11.5
- Flyway 9.22.0

**Target Stack:**
- Java 25 (LTS) - Already upgraded ✅
- Spring Boot 4.0.0
- Spring Framework 7.0
- Jackson 3.x
- JJWT 0.12.x
- Flyway 10.x

---

## Impact Analysis Summary

### Areas Requiring Changes

| Area | Impact Level | Files Affected | Effort (Days) |
|------|--------------|----------------|---------------|
| Testing (@MockBean/@SpyBean) | ✅ NONE | 0 files | 0 days |
| Dependencies Upgrade | 🟡 MEDIUM | 1 file | 0.5 day |
| JJWT API Migration | 🔴 CRITICAL | 1 file | 0.5 day |
| Custom JSONB Converter | 🟡 HIGH | 2 files (1 new) | 0.5 day |
| Spring Security CORS | 🟡 MEDIUM | 1 file | 0.5 day |
| Jackson 3.x Verification | 🟡 MEDIUM | 13 files | 1 day |
| Testing & Validation | 🟡 MEDIUM | All | 2 days |

**Total Estimated Effort:** 5 days (conservative approach)

---

## Phase 1: Dependency Updates

### 1.1 Update build.gradle

**File:** `/Users/fajars/Documents/development/java/expense-tracker/build.gradle`

**Changes Required:**

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.0.0'  // FROM: 3.5.7
    id 'io.spring.dependency-management' version '1.1.7'  // Verify latest
}

dependencies {
    // JJWT: Update to 0.12.x for Spring Boot 4.0 compatibility
    implementation 'io.jsonwebtoken:jjwt-api:0.12.5'      // FROM: 0.11.5
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'        // FROM: 0.11.5
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'     // FROM: 0.11.5

    // Flyway: Update to 10.x for Spring Boot 4.0
    implementation 'org.flywaydb:flyway-core:10.20.1'     // FROM: 9.22.0
    implementation 'org.flywaydb:flyway-database-postgresql:10.20.1'  // NEW: Required in Flyway 10

    // REMOVE: Hypersistence Utils (no Jackson 3.x support - will be replaced with custom converter)
    // implementation 'io.hypersistence:hypersistence-utils-hibernate-63:3.7.3'  // REMOVED

    // All other dependencies remain unchanged
}
```

**Rationale:**
- Spring Boot 4.0: Major version upgrade with Jackson 3.x support
- JJWT 0.12.x: Breaking API changes, must upgrade
- Flyway 10.x: Spring Boot 4.0 compatibility, requires postgresql-specific module
- **Hypersistence Utils REMOVED:** No official Jackson 3.x support as of December 2025, replaced with custom JSONB converter (see Phase 2.2)

---

## Phase 2: Code Changes

### 2.1 JJWT API Migration (CRITICAL)

#### Update JwtUtil.java

**File:** `/Users/fajars/Documents/development/java/expense-tracker/src/main/java/com/fajars/expensetracker/common/util/JwtUtil.java`

**Breaking Changes in JJWT 0.12.x:**

#### Change 1: Token Generation (Lines 39-44)

**BEFORE (0.11.5):**
```java
return Jwts.builder()
    .setSubject(username)
    .setIssuedAt(new Date())
    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
    .signWith(key, SignatureAlgorithm.HS256)
    .compact();
```

**AFTER (0.12.x):**
```java
return Jwts.builder()
    .subject(username)                    // Changed: setSubject() → subject()
    .issuedAt(new Date())                 // Changed: setIssuedAt() → issuedAt()
    .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // Changed: setExpiration() → expiration()
    .signWith(key)                        // Changed: signWith(key, algorithm) → signWith(key)
    .compact();
```

#### Change 2: Token Parsing (Lines 63-67)

**BEFORE (0.11.5):**
```java
return Jwts.parserBuilder()
    .setSigningKey(key)
    .build()
    .parseClaimsJws(token)
    .getBody();
```

**AFTER (0.12.x):**
```java
return Jwts.parser()                      // Changed: parserBuilder() → parser()
    .verifyWith(key)                      // Changed: setSigningKey() → verifyWith()
    .build()
    .parseSignedClaims(token)             // Changed: parseClaimsJws() → parseSignedClaims()
    .getPayload();                        // Changed: getBody() → getPayload()
```

#### Change 3: Import Statements

**Remove:**
```java
import io.jsonwebtoken.SignatureAlgorithm;
```

**Keep/Add:**
```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import javax.crypto.SecretKey;
```

### 2.2 Create Custom JSONB Converter (NEW - Replacing Hypersistence Utils)

**Context:** Hypersistence Utils tidak support Jackson 3.x, jadi kita akan create custom JPA AttributeConverter untuk handle JSONB columns dengan Jackson 3.x.

#### Step 1: Create JsonbConverter Class (NEW FILE)

**File:** `/Users/fajars/Documents/development/java/expense-tracker/src/main/java/com/fajars/expensetracker/common/converter/JsonbConverter.java`

**Implementation:**
```java
package com.fajars.expensetracker.common.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JPA AttributeConverter for converting Map<String, Object> to/from PostgreSQL JSONB.
 * Uses Jackson 3.x ObjectMapper for serialization/deserialization.
 *
 * This replaces Hypersistence Utils which doesn't support Jackson 3.x yet.
 */
@Slf4j
@Component
@Converter(autoApply = false)  // Explicitly apply to specific fields
public class JsonbConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper objectMapper;

    // Constructor injection to use the configured ObjectMapper
    public JsonbConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Convert Map to JSON string for database storage.
     */
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting Map to JSON string", e);
            throw new IllegalArgumentException("Error converting Map to JSON", e);
        }
    }

    /**
     * Convert JSON string from database to Map.
     */
    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON string to Map", e);
            throw new IllegalArgumentException("Error converting JSON to Map", e);
        }
    }
}
```

**Key Features:**
- Uses injected `ObjectMapper` bean (configured in `JacksonConfig.java`)
- Handles `null` values gracefully
- Proper error handling with logging
- Type-safe conversion with `TypeReference`
- Compatible with Jackson 3.x

#### Step 2: Update PaymentTransaction Entity

**File:** `/Users/fajars/Documents/development/java/expense-tracker/src/main/java/com/fajars/expensetracker/payment/PaymentTransaction.java`

**Changes Required:**

**Remove Hypersistence Utils imports:**
```java
// REMOVE:
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
```

**Add JPA converter import:**
```java
// ADD:
import com.fajars.expensetracker.common.converter.JsonbConverter;
import jakarta.persistence.Convert;
```

**Update webhookPayload field (Lines 123-125):**

**BEFORE:**
```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "webhook_payload", columnDefinition = "jsonb")
private Map<String, Object> webhookPayload;
```

**AFTER:**
```java
@Convert(converter = JsonbConverter.class)
@Column(name = "webhook_payload", columnDefinition = "jsonb")
private Map<String, Object> webhookPayload;
```

**Update metadata field (Lines 138-140):**

**BEFORE:**
```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb")
private Map<String, Object> metadata;
```

**AFTER:**
```java
@Convert(converter = JsonbConverter.class)
@Column(columnDefinition = "jsonb")
private Map<String, Object> metadata;
```

**Summary of Changes:**
- Replace `@JdbcTypeCode(SqlTypes.JSON)` with `@Convert(converter = JsonbConverter.class)`
- Remove Hypersistence Utils annotations
- Use standard JPA `@Convert` annotation
- No changes to field types or business logic

**Breaking Changes:** NONE ✅
- API interface tetap sama
- Database schema tidak berubah
- Business logic tidak terpengaruh
- Existing tests tetap valid

---

## Phase 3: Spring Security Updates

### 3.1 Fix CORS Configuration

**File:** `/Users/fajars/Documents/development/java/expense-tracker/src/main/java/com/fajars/expensetracker/common/security/SecurityConfig.java`

**Line 60-69: CORS Configuration**

**BEFORE:**
```java
config.addAllowedOrigin("*");  // DEPRECATED in Spring Security 6.x+
```

**AFTER:**
```java
config.addAllowedOriginPattern("*");  // Use pattern-based matching
```

**Full Updated Method:**
```java
@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOriginPattern("*");       // Changed from addAllowedOrigin
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**Rationale:**
- `addAllowedOrigin("*")` deprecated when `allowCredentials(true)` is set
- Will be removed in Spring Framework 7.0
- `addAllowedOriginPattern("*")` is the modern replacement

---

## Phase 4: Jackson 3.x Compatibility

### 4.1 Update JacksonConfig.java

**File:** `/Users/fajars/Documents/development/java/expense-tracker/src/main/java/com/fajars/expensetracker/common/config/JacksonConfig.java`

**Current Implementation (Lines 22-34):**
```java
@Bean
@Primary
public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    // Register JavaTimeModule for LocalDate, LocalDateTime support
    mapper.registerModule(new JavaTimeModule());

    // Configure timezone
    mapper.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));

    // Don't serialize dates as timestamps
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
}
```

**Action for Jackson 3.x:**

**Verification Required:**
1. Check if `JavaTimeModule` explicit registration is still needed (Jackson 3.x includes it by default)
2. Test timezone configuration works correctly
3. Verify WRITE_DATES_AS_TIMESTAMPS behavior unchanged

**Potential Update:**
```java
@Bean
@Primary
public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    // Jackson 3.x may include JavaTimeModule by default
    // Keep this for explicit configuration
    mapper.registerModule(new JavaTimeModule());

    mapper.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    return mapper;
}
```

### 4.2 Update Application Properties

**File:** `/Users/fajars/Documents/development/java/expense-tracker/src/main/resources/application.yaml`

**Lines 18-21: Jackson Configuration**

**BEFORE:**
```yaml
jackson:
  time-zone: Asia/Jakarta
  serialization:
    write-dates-as-timestamps: false
```

**AFTER (Verify if property paths changed):**
```yaml
jackson:
  time-zone: Asia/Jakarta
  serialization:
    write-dates-as-timestamps: false
  # NOTE: According to upgrade docs, some properties moved:
  # spring.jackson.read.* → spring.jackson.json.read.*
  # spring.jackson.write.* → spring.jackson.json.write.*
  # Current properties may not need changes, verify in Spring Boot 4.0 docs
```

**Action:** Test that timezone and date serialization work correctly after upgrade.

### 4.3 Critical: Hypersistence Utils for JSONB

**File:** `/Users/fajars/Documents/development/java/expense-tracker/src/main/java/com/fajars/expensetracker/payment/PaymentTransaction.java`

**Lines 123-125, 138-140: JSONB Columns**

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "webhook_payload", columnDefinition = "jsonb")
private Map<String, Object> webhookPayload;

@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb")
private Map<String, Object> metadata;
```

**CRITICAL DEPENDENCY:**
- Current: `io.hypersistence:hypersistence-utils-hibernate-63:3.7.3`
- Supports: Jackson 2.x

**Action Required:**
1. Research Hypersistence Utils version compatible with Jackson 3.x
2. Likely need version 3.8.x or 4.x
3. Update dependency in build.gradle
4. Test JSONB serialization/deserialization thoroughly

**Testing Priority: HIGH**
- Test saving PaymentTransaction with webhookPayload
- Test retrieving PaymentTransaction and verifying JSONB data
- Test Midtrans webhook processing end-to-end

### 4.4 Verify Jackson Annotations Compatibility

**Files with Jackson Annotations (13 files):**

1. **@JsonFormat Annotations (3 files):**
    - `ExportFilter.java` - Lines 17-18, 21-22
    - `CreateTransactionRequest.java` - Lines 38-39
    - `UpdateTransactionRequest.java` - Lines 38-39

   **Action:** Test date/time formatting with Asia/Jakarta timezone

2. **@JsonProperty Annotations (3 files):**
    - `MidtransWebhookPayload.java` - 11 fields with snake_case mapping
    - `MidtransSnapResponse.java` - 2 fields
    - `MidtransSnapRequest.java` - Multiple nested records

   **Action:** Test Midtrans API integration (request/response serialization)

3. **@JsonInclude Annotation (1 file):**
    - `ErrorResponse.java` - Line 16

   **Action:** Test error responses exclude null fields

4. **ObjectMapper Programmatic Usage:**
    - `ProcessPaymentWebhookUseCase.java` - Lines 154-166

   **Action:** Test webhook payload conversion to Map

5. **WebClient JSON Serialization:**
    - `MidtransClient.java` - Lines 46-51

   **Action:** Test Midtrans Snap API calls

### 4.5 Date/Time Serialization Testing

**Files with LocalDateTime fields (Multiple response DTOs):**
- `TransactionResponse.java`
- `SubscriptionStatusResponse.java`
- `DebtResponse.java`
- `ExportResponse.java`

**Action:**
- Test all REST API endpoints return correct date/time format
- Verify timezone handling (Asia/Jakarta)
- Ensure ISO-8601 format maintained

---

## Phase 5: Testing Strategy

### 5.1 Compile & Build

```bash
# Clean build with new dependencies
./gradlew clean compileJava compileTestJava

# Expected: No compilation errors
```

### 5.2 Unit Tests

```bash
# Run all unit tests
./gradlew test

# Expected: All 21 test classes pass (100% success)
```

**Critical Tests:**
- `JwtUtilTest.java` - Verify JJWT 0.12.x migration

### 5.3 Integration Testing

**Manual Testing Checklist:**

#### Authentication & JWT
- [ ] User registration
- [ ] User login (verify JWT token generation)
- [ ] Token refresh
- [ ] Token validation in protected endpoints

#### Date/Time Serialization
- [ ] Create transaction with LocalDateTime
- [ ] Retrieve transaction (verify date format)
- [ ] Export report with date filters
- [ ] Dashboard with date ranges

#### JSONB Functionality (CRITICAL)
- [ ] Create payment transaction with webhookPayload
- [ ] Retrieve payment transaction
- [ ] Process Midtrans webhook
- [ ] Verify metadata field serialization

#### Midtrans Integration
- [ ] Create Snap transaction (test MidtransSnapRequest serialization)
- [ ] Parse Snap response (test MidtransSnapResponse deserialization)
- [ ] Process webhook (test MidtransWebhookPayload with snake_case fields)

#### Export Functionality
- [ ] Export transactions to CSV
- [ ] Export transactions to Excel
- [ ] Export transactions to PDF
- [ ] Verify date formatting in exports

#### CORS & Security
- [ ] OPTIONS preflight requests
- [ ] Cross-origin requests (if applicable)
- [ ] Actuator endpoints access

### 5.4 Database Migration Testing

```bash
# Test Flyway migrations with version 10.x
docker-compose up -d postgres
./gradlew bootRun

# Expected: All migrations apply successfully
```

**Verify:**
- Check Flyway schema_version table
- Ensure no migration errors in logs

### 5.5 Performance Testing (Optional but Recommended)

**Baseline Comparison:**
- Application startup time
- API response times (p50, p95, p99)
- Memory usage
- GC behavior with virtual threads

**Tools:**
- JMeter, Gatling, or k6 for load testing
- Actuator metrics endpoint
- Prometheus + Grafana monitoring

---

## Phase 6: Rollback Plan

### 6.1 Git Strategy

```bash
# Create feature branch
git checkout -b upgrade/spring-boot-4.0

# Commit changes incrementally
git commit -m "chore: upgrade dependencies to Spring Boot 4.0"
git commit -m "refactor: migrate JJWT API to 0.12.x"
git commit -m "fix: update CORS configuration for Spring Security 7.0"
git commit -m "test: verify Jackson 3.x compatibility"
```

### 6.2 Rollback Procedure

**If issues occur during testing:**

```bash
# Revert to Spring Boot 3.5.7
git checkout feature/upgrade_java25_refactor

# Or revert specific commits
git revert <commit-hash>
```

### 6.3 Rollback Triggers

**Immediate rollback if:**
- Unit tests fail (>5% failure rate)
- JSONB serialization fails
- JWT authentication broken
- Midtrans API integration fails
- Database migrations fail
- Critical production features unusable

---

## Phase 7: Deployment Strategy

### 7.1 Pre-Deployment

1. **Code Review:**
    - Review all changes with team
    - Verify test coverage
    - Check for security issues

2. **Documentation Update:**
    - Update README.md with Spring Boot 4.0 requirement
    - Document breaking changes
    - Update deployment guide

3. **Dependency Audit:**
   ```bash
   ./gradlew dependencies
   # Verify no version conflicts
   ```

### 7.2 Staging Deployment

```bash
# Build production artifact
./gradlew clean bootJar

# Build Docker image
docker build -t expense-tracker:1.0.0-springboot4 .

# Deploy to staging
docker-compose -f docker-compose.staging.yml up -d

# Monitor for 24-48 hours
```

**Monitoring Checklist:**
- Application logs (no errors)
- Actuator health endpoint
- Prometheus metrics
- API response times
- Database connection pool
- Error rates

### 7.3 Production Deployment

**Recommended: Blue-Green Deployment**

1. Deploy new version alongside old version
2. Route 10% traffic to new version
3. Monitor for 4-8 hours
4. Gradually increase traffic (25% → 50% → 100%)
5. Decommission old version

**Alternative: Rolling Deployment**
- Update instances one at a time
- Verify health checks pass
- Continue with next instance

---

## Risk Assessment & Mitigation

### Critical Risks

| Risk | Severity | Probability | Mitigation |
|------|----------|-------------|------------|
| JSONB serialization fails (Hypersistence Utils incompatibility) | 🔴 CRITICAL | Medium | Verify Hypersistence Utils Jackson 3.x support before upgrade; have rollback ready |
| JWT authentication broken (JJWT API changes) | 🔴 CRITICAL | Low | Comprehensive testing; maintain test coverage |
| Midtrans API integration fails (Jackson serialization) | 🔴 HIGH | Medium | Test all Midtrans endpoints; verify snake_case mapping |
| Date/time serialization issues | 🟡 MEDIUM | Low | Test timezone handling; verify ISO-8601 format |
| Flyway migration conflicts | 🟡 MEDIUM | Low | Test migrations in staging first |
| Performance regression | 🟡 MEDIUM | Low | Benchmark before/after; use profiling tools |

### Success Criteria

- ✅ All 21 unit tests pass (100%)
- ✅ All integration tests pass
- ✅ JSONB serialization works correctly
- ✅ JWT authentication functional
- ✅ Midtrans API integration working
- ✅ All exports (CSV/Excel/PDF) generate correctly
- ✅ No increase in error rates
- ✅ API response times within 10% of baseline
- ✅ Application startup time ≤ 30 seconds
- ✅ Zero critical security vulnerabilities

---

## Timeline

### Conservative Approach (Recommended) - 5 Days

**Day 1: Dependencies & JJWT Migration**
- Morning: Update build.gradle (Spring Boot 4.0, JJWT 0.12.x, Flyway 10.x, remove Hypersistence Utils)
- Afternoon: Migrate JJWT API in JwtUtil.java
- Evening: Compile and run unit tests

**Day 2: Custom JSONB Converter & CORS Fix**
- Morning: Create JsonbConverter.java (new custom converter)
- Afternoon: Update PaymentTransaction.java (replace Hypersistence Utils annotations)
- Evening: Fix CORS configuration in SecurityConfig.java

**Day 3: Jackson 3.x Verification & Testing**
- Morning: Verify JacksonConfig.java for Jackson 3.x compatibility
- Afternoon: Test JWT authentication end-to-end
- Evening: Test JSONB serialization (PaymentTransaction with webhookPayload)

**Day 4: Integration Testing**
- Morning: Test Midtrans API integration (request/response serialization with @JsonProperty)
- Afternoon: Test CRUD operations (wallets, categories, transactions, debts)
- Evening: Test export functionality (CSV/Excel/PDF) and date/time serialization

**Day 5: Final Validation & Documentation**
- Morning: Run full test suite (all 21 unit tests + manual integration tests)
- Afternoon: Performance validation, Flyway migrations check
- Evening: Update documentation (README.md, migration guide)
- Prepare for staging deployment

### Aggressive Approach (Higher Risk)

**Total Effort: 3 days**

**Day 1: All Code Changes**
- Update all files (dependencies, JJWT, CORS, Jackson)
- Compile and fix immediate errors

**Day 2: Testing**
- Run unit tests
- Manual integration testing
- Focus on critical paths

**Day 3: Validation & Deployment**
- Final testing
- Staging deployment
- Documentation updates

---

## Implementation Checklist

### Pre-Migration
- [ ] Read Spring Boot 4.0 Migration Guide
- [ ] Create feature branch `upgrade/spring-boot-4.0`
- [ ] Backup production database
- [ ] Document current application behavior

### Code Changes
- [ ] Update build.gradle (Spring Boot, JJWT, Flyway, Hypersistence Utils)
- [ ] Migrate JJWT API in JwtUtil.java
- [ ] Fix CORS configuration in SecurityConfig.java
- [ ] Verify JacksonConfig.java for Jackson 3.x
- [ ] Update application.yaml if needed

### Testing
- [ ] Run `./gradlew clean compileJava compileTestJava`
- [ ] Run `./gradlew test` (all unit tests)
- [ ] Test JWT authentication (register, login, refresh)
- [ ] Test JSONB serialization (PaymentTransaction)
- [ ] Test Midtrans API integration
- [ ] Test date/time serialization (all DTOs)
- [ ] Test export functionality (CSV, Excel, PDF)
- [ ] Test CORS functionality
- [ ] Verify Flyway migrations

### Documentation
- [ ] Update README.md (Spring Boot 4.0 requirement)
- [ ] Document breaking changes
- [ ] Update deployment guide
- [ ] Document rollback procedure

### Deployment
- [ ] Build Docker image with Spring Boot 4.0
- [ ] Deploy to staging environment
- [ ] Monitor for 24-48 hours
- [ ] Deploy to production (blue-green or rolling)

---

## Critical Files Reference

### Files to Modify (7 files total: 6 existing + 1 new)

1. **build.gradle** (Dependencies)
    - `/Users/fajars/Documents/development/java/expense-tracker/build.gradle`
    - Lines 2, 33-35, 61
    - Update: Spring Boot 4.0, JJWT 0.12.x, Flyway 10.x, remove Hypersistence Utils

2. **JwtUtil.java** (JJWT API Migration - CRITICAL)
    - `/Users/fajars/Documents/development/java/expense-tracker/src/main/java/com/fajars/expensetracker/common/util/JwtUtil.java`
    - Lines 39-44 (Token generation), Lines 63-67 (Token parsing)
    - Update: New JJWT 0.12.x builder API

3. **JsonbConverter.java** (NEW FILE - Custom JSONB Converter)
    - `/Users/fajars/Documents/development/java/expense-tracker/src/main/java/com/fajars/expensetracker/common/converter/JsonbConverter.java`
    - Create new JPA AttributeConverter for JSONB support with Jackson 3.x
    - Replaces Hypersistence Utils

4. **PaymentTransaction.java** (Replace Hypersistence Utils)
    - `/Users/fajars/Documents/development/java/expense-tracker/src/main/java/com/fajars/expensetracker/payment/PaymentTransaction.java`
    - Lines 8-9 (imports), Lines 123-125 (webhookPayload), Lines 138-140 (metadata)
    - Update: Replace @JdbcTypeCode with @Convert(converter = JsonbConverter.class)

5. **SecurityConfig.java** (CORS Fix)
    - `/Users/fajars/Documents/development/java/expense-tracker/src/main/java/com/fajars/expensetracker/common/security/SecurityConfig.java`
    - Lines 60-69 (CORS configuration)
    - Update: addAllowedOrigin() → addAllowedOriginPattern()

6. **JacksonConfig.java** (Verification Only)
    - `/Users/fajars/Documents/development/java/expense-tracker/src/main/java/com/fajars/expensetracker/common/config/JacksonConfig.java`
    - Lines 22-34 (ObjectMapper configuration)
    - Verify Jackson 3.x compatibility (likely no changes needed)

7. **application.yaml** (Verification Only)
    - `/Users/fajars/Documents/development/java/expense-tracker/src/main/resources/application.yaml`
    - Lines 18-21 (Jackson properties)
    - Verify property paths for Jackson 3.x (likely no changes needed)

### Files to Test (13 files with Jackson usage)

1. Payment Integration (5 files):
    - `PaymentTransaction.java` (JSONB columns) - CRITICAL
    - `ProcessPaymentWebhookUseCase.java` (ObjectMapper usage)
    - `MidtransWebhookPayload.java` (@JsonProperty)
    - `MidtransSnapRequest.java` (@JsonProperty with records)
    - `MidtransSnapResponse.java` (@JsonProperty)

2. Request/Response DTOs (8 files):
    - `ExportFilter.java` (@JsonFormat)
    - `CreateTransactionRequest.java` (@JsonFormat)
    - `UpdateTransactionRequest.java` (@JsonFormat)
    - `TransactionResponse.java` (LocalDateTime)
    - `SubscriptionStatusResponse.java` (LocalDateTime)
    - `DebtResponse.java` (LocalDateTime)
    - `ExportResponse.java` (LocalDateTime)
    - `ErrorResponse.java` (@JsonInclude)

---

## References

### Official Documentation
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Framework 7.0 GA](https://spring.io/blog/2025/11/13/spring-framework-7-0-general-availability/)
- [JJWT 0.12.0 Release Notes](https://github.com/jwtk/jjwt/releases/tag/0.12.0)
- [Jackson 3.0 Migration Guide](https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0)
- [Flyway 10 Release Notes](https://documentation.red-gate.com/fd/release-notes-for-flyway-engine-179732572.html)

### Internal Documentation
- Project plan: `project_plan/milestone_8/upgrade_java25.md`
- Current application: `README.md`
- API documentation: Swagger UI at `/api/v1/swagger-ui.html`

---

## Post-Migration Tasks

### Immediate
1. Monitor application logs for errors
2. Check Actuator health endpoint
3. Verify Prometheus metrics
4. Test critical user flows

### Week 1
1. Monitor error rates
2. Track API performance metrics
3. Review GC logs
4. Collect user feedback

### Week 2-4
1. Performance optimization if needed
2. Adopt new Spring Boot 4.0 features
3. Update team documentation
4. Knowledge sharing session

---

## Conclusion

Upgrade ke Spring Boot 4.0 memiliki kompleksitas **MEDIUM** dengan effort **3-5 hari**. Kabar baiknya, testing layer sudah compatible dan tidak ada @MockBean/@SpyBean yang perlu dimigrasi.

**Critical Focus Areas:**
1. JJWT API migration (breaking changes)
2. Hypersistence Utils compatibility dengan Jackson 3.x (untuk JSONB)
3. Midtrans API integration testing
4. Date/time serialization testing

**Recommended Timeline:** 5 hari untuk conservative approach dengan testing menyeluruh.

**Next Steps:**
1. Review plan ini dengan tim
2. Set timeline untuk migration
3. Create backup sebelum mulai
4. Execute phase by phase dengan testing incremental
