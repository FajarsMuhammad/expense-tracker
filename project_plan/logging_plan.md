# Logging, Monitoring & Infrastructure Plan

## 📋 Overview

Plan ini mencakup implementation logging, monitoring, error tracking, dan infrastructure setup untuk production-ready application.

---

## 1️⃣ Application Logging

### Dependencies Setup

* [x] Add Logback configuration (already included in Spring Boot)
* [x] Add structured logging encoder
  ```gradle
  implementation 'net.logstash.logback:logstash-logback-encoder:7.4'
  ```
* [x] Add MDC (Mapped Diagnostic Context) for correlation IDs

### Logback Configuration

* [x] Create `logback-spring.xml` in `src/main/resources/`
* [x] Configure console appender for development
  * [x] Pattern with timestamp, level, thread, logger, message
  * [x] Colorized output for easier reading
* [x] Configure file appender for production
  * [x] Rolling policy (daily rotation)
  * [x] Max file size: 100MB
  * [x] Keep last 30 days
  * [x] JSON format for production
* [x] Configure separate file for error logs
  * [x] ERROR level and above only
  * [x] Easier troubleshooting

### Log Levels Configuration

* [x] Set default level to INFO
* [x] Set DEBUG level for development profile
* [x] Set specific loggers:
  * [x] `com.fajars.expensetracker` → DEBUG/INFO
  * [x] `org.springframework.web` → INFO
  * [x] `org.springframework.security` → INFO
  * [x] `org.hibernate.SQL` → DEBUG (development only)
  * [x] `org.hibernate.type.descriptor.sql.BasicBinder` → TRACE (dev only)

### Structured Logging Implementation

* [x] Create `LoggingAspect` with AOP
  * [x] Log all controller method entries
  * [x] Log execution time
  * [x] Log parameters (sanitized)
* [x] Add correlation ID filter
  * [x] Generate unique request ID
  * [x] Add to MDC
  * [x] Include in response headers
* [x] Create `SecurityLoggingFilter`
  * [x] Log authentication attempts
  * [x] Log authorization failures
  * [x] Track suspicious activities
* [x] Implement sensitive data filtering (enhanced with SensitiveDataFilter utility)
  * [x] Mask passwords in logs
  * [x] Mask credit card numbers
  * [x] Mask JWT tokens
  * [x] Mask email (partial)

### Business Event Logging

* [x] Log user registration
* [x] Log successful/failed logins
* [x] Log transaction creation/updates
* [x] Log wallet operations
* [x] Log category operations
* [x] Log subscription changes
* [x] Log payment events
* [x] Log export operations
* [x] Created BusinessEventLogger utility class

### Error Logging

