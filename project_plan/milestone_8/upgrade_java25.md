# Java 25 & Spring Boot 4.0 Upgrade Implementation Plan

## Executive Summary

This document outlines the implementation plan and impact analysis for upgrading the Expense Tracker application from Java 21 + Spring Boot 3.5.7 to Java 25 + Spring Boot 4.0. The upgrade aims to leverage performance improvements, new language features, enhanced runtime optimizations, and Spring Framework 7.0 features.

**Current Versions:**
- Java 21 (LTS)
- Spring Boot 3.5.7
- Spring Framework 6.x

**Target Versions:**
- Java 25 (LTS - Expected September 2025)
- Spring Boot 4.0.0 (Expected November 2025)
- Spring Framework 7.0

**Estimated Effort:** 5-7 days (increased due to Spring Boot major version upgrade)
**Risk Level:** Medium (due to Spring Boot 3.x → 4.0 migration)

---

## 1. Benefits & Motivation

### 1.1 Java 25 Performance Improvements

**Generational ZGC (JEP 439 - Final in Java 23)**
- Reduced latency for garbage collection pauses
- Better throughput for high-memory applications
- Improved handling of large heap sizes (beneficial for caching and report generation)

**Vector API Enhancements (Incubating)**
- Potential performance gains in numerical operations
- Useful for financial calculations and bulk data processing

**JVM Optimizations**
- Better C2 compiler optimizations
- Improved escape analysis and inlining
- Enhanced string handling and concatenation

### 1.1b Spring Boot 4.0 / Spring Framework 7.0 Benefits

**Performance Enhancements**
- Improved startup time and memory efficiency
- Better resource management and connection pooling
- Enhanced reactive programming support

**Framework Improvements**
- Complete modularization of Spring Boot codebase
- Better observability and metrics integration
- Enhanced AOT (Ahead-of-Time) compilation support
- Improved GraalVM native image support

**Developer Experience**
- Modern API improvements
- Better error messages and diagnostics
- Enhanced configuration validation
- Improved testing support

### 1.2 Language Features

**String Templates (Preview in Java 21, potentially final in Java 25)**
```java
// Current
String query = "SELECT * FROM transactions WHERE user_id = " + userId + " AND date = '" + date + "'";

// With String Templates
String query = STR."SELECT * FROM transactions WHERE user_id = \{userId} AND date = '\{date}'";
```

**Unnamed Patterns and Variables (Preview)**
- Cleaner code with intentionally unused variables
- Better pattern matching expressions

**Scoped Values (Preview)**
- More efficient than ThreadLocal for request-scoped data
- Useful for correlation IDs and security context

**Structured Concurrency (Preview)**
- Better async/await patterns
- Improved error handling in concurrent operations

### 1.3 Security Enhancements
- Latest security patches and fixes
- Enhanced cryptographic algorithms
- Improved TLS support

---

## 2. Impact Analysis

### 2.1 Dependency Compatibility

| Dependency | Current Version | Java 25 Compatible | Action Required |
|------------|----------------|-------------------|-----------------|
| Spring Boot | 3.5.7 |  Yes | None |
| Spring Data JPA | (via Boot) |  Yes | None |
| Spring Security | (via Boot) |  Yes | None |
| PostgreSQL Driver | Latest |  Yes | None |
| Lombok | Latest |  Yes | None |
| Flyway | 9.22.0 |  Yes | Test migrations |
| JJWT | 0.11.5 |  Yes | None |
| Springdoc OpenAPI | 2.8.14 |  Yes | None |
| Logback | (via Boot) |  Yes | None |
| Micrometer | (via Boot) |  Yes | None |
| Apache POI | 5.2.5 |  Yes | None |
| OpenPDF | 2.0.2 |  Yes | None |
| Caffeine Cache | (via Boot) |  Yes | None |
| Hypersistence Utils | 3.7.3 | � Check | Verify compatibility |

**Note:** Java 25 is an LTS release expected in September 2025. Spring Boot 3.5.x officially supports Java 21 (LTS) and should support Java 25 LTS. We should verify Spring Boot's Java 25 compatibility through testing.

**CRITICAL UPDATE:** For full Java 25 LTS support, **Spring Boot 4.0 is required** (available November 2025).

### 2.2 Spring Boot 4.0 Breaking Changes & Migration

Spring Boot 4.0 is a major version upgrade with significant breaking changes. The following section outlines critical changes that will impact the application.

#### 2.2.1 Major Breaking Changes

**1. Dependency Updates (Mandatory)**
- **Spring Framework 7.0** (from 6.x)
- **Jakarta EE 11** alignment
- **Java 17 minimum**, Java 21+ recommended, **Java 25 fully supported**
- **Jackson 3.x** (from 2.x) - Breaking serialization changes
- **JUnit 6** (from JUnit 5)
- Many other dependency major version updates

**2. Deprecated API Removals**
Spring Boot 4.0 removes **88% of all deprecations** from 2.x and 3.x:
- `@MockBean` and `@SpyBean` (deprecated in 3.4, removed in 4.0)
  - **Impact:** All test files using these annotations must be updated
  - **Alternative:** Use Spring's native testing support or Mockito directly
- Legacy security configuration APIs
  - **Impact:** Security configuration may need refactoring
- Various deprecated utility classes and methods

**3. Jackson 3.x Migration** (HIGH IMPACT)
- Removed annotations and stricter type handling
- Module consolidation
- Changes to `ObjectMapper` behavior
- **Impact Areas in our app:**
  - JSON serialization in REST controllers
  - Request/Response DTOs
  - Custom Jackson configurations
  - Date/time serialization

**4. Complete Modularization**
- Spring Boot codebase completely modularized
- Smaller, more focused JARs
- **Impact:** Dependency declarations may need adjustment

**5. Embedded Server Changes**
- **Undertow removed** as embedded server option
- **Impact:** We use default Tomcat, so no impact

**6. Configuration Property Changes**
- Jackson properties moved:
  - `spring.jackson.read.*` → `spring.jackson.json.read.*`
  - `spring.jackson.write.*` → `spring.jackson.json.write.*`
- **Impact:** Review `application.properties` / `application.yml`

**7. Testing Framework Changes**
- JUnit 6 migration required
- Testing annotations updated
- Mock frameworks changes
- **Impact:** All test files need review and potential updates

#### 2.2.2 Application-Specific Impact Assessment

**Files Requiring Updates:**

| Area | Impact Level | Estimated Changes |
|------|--------------|-------------------|
| Test files (`@MockBean`/`@SpyBean`) | 🔴 High | ~15-20 test classes |
| Security configuration | 🟡 Medium | 1-2 security config classes |
| Jackson custom serializers | 🟡 Medium | Date serializers, custom DTOs |
| Application properties | 🟢 Low | Configuration file updates |
| REST Controllers | 🟢 Low | Mostly compatible |
| JPA Entities | 🟢 Low | Mostly compatible |
| Service layer | 🟢 Low | Mostly compatible |

**Estimated Effort:**
- Code changes: 3-4 days
- Testing and validation: 2-3 days
- **Total additional effort:** 5-7 days (on top of Java upgrade)

#### 2.2.3 Migration Strategy

**Official Migration Path (Recommended by Spring Team):**
1. **First:** Upgrade to Spring Boot 3.5.x (latest) - ALREADY DONE ✅
2. **Second:** Review deprecation warnings and fix them
3. **Third:** Upgrade to Spring Boot 4.0