* [x] Create global `@ControllerAdvice` for exception handling
* [x] Log all exceptions with stack trace
* [x] Include request context (URL, method, user)
* [x] Add correlation ID to errors
* [x] Return user-friendly error messages (don't expose internals)
* [x] Created custom exceptions (ResourceNotFoundException, BusinessException)

---

## 3️⃣ Application Metrics

### Spring Boot Actuator

* [x] Add Actuator dependency (already included)
* [x] Configure Actuator endpoints in `application.yml`
  * [x] Expose `/actuator/health`
  * [x] Expose `/actuator/metrics`
  * [x] Expose `/actuator/prometheus`
  * [x] Expose `/actuator/info`
  * [ ] Secure other endpoints (require auth)
* [ ] Customize health indicators
  * [ ] Database health check
  * [ ] Disk space check
  * [ ] Custom business health (e.g., payment gateway)
* [x] Add build info to `/actuator/info`
  * [x] Version
  * [x] Git commit
  * [x] Build time

### Micrometer Metrics

* [x] Add Prometheus registry
  ```gradle
  implementation 'io.micrometer:micrometer-registry-prometheus'
  ```
* [x] Configure Micrometer in `application.yml`
* [x] Create custom metrics
  * [x] Counter: `transactions.created.total`
  * [x] Counter: `users.registered.total`
  * [x] Counter: `login.attempts.total` (with success/failure tags)
  * [x] Gauge: `wallets.active.count`
  * [x] Gauge: `subscriptions.active.count`
  * [x] Timer: `transaction.creation.duration`
  * [x] Timer: `report.generation.duration`
  * [x] Created MetricsService utility class
* [x] Add metrics to use cases (integrated into all use cases)
  * [x] Metrics integrated into AuthService (user registration, login success/failure)
  * [x] Metrics integrated into Wallet use cases (wallet creation)
  * [x] Metrics integrated into Category use cases (category creation)
  * [x] BusinessEventLogger integrated into all CRUD operations
  * [x] Created Transaction use cases with full metrics and logging
  * [x] Metrics for transaction creation with timing
  * [x] Record timing for dashboard generation
  * [ ] Track subscription conversions (pending - subscription feature not yet implemented)
* [x] Add JVM metrics (built-in with Micrometer)
  * [x] Memory usage
  * [x] GC metrics
  * [x] Thread pool metrics

---

## 4️⃣ Infrastructure Monitoring

### Prometheus Setup

* [x] Create `prometheus.yml` configuration
* [x] Add Spring Boot application as target
  ```yaml
  scrape_configs:
    - job_name: 'expense-tracker'
      metrics_path: '/actuator/prometheus'
      static_configs:
        - targets: ['app:8081']
  ```
* [x] Configure scrape interval (15s)
* [x] Add retention period (30 days)
* [x] Add to `docker-compose-monitoring.yml`
  ```yaml
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    ports:
      - "9090:9090"
  ```

### Grafana Setup

* [x] Add to `docker-compose-monitoring.yml`
  ```yaml
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
  ```
* [x] Create Prometheus datasource configuration
* [ ] Import Spring Boot dashboard template (manual step)
* [ ] Create custom dashboards (can be done via UI):
  * [ ] **Application Overview**
    - Request rate (requests/sec)
    - Response time (p50, p95, p99)
    - Error rate (%)
    - Active users
  * [ ] **Business Metrics**
    - Transactions created per day
    - New user registrations
    - Subscription conversions
    - Revenue tracking
  * [ ] **Database Metrics**
    - Connection pool usage
    - Query execution time
    - Slow queries count
    - Database size growth
  * [ ] **System Metrics**
    - JVM memory usage
    - GC frequency and duration
    - Thread pool usage
    - CPU usage

### Alerting with AlertManager

* [ ] Add AlertManager to docker-compose
* [ ] Create alerting rules in Prometheus
  * [ ] Alert: High error rate (>5% for 5 minutes)
  * [ ] Alert: Response time degradation (p95 > 2s for 5 minutes)
  * [ ] Alert: Database connection pool exhaustion (>80%)
  * [ ] Alert: High memory usage (>85%)
  * [ ] Alert: Disk space low (<15%)
  * [ ] Alert: Failed login spike (>10 failures in 1 minute)
* [ ] Configure notification channels
  * [ ] Email for warnings
  * [ ] Slack/Discord webhook for critical
  * [ ] PagerDuty for production outages (optional)

---

## 5️⃣ Centralized Logging (Optional for Production)

### Loki + Promtail Setup

* [ ] Add Loki to docker-compose
  ```yaml
  loki:
    image: grafana/loki:latest
    ports:
      - "3100:3100"
    volumes:
      - loki-data:/loki
  ```
* [ ] Add Promtail to docker-compose
  ```yaml
  promtail:
    image: grafana/promtail:latest
    volumes:
      - /var/log:/var/log
      - ./promtail-config.yml:/etc/promtail/config.yml
  ```
* [ ] Configure Promtail to scrape application logs
* [ ] Add Loki datasource to Grafana
* [ ] Create log search dashboard
* [ ] Set up log retention policy (30 days)

### Alternative: ELK Stack (For Larger Scale)

* [ ] Elasticsearch setup
* [ ] Logstash configuration
* [ ] Kibana dashboard
* [ ] Log shipping from application

---

## 6️⃣ Uptime Monitoring

### Uptime Kuma (Self-Hosted)

* [ ] Add Uptime Kuma to docker-compose
  ```yaml
  uptime-kuma:
    image: louislam/uptime-kuma:1
    ports:
      - "3001:3001"
    volumes:
      - uptime-kuma-data:/app/data
  ```
* [ ] Configure monitors:
  * [ ] HTTP monitor for main app endpoint
  * [ ] HTTP monitor for API health endpoint
  * [ ] Keyword monitor (check for "UP" in health response)
  * [ ] Database connection check (via health endpoint)
* [ ] Set check interval (60 seconds)
* [ ] Configure notifications
  * [ ] Email on downtime
  * [ ] Telegram bot for instant alerts
  * [ ] Discord webhook

### Alternative: Cloud Services

* [ ] Option 1: UptimeRobot (free, 50 monitors)
* [ ] Option 2: Pingdom
* [ ] Option 3: Better Uptime

---

## 7️⃣ Infrastructure as Code

### Docker Compose Complete Setup

* [x] Create `docker-compose-monitoring.yml`
  * [x] Application service with health checks
  * [x] PostgreSQL with persistent volume
  * [x] Prometheus with 30-day retention
  * [x] Grafana with auto-provisioning
  * [ ] Loki + Promtail (optional, for centralized logging)
  * [ ] Uptime Kuma (optional, for uptime monitoring)
  * [ ] Nginx reverse proxy (for production)
* [x] Create `.env.example` with all required variables
* [x] Document volume mapping in docker-compose
* [x] Document port mapping in docker-compose
* [x] Add health checks to all services
* [x] Configure restart policies (unless-stopped)
* [x] Create Dockerfile for application
* [x] Create monitoring/README.md with setup instructions

### Nginx Reverse Proxy

* [ ] Create `nginx.conf`
* [ ] Configure routes:
  * [ ] `/` → Frontend (port 80)
  * [ ] `/api` → Backend (port 8080)
  * [ ] `/grafana` → Grafana (port 3000)
  * [ ] `/prometheus` → Prometheus (port 9090, auth required)
* [ ] Configure SSL/TLS
  * [ ] Let's Encrypt with Certbot
  * [ ] Auto-renewal
* [ ] Configure rate limiting
* [ ] Configure request size limits
* [ ] Add security headers
  * [ ] X-Frame-Options
  * [ ] X-Content-Type-Options
  * [ ] Strict-Transport-Security

### Backup Strategy

* [ ] Database backup script
  * [ ] Daily pg_dump
  * [ ] Compress with gzip
  * [ ] Upload to S3/cloud storage
  * [ ] Retention: keep 30 days
  * [ ] Test restore monthly
* [ ] Prometheus data backup (optional)
* [ ] Grafana dashboard export
* [ ] Application configuration backup
* [ ] Automated backup testing

---

## 8️⃣ Security & Compliance

### Security Logging

* [ ] Log all authentication attempts
  * [ ] Username (or email)
  * [ ] IP address
  * [ ] User agent
  * [ ] Success/failure
  * [ ] Timestamp
* [ ] Log authorization failures
  * [ ] User ID
  * [ ] Requested resource
  * [ ] Permission denied reason
* [ ] Log suspicious activities
  * [ ] Multiple failed login attempts
  * [ ] Access to deleted resources
  * [ ] SQL injection attempts (from WAF)
  * [ ] Unusual API usage patterns

### Audit Trail

* [ ] Create `audit_logs` table
  * [ ] User ID
  * [ ] Action (CREATE, UPDATE, DELETE)
  * [ ] Entity type (Transaction, Wallet, etc.)
  * [ ] Entity ID
  * [ ] Old value (JSON)
  * [ ] New value (JSON)
  * [ ] Timestamp
  * [ ] IP address
* [ ] Implement audit logging aspect
* [ ] Track sensitive operations:
  * [ ] Password changes
  * [ ] Email changes
  * [ ] Subscription changes
  * [ ] Large transactions (> threshold)
  * [ ] Bulk operations

### GDPR Compliance (If Applicable)

* [ ] Log data access (for audit purposes)
* [ ] Implement data export endpoint
* [ ] Implement data deletion endpoint
* [ ] Mask PII in logs
* [ ] Data retention policy for logs (30 days default)

---

## 9️⃣ Performance Monitoring

### Application Performance Monitoring (APM)

* [ ] Enable Sentry Performance monitoring
  * [ ] Transaction tracing
  * [ ] Database query tracking
  * [ ] External API call tracking
* [ ] Alternative: New Relic (if budget allows)
* [ ] Track key transactions:
  * [ ] Transaction creation flow
  * [ ] Dashboard loading
  * [ ] Report generation
  * [ ] Export operations

### Database Performance

* [ ] Enable slow query logging in PostgreSQL
  * [ ] Log queries > 1 second
  * [ ] Analyze slow queries weekly
* [ ] Monitor connection pool
  * [ ] Active connections
  * [ ] Idle connections
  * [ ] Wait time
* [ ] Track query execution plans
* [ ] Index optimization based on slow queries

### Load Testing

* [ ] Create JMeter/K6 test scripts
* [ ] Test scenarios:
  * [ ] User registration flow
  * [ ] Transaction creation (bulk)
  * [ ] Dashboard loading
  * [ ] Report generation
* [ ] Baseline performance metrics
* [ ] Identify bottlenecks
* [ ] Optimize based on results

---

## 🔟 Deployment & CI/CD Integration

### CI/CD Pipeline with Logging

* [ ] GitHub Actions workflow
  * [ ] Build and test
  * [ ] Docker image build
  * [ ] Push to registry
  * [ ] Deploy to production
* [ ] Log deployment events to Sentry
  * [ ] Release version
  * [ ] Git commit
  * [ ] Deployment time
  * [ ] Deployer info
* [ ] Post-deployment checks
  * [ ] Health check endpoint
  * [ ] Smoke tests
  * [ ] Alert on failure

### Production Deployment Checklist

* [ ] Verify all environment variables set
* [ ] Verify database connection
* [ ] Verify external API keys (Sentry, payment gateways)
* [ ] Run database migrations
* [ ] Test health endpoints
* [ ] Verify Prometheus scraping
* [ ] Verify Grafana dashboards loading
* [ ] Test alerting (trigger dummy alert)
* [ ] Check log rotation working
* [ ] Verify backup scripts running

---

## 1️⃣1️⃣ Documentation

### Monitoring Documentation

* [ ] Create `MONITORING.md` in docs/
  * [ ] How to access Grafana
  * [ ] How to read dashboards
  * [ ] How to create alerts
  * [ ] How to check logs in Loki
* [ ] Document metric naming conventions
* [ ] Document alerting runbook
  * [ ] What to do when alert fires
  * [ ] How to investigate
  * [ ] How to resolve common issues

### Logging Documentation

* [ ] Create `LOGGING.md` in docs/
  * [ ] Log levels and when to use
  * [ ] How to add new logs
  * [ ] Sensitive data filtering rules
  * [ ] How to search logs
  * [ ] Common log patterns for troubleshooting

### Incident Response

* [ ] Create incident response playbook
  * [ ] How to detect incidents
  * [ ] Severity levels
  * [ ] Communication channels
  * [ ] Escalation procedures
  * [ ] Post-incident review process

---

## 1️⃣2️⃣ Testing

### Logging Tests

* [ ] Unit tests for LoggingAspect
* [ ] Test sensitive data filtering
* [ ] Test correlation ID generation
* [ ] Integration test for audit logging

### Monitoring Tests

* [ ] Test custom metrics collection
* [ ] Verify Actuator endpoints working
* [ ] Test health check indicators
* [ ] Simulate alerts and verify notifications

---

## 1️⃣3️⃣ Cost Optimization

### Free Tier Monitoring Stack

* [ ] Sentry: 5K errors/month free
* [ ] Grafana Cloud: 10K metrics free (if using cloud)
* [ ] Uptime Kuma: Self-hosted, free
* [ ] Prometheus + Grafana: Self-hosted, free
* [ ] Loki: Self-hosted, free

### Paid Options (If Scaling)

* [ ] Datadog: $15/host/month
* [ ] New Relic: Starts at $25/month
* [ ] PagerDuty: $21/user/month
* [ ] Hosted Prometheus: Grafana Cloud, AWS AMP

---

## 📊 Milestones

### Phase 1: Basic Logging (Week 1) ✅ COMPLETED
- [x] Logback configuration
- [x] Structured logging
- [x] Correlation IDs
- [x] Sensitive data filtering
- [x] Security logging
- [x] Business event logging
- [x] Global exception handler

### Phase 2: Error Tracking (Week 2)
- [ ] Sentry integration
- [ ] Error alert setup
- [ ] Performance monitoring

### Phase 3: Metrics & Monitoring (Week 3) ✅ COMPLETED
- [x] Actuator setup
- [x] Custom metrics service created
- [x] Integrate metrics into all use cases
- [x] Prometheus + Grafana setup with docker-compose
- [ ] Basic dashboards (can be imported manually)

### Phase 4: Infrastructure (Week 4) 🔄 IN PROGRESS
- [x] Docker Compose complete with monitoring stack
- [x] Dockerfile for application
- [x] PostgreSQL with persistent volumes
- [ ] Nginx reverse proxy (optional for production)
- [ ] SSL/TLS setup (optional for production)
- [ ] Backup automation (next step)

### Phase 5: Advanced Monitoring (Week 5)
- [ ] Centralized logging (Loki)
- [ ] Uptime monitoring
- [ ] Alerting rules
- [ ] Performance optimization

### Phase 6: Production Ready (Week 6)
- [ ] Load testing
- [ ] Security hardening
- [ ] Documentation
- [ ] Production deployment

---

## 🎯 Success Criteria

* [ ] All application logs are structured and searchable
* [ ] Error rate < 1% tracked in Sentry
* [ ] Response time p95 < 500ms tracked in Grafana
* [ ] 99.9% uptime monitored
* [ ] All alerts tested and working
* [ ] Dashboards provide actionable insights
* [ ] Backup and restore tested successfully
* [ ] Documentation complete and accurate
* [ ] Team trained on monitoring tools

---

## 📚 Resources

### Tools Documentation
* [Logback](https://logback.qos.ch/documentation.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
* [Micrometer](https://micrometer.io/docs)
* [Sentry](https://docs.sentry.io/platforms/java/guides/spring-boot/)
* [Prometheus](https://prometheus.io/docs/introduction/overview/)
* [Grafana](https://grafana.com/docs/grafana/latest/)
* [Loki](https://grafana.com/docs/loki/latest/)

### Tutorials
* [Spring Boot Monitoring with Prometheus & Grafana](https://www.baeldung.com/spring-boot-actuator-prometheus)
* [Structured Logging Best Practices](https://www.dataset.com/blog/the-10-commandments-of-logging/)
* [Sentry Integration Guide](https://docs.sentry.io/platforms/java/guides/spring-boot/)

---

**Last Updated:** November 25, 2025
**Status:** 🔄 Implementation Phase
**Phase 1 Status:** ✅ COMPLETED (Logging, filtering, business events)
**Phase 3 Status:** ✅ COMPLETED (Metrics integrated into all use cases)
**Phase 4 Status:** 🔄 IN PROGRESS (Monitoring stack setup complete)

**Completed Today:**
1. ✅ Transaction use cases created with full logging and metrics
2. ✅ Prometheus + Grafana infrastructure setup
3. ✅ Docker Compose with full monitoring stack
4. ✅ Dockerfile for containerized deployment
5. ✅ Environment variables and .gitignore configuration
6. ✅ Comprehensive monitoring README

**Next Actions:**
1. Test docker-compose stack: `docker-compose -f docker-compose-monitoring.yml up`
2. Import Grafana dashboards (ID: 11378 for JVM metrics)
3. Create custom business metrics dashboards
4. Optional: Setup Sentry (see sentry_plan.md)
5. Optional: Add Nginx reverse proxy for production