**References:**
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Boot 4: 7 Breaking Changes](https://medium.com/@pmLearners/spring-boot-4-the-7-breaking-changes-every-developer-must-know-99de4c2b60e2)

### 2.3 Build System Impact

**Gradle Compatibility**
- Gradle 8.5+ required for full Java 25 support
- Current project uses Gradle wrapper (version needs verification)
- Action: Update Gradle wrapper if needed

**Gradle Plugins**
- Spring Boot Gradle Plugin: 3.5.7 → **4.0.0** (REQUIRED)
- Dependency Management Plugin: 1.1.7 → Latest compatible version

### 2.3 Docker & Deployment Impact

**Base Images**
- Build Image: `eclipse-temurin:21-jdk` � `eclipse-temurin:25-jdk`
- Runtime Image: `eclipse-temurin:21-jre` � `eclipse-temurin:25-jre`
- Action: Update Dockerfile

**Container Size**
- Expected minimal change in image size
- JRE size typically 180-220 MB

**Deployment Environments**
- Development: Local Java 25 installation required
- Staging/Production: Docker-based (no host Java version dependency)

### 2.4 Code Impact

**Low Risk Areas** (Expected to work without changes)
-  REST Controllers and API endpoints
-  JPA entities and repositories
-  Service layer business logic
-  Security configuration
-  Validation annotations
-  Exception handling
-  Logging and monitoring

**Medium Risk Areas** (Require testing)
- � JDBC operations and database connectivity
- � Reflection-based operations (JSON serialization, Lombok)
- � JWT token handling
- � File export operations (CSV, Excel, PDF)
- � WebFlux/WebClient for Midtrans integration
- � AOP aspects and proxying

**Preview Features** (Optional adoption)
- =
 String Templates for SQL query building
- =
 Scoped Values for correlation ID handling
- =
 Structured Concurrency for async operations

### 2.5 Testing Impact

**Unit Tests**
- Expected to run without modifications
- Test coverage should remain unchanged

**Integration Tests**
- Database integration tests need verification
- Security tests need verification
- API endpoint tests should work as-is

**Performance Tests**
- Baseline comparison needed (Java 21 vs Java 25)
- Focus areas: API response time, GC pauses, memory usage

---

## 3. Implementation Plan

### Phase 1: Environment Preparation (Day 1)

**Step 1.1: Install Java 25**
```bash
# macOS (Homebrew)
brew install openjdk@25
sudo ln -sfn $(brew --prefix)/opt/openjdk@25/libexec/openjdk.jdk \
  /Library/Java/JavaVirtualMachines/openjdk-25.jdk

# Verify installation
java -version  # Should show Java 25
```

**Step 1.2: Update Gradle Wrapper**
```bash
# Check current Gradle version
./gradlew --version

# Update to latest Gradle (8.5+)
./gradlew wrapper --gradle-version=8.11 --distribution-type=bin
```

**Step 1.3: Create Feature Branch**
```bash
git checkout -b upgrade/java-25
```

### Phase 2: Configuration Updates (Day 1)

**Step 2.1: Update build.gradle**
```gradle
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

**Step 2.2: Update Dockerfile**
```dockerfile
# Stage 1: Build
FROM eclipse-temurin:25-jdk AS build

# Stage 2: Runtime
FROM eclipse-temurin:25-jre
```

**Step 2.3: Update Documentation**
- README.md: Update Java version requirement
- Development setup documentation
- Deployment documentation

### Phase 3: Testing & Validation (Day 2)

**Step 3.1: Compile Verification**
```bash
./gradlew clean compileJava compileTestJava
```

**Step 3.2: Run Unit Tests**
```bash
./gradlew test
```

**Step 3.3: Run Integration Tests**
```bash
# Start PostgreSQL (if needed)
docker run -d --name postgres-test \
  -e POSTGRES_PASSWORD=test \
  -e POSTGRES_DB=expensetracker_test \
  -p 5432:5432 \
  postgres:16

# Run tests
./gradlew test integrationTest
```

**Step 3.4: Build Docker Image**
```bash
docker build -t expense-tracker:java25 .

# Verify image
docker run --rm expense-tracker:java25 java -version
```

**Step 3.5: Manual Testing Checklist**
- [ ] Application starts successfully
- [ ] User authentication (register, login, refresh token)
- [ ] Wallet CRUD operations
- [ ] Category CRUD operations
- [ ] Transaction CRUD operations
- [ ] Debt management features
- [ ] Export functionality (CSV, Excel, PDF)
- [ ] Financial reports and summaries
- [ ] Subscription features
- [ ] Midtrans payment integration
- [ ] Actuator endpoints (/health, /metrics, /prometheus)
- [ ] Swagger UI accessible

### Phase 4: Performance Benchmarking (Day 2-3)

**Step 4.1: Baseline Metrics (Java 21)**
```bash
# Run with Java 21
# Capture metrics:
# - Application startup time
# - Memory usage (heap, non-heap)
# - GC statistics (pause time, frequency)
# - API response times (p50, p95, p99)
# - Database query performance
# - Export operation duration
```

**Step 4.2: New Metrics (Java 25)**
```bash
# Run with Java 25
# Compare same metrics
# Document improvements/regressions
```

**Step 4.3: Load Testing**
```bash
# Use Apache JMeter, Gatling, or k6
# Test scenarios:
# - 100 concurrent users
# - 1000 transactions/minute
# - Bulk export operations
# - Report generation under load
```

### Phase 5: Optimization (Day 3)

**Step 5.1: JVM Tuning**
```bash
# Enable Generational ZGC
-XX:+UseZGC
-XX:+ZGenerational

# GC logging
-Xlog:gc*:file=/app/logs/gc.log:time,uptime:filecount=5,filesize=10M

# Memory settings
-Xms512m
-Xmx2g
```

**Step 5.2: Adopt Preview Features (Optional)**

**String Templates Example:**
```java
// In error messages
String error = STR."Transaction \{txId} not found for user \{userId}";

// In logging
log.info(STR."Processing transaction: id=\{tx.getId()}, amount=\{tx.getAmount()}");

// In SQL query building (with caution - prefer JPA)
// Not recommended for production without proper sanitization
```

**Scoped Values Example:**
```java
// Replace ThreadLocal for correlation IDs
public class CorrelationContext {
    private static final ScopedValue<String> CORRELATION_ID =
        ScopedValue.newInstance();

    public static void runWithCorrelationId(String id, Runnable task) {
        ScopedValue.where(CORRELATION_ID, id).run(task);
    }

    public static String get() {
        return CORRELATION_ID.orElse(null);
    }
}
```

**Step 5.3: Code Quality Review**
- Remove deprecated API usages (if any)
- Leverage new Java 25 APIs where beneficial
- Update code patterns to modern standards

### Phase 6: Deployment & Rollback Plan (Day 3)

**Step 6.1: Staging Deployment**
```bash
# Build production image
./gradlew bootJar
docker build -t expense-tracker:1.0.0-java25 .

# Deploy to staging
docker-compose -f docker-compose.staging.yml up -d

# Monitor for 24-48 hours
# Check logs, metrics, error rates
```

**Step 6.2: Rollback Plan**
```bash
# If issues occur, rollback is simple:
git checkout main
docker build -t expense-tracker:1.0.0-java21 .
docker-compose up -d

# Or use previous image
docker pull expense-tracker:1.0.0-java21
docker-compose up -d
```

**Step 6.3: Production Deployment**
```bash
# Blue-Green deployment recommended
# 1. Deploy new version alongside old
# 2. Route 10% traffic to new version
# 3. Monitor for issues
# 4. Gradually increase traffic (25%, 50%, 100%)
# 5. Decommission old version
```

---

## 4. Risk Mitigation

### 4.1 Identified Risks

| Risk | Severity | Probability | Mitigation |
|------|----------|-------------|------------|
| Dependency incompatibility | High | Low | Test all dependencies, have fallback versions |
| Runtime errors in production | High | Low | Comprehensive testing, staged rollout |
| Performance regression | Medium | Low | Benchmark before/after, load testing |
| Docker image availability | Medium | Low | Verify Eclipse Temurin 25 images exist |
| Team unfamiliarity | Low | Medium | Documentation, knowledge sharing |
| Preview feature instability | Low | Low | Don't use preview features in production initially |

### 4.2 Success Criteria

-  All unit tests pass (100%)
-  All integration tests pass (100%)
-  Application starts in < 30 seconds
-  No increase in error rates
-  API response times within 10% of baseline
-  GC pause times reduced or maintained
-  Memory usage within acceptable limits
-  Zero critical security vulnerabilities

### 4.3 Rollback Triggers

Immediate rollback if:
- Critical production errors > 0.1%
- API response time degradation > 50%
- Memory leaks detected
- Data corruption issues
- Security vulnerabilities introduced

---

## 5. Post-Upgrade Tasks

### 5.1 Monitoring & Observability

**Enhanced Metrics**
```yaml
# Prometheus metrics to monitor
- jvm_gc_pause_seconds (watch for improvements)
- jvm_memory_used_bytes (watch for efficiency)
- http_server_requests_seconds (API performance)
- hikaricp_connections (database connection pool)
```

**Alerting Rules**
- GC pause time > 100ms
- Memory usage > 80% of max heap
- API p99 latency > 500ms
- Error rate > 1%

### 5.2 Documentation Updates

- [ ] Update README.md with Java 25 requirement
- [ ] Update developer onboarding guide
- [ ] Update deployment documentation
- [ ] Document any new JVM flags or configurations
- [ ] Update performance baseline documentation

### 5.3 Knowledge Transfer

- Share findings with development team
- Document lessons learned
- Create runbook for Java version upgrades
- Update CI/CD pipeline (if applicable)

---

## 6. Alternative Considerations

### 6.1 Java LTS Release Schedule

The Java LTS (Long-Term Support) releases follow this schedule:
- **Java 17 (LTS)** - September 2021 - Support until 2029
- **Java 21 (LTS)** - September 2023 - Support until 2031 ← Currently using
- **Java 25 (LTS)** - September 2025 - Support until 2033 ← Target version

**Why Upgrade to Java 25 LTS:**
1. Next LTS release with 8+ years of support
2. Cumulative performance improvements from Java 22-25
3. Finalized features that were preview in Java 21-24
4. Latest security enhancements and bug fixes
5. Better tooling and ecosystem support over time

### 6.2 Timing Considerations

| Factor | Upgrade Now (Pre-release) | Upgrade Post-September 2025 |
|--------|---------------------------|------------------------------|
| Release Status | Early Access / RC | General Availability (GA) |
| Risk Level | Medium-High | Low |
| Benefits | Early adoption, feedback opportunity | Stable, battle-tested |
| Community Support | Limited | Full ecosystem support |
| Production Ready | Not recommended | Recommended |

**Recommendation:**
- **For production:** Wait until Java 25 LTS GA release (September 2025)
- **For testing/staging:** Begin testing with EA builds (available now)
- **For planning:** Start preparation and impact analysis now (this document)

---

## 7. Timeline

### Conservative Approach (Recommended)

**Option A: Wait for Java 25 LTS GA (September 2025)**
- **Q1-Q2 2025:** Monitor Early Access builds and community feedback
- **Q3 2025:** Java 25 LTS GA released (September)
- **Q3-Q4 2025:** Begin Java 25 LTS upgrade to production
- **Timeline:** Wait 9 months for stable release

**Option B: Early Adoption with EA Builds**
- **Now - Q2 2025:** Test with Early Access builds in staging
- **Q3 2025:** Upgrade to GA when released
- **Timeline:** Start testing now, production upgrade in September

### Aggressive Approach (Not Recommended for Production)

**Immediate Testing with EA Builds**
- **Week 1:** Setup EA build environment
- **Week 2:** Testing and validation
- **Week 3:** Performance benchmarking
- **Week 4:** Document findings
- **Note:** For R&D and staging environments only, not production

---

## 8. Cost-Benefit Analysis

### Costs (Updated for Spring Boot 4.0 Migration)
- Development time: 5-7 days (was 2-3 days)
  - Java 25 upgrade: 1-2 days
  - Spring Boot 4.0 migration: 3-4 days
  - Code refactoring (@MockBean, Jackson 3.x): 1-2 days
- Testing effort: 2-3 days (was 1-2 days)
  - Test file updates: 1-2 days
  - Integration testing: 1 day
- Risk of production issues: Medium (increased due to Boot 4.0)
- Potential need to rollback: Medium
- Learning curve: Medium (Jackson 3.x, JUnit 6, Spring Framework 7)

### Benefits (Updated for Spring Boot 4.0)
- Performance improvements: 10-20% (estimated, combined Java 25 + Boot 4.0)
- Lower GC pause times: 20-40% (with Generational ZGC)
- Faster startup time (Spring Boot 4.0 optimizations)
- Access to modern Java 25 language features
- Spring Framework 7.0 improvements (modularization, AOT)
- Security patches and fixes (Java 25 + Boot 4.0)
- Better developer experience and tooling
- 8+ years LTS support for both Java 25 and Boot 4.0
- Future-proofing codebase with latest stable versions

### ROI Assessment (Updated for Spring Boot 4.0)
- **Java 25 LTS + Spring Boot 4.0 (GA):** Very High benefit
  - 8+ years support for both
  - Significant performance improvements
  - Modern features and better tooling
  - Medium effort but high value
- **Java 25 LTS + Spring Boot 4.0 (EA Testing):** Medium-High benefit
  - Early testing reduces migration risk
  - Not for production until GA
- **Stay on Java 21 + Spring Boot 3.5:** Low effort
  - Miss out on 10-20% performance gains
  - Eventually must upgrade when support ends
  - Technical debt accumulation

---

## 9. Recommendation (Updated for Spring Boot 4.0)

### Primary Recommendation: **WAIT FOR BOTH GA RELEASES (November 2025)**

**Timeline:**
- **Java 25 LTS GA:** September 2025
- **Spring Boot 4.0.0 GA:** November 2025 ✅ **Wait for this**

**Rationale:**
1. **Both are LTS releases** with support until 2033+
2. **Combined migration is more efficient** than two separate upgrades
3. **Spring Boot 4.0 is REQUIRED** for full Java 25 support
4. **Lower risk** waiting for GA releases and community feedback
5. **Ecosystem maturity** - libraries will have Boot 4.0 compatible versions

**Recommended Approach:**
- **September 2025:** Java 25 LTS GA released - wait for Boot 4.0
- **November 2025:** Spring Boot 4.0 GA released - **START MIGRATION**
- **December 2025:** Complete migration with both upgrades together

### Secondary Recommendation: Phased Testing (Optional but Recommended)

**Phase 1: Java 25 EA Testing (Q1-Q2 2025)**
1. Set up testing environment with Java 25 EA + Spring Boot 3.5.7
2. Run tests to identify Java-specific issues
3. Limited scope - just Java compatibility

**Phase 2: Spring Boot 4.0 Planning (Q3 2025)**
1. Review official migration guide when available
2. Audit code for deprecated APIs (@MockBean, @SpyBean)
3. Plan Jackson 3.x migration strategy
4. Identify all test files requiring updates

**Phase 3: Combined Migration (Q4 2025)**
1. Upgrade both Java 25 + Spring Boot 4.0 simultaneously
2. Execute full migration plan
3. Complete testing and deployment

**Benefits of Phased Approach:**
- Early identification of Java 25 compatibility issues
- Time to plan Spring Boot 4.0 breaking changes
- Smoother combined migration
- Reduced production risk

### ⚠️ Important: Do NOT Upgrade Incrementally

**Avoid this approach:**
- ❌ Upgrade to Java 25 now with Boot 3.5
- ❌ Then upgrade to Boot 4.0 later

**Why:**
- Boot 3.5 has limited Java 25 support
- You'll do migration work twice
- Higher risk of compatibility issues
- More downtime and testing effort

**Instead:**
- ✅ Wait for both GA releases
- ✅ Migrate Java 25 + Boot 4.0 together in one effort
- ✅ Test once, deploy once

---

## 10. Sign-off

**Prepared By:** Claude Code
**Date:** 2025-12-12
**Version:** 2.0 (Updated to include Spring Boot 4.0 migration)
**Status:** Draft - Pending Review

**Major Update:**
This document has been significantly updated to reflect the requirement for Spring Boot 4.0 migration alongside Java 25 upgrade. The effort estimate has increased from 2-3 days to 5-7 days due to Spring Boot breaking changes.

**Approvals Required:**
- [ ] Technical Lead
- [ ] DevOps Team
- [ ] Product Owner
- [ ] Security Team

---

## Appendix A: Useful Commands Reference

```bash
# Check Java version
java -version
javac -version

# Check Gradle version
./gradlew --version

# List available Java versions (macOS)
/usr/libexec/java_home -V

# Switch Java version (macOS)
export JAVA_HOME=$(/usr/libexec/java_home -v 25)

# Clean build
./gradlew clean build

# Run with specific JVM flags
java -XX:+UseZGC -XX:+ZGenerational -jar expense-tracker.jar

# Docker build with no cache
docker build --no-cache -t expense-tracker:java25 .

# Check Docker image size
docker images expense-tracker

# Monitor JVM in container
docker exec -it <container_id> jps -lvm
docker exec -it <container_id> jstat -gc <pid> 1000
```

## Appendix B: Key Java 25 Features

### Finalized JEPs (from Java 22-25)
- JEP 456: Unnamed Variables & Patterns
- JEP 459: String Templates (Preview)
- JEP 464: Scoped Values (Second Preview)
- JEP 462: Structured Concurrency (Second Preview)
- JEP 466: Class-File API (Second Preview)
- JEP 467: Markdown Documentation Comments

### Performance JEPs
- JEP 439: Generational ZGC (finalized in Java 23)
- JEP 460: Vector API (Seventh Incubator)

## Appendix C: Contact & Resources

**Official Resources:**

*Java 25:*
- Java 25 Release Notes: https://openjdk.org/projects/jdk/25/
- Eclipse Temurin Downloads: https://adoptium.net/
- New Features in Java 25: https://www.baeldung.com/java-25-features

*Spring Boot 4.0:*
- Spring Boot 4.0 Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
- Spring Boot 4.0 Release Notes: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes
- Spring Boot 4.0 RC1 Release Notes: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0.0-RC1-Release-Notes
- Spring Boot 4.0.0 GA Announcement: https://spring.io/blog/2025/11/20/spring-boot-4-0-0-available-now/
- Spring Framework 7.0 GA: https://spring.io/blog/2025/11/13/spring-framework-7-0-general-availability/
- Spring Boot 4 Breaking Changes: https://medium.com/@pmLearners/spring-boot-4-the-7-breaking-changes-every-developer-must-know-99de4c2b60e2
- Spring Framework Versions: https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-Versions
- Spring Boot Compatibility: https://spring.io/projects/spring-boot
- Spring Boot End of Life: https://endoflife.date/spring-boot

**Internal Resources:**
- Project Repository: [current repo]
- CI/CD Pipeline: [if applicable]
- Deployment Docs: [if applicable]

**Escalation:**
- Technical Issues: [Team Lead]
- Production Issues: [DevOps/SRE]
- Security Concerns: [Security Team]