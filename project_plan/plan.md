Berikut **markdown final tanpa tambahan obrolan**, siap kamu copy langsung ke file `PROJECT_PLAN.md`:

---

# 📘 Expense Tracker UMKM-Friendly — Project Plan

## 🗂 Table of Contents

1. [Project Overview](#1-project-overview)
2. [Goals & Success Metrics](#2-goals--success-metrics)
3. [User Personas](#3-user-personas)
4. [MVP Feature List](#4-mvp-feature-list)
5. [Tech Stack](#5-tech-stack)
6. [High-Level Architecture](#6-high-level-architecture)
7. [Database Schema](#7-database-schema)
8. [REST API Summary](#8-rest-api-summary)
9. [Auth & Security](#9-auth--security)
10. [Subscription & Monetization](#10-subscription--monetization)
11. [Offline Strategy](#11-offline-strategy)
12. [Development Roadmap](#12-development-roadmap)
13. [Deliverables per Milestone](#13-deliverables-per-milestone)
14. [Testing Plan](#14-testing-plan)
15. [Deployment & DevOps](#15-deployment--devops)
16. [Monitoring & Maintenance](#16-monitoring--maintenance)
17. [Privacy & Legal](#17-privacy--legal)
18. [UX Guidelines](#18-ux-guidelines)
19. [README Contents](#19-readme-contents)
20. [Immediate Next Steps](#20-immediate-next-steps)
21. [Optional: What I Can Generate Next](#21-optional-what-i-can-generate-next)
22. [Notes & Assumptions](#22-notes--assumptions)

---

# 1. Project Overview

**Nama proyek:** Expense Tracker UMKM-Friendly
**Target:** Solo developer (Java + Vue), Indonesia
**Tujuan:** Membuat aplikasi pencatatan keuangan sederhana untuk individu & UMKM lokal.
**Monetisasi:** Freemium + Subscription (premium).
**MVP fokus:** Input transaksi cepat, hutang/piutang, dashboard ringkas, dan fitur premium penting (backup & export).

---

# 2. Goals & Success Metrics

## 🎯 Goals

* MVP dapat digunakan secara daily oleh user.
* Mendapatkan user retention dasar (7-hari).
* Membangun flow langaran (trial → paid).

## 📈 Metrics

* Activation rate (signup → add 1 transaksi).
* 7-day retention.
* Conversion free → premium.
* Usage fitur hutang/piutang & transaksi harian.

---

# 3. User Personas

### 👩 Ibu Warung

* Catat pemasukan/pengeluaran cepat
* Catat hutang pelanggan
* Perlu UI besar dan tidak ribet

### 👨‍💼 Pemilik Toko / UMKM

* Perlu laporan mingguan/bulanan
* Export transaksi untuk pembukuan

### 👨‍💻 Freelancer / Karyawan

* Catat pengeluaran pribadi
* Butuh multi-wallet sederhana

---

# 4. MVP Feature List

## 🆓 Core (Gratis)

* Auth (Register, Login)
* Dashboard ringkas
* CRUD transaksi (Income/Expense)
* CRUD kategori (limited)
* CRUD wallet (1 wallet gratis)
* Hutang/piutang (create, partial payment, mark paid)
* Transactions list + filter
* Export CSV (limit)
* Guest/local mode

## 💎 Premium (Subscription)

* Cloud sync & auto backup
* Unlimited categories
* Unlimited wallets
* Export PDF / Excel tanpa watermark
* Reminders otomatis
* Multi-month reports
* Trial 7–14 hari

---

# 5. Tech Stack

### Backend (Java)

* Java 17+
* Spring Boot (Web, Security, Data JPA, Validation)
* Flyway
* PostgreSQL
* MapStruct
* Gradle

### Frontend (Vue)

* Vue 3 (Composition API)
* Vite
* Pinia
* Vue Router
* TailwindCSS
* Axios

### Mobile (Optional)

* Capacitor (wrap Vue → Android)

### Payments

* Stripe (global)
* Midtrans / Xendit (Indonesia)
* Play Store Billing (Android)

### Deployment

* Docker & Docker Compose
* VPS / Cloud Run / App Engine
* Sentry for error monitoring

---

# 6. High-Level Architecture

```
Frontend (Vue)
     ↓ API Calls
Backend (Spring Boot)
     ↓ JPA
PostgreSQL Database
```

Optional:

* Object storage (S3/MinIO) untuk backup
* Webhooks untuk pembayaran
* Auth: JWT access + refresh tokens

---

# 7. Database Schema

```sql
users (
  id UUID PK,
  email TEXT UNIQUE,
  password_hash TEXT,
  name TEXT,
  locale TEXT,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

wallets (
  id UUID PK,
  user_id UUID FK,
  name TEXT,
  currency VARCHAR(3) DEFAULT 'IDR',
  initial_balance NUMERIC,
  created_at TIMESTAMP
);

categories (
  id UUID PK,
  user_id UUID NULL,
  name TEXT,
  type VARCHAR(10),
  created_at TIMESTAMP
);

transactions (
  id UUID PK,
  user_id UUID FK,
  wallet_id UUID FK,
  category_id UUID FK,
  type VARCHAR(10),
  amount NUMERIC,
  note TEXT,
  date TIMESTAMP,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

debts (
  id UUID PK,
  user_id UUID FK,
  counterparty_name TEXT,
  total_amount NUMERIC,
  remaining_amount NUMERIC,
  due_date DATE,
  status VARCHAR(10),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

debt_payments (
  id UUID PK,
  debt_id UUID FK,
  amount NUMERIC,
  paid_at TIMESTAMP,
  note TEXT
);

subscriptions (
  id UUID PK,
  user_id UUID FK,
  provider TEXT,
  provider_subscription_id TEXT,
  plan TEXT,
  status TEXT,
  started_at TIMESTAMP,
  ended_at TIMESTAMP
);
```

---

# 8. REST API Summary

## Auth

* `POST /auth/register`
* `POST /auth/login`
* `POST /auth/refresh`

## Users

* `GET /me`

## Wallets

* CRUD endpoints

## Categories

* CRUD endpoints

## Transactions

* List + filter
* Create
* Get detail

## Debts

* List
* Create
* Add payment
* Mark paid

## Reports

* Summary (7d / 30d / custom)

## Subscriptions

* Checkout
* Webhook
* Get subscription status

---

# 9. Auth & Security

* JWT access tokens + refresh tokens
* Password bcrypt/argon2
* Input validation (server-side)
* Rate limit login endpoint
* HTTPS mandatory
* Ownership checks per-user untuk semua data

---

# 10. Subscription & Monetization

### Flow

1. User klik upgrade
2. Backend buat checkout session
3. User bayar → provider kirim webhook
4. Backend update table `subscriptions`
5. Premium unlocked

### Fitur premium:

Backup cloud, unlimited kategori/wallet, export PDF/Excel, reminder otomatis, laporan lanjutan.

---

# 11. Offline Strategy (MVP)

### MVP:

* Local storage & manual backup (JSON/CSV).

### Future:

* Operation log sync (timestamp-based).
* Conflict resolution: last-write-wins.

---

# 12. Development Roadmap

### Milestone 0 — Setup

* Repo + backend/frontend scaffolding

### Milestone 1 — Auth

* Users, register/login, JWT, profile page

### Milestone 2 — Wallets, Categories, Transactions

* CRUD & dashboard

### Milestone 3 — Debts

* Debts + payments + UI

### Milestone 4 — Reports + Export

* Summary API + CSV export

### Milestone 5 — Subscription

* Integrate checkout + webhook (test mode)

### Milestone 6 — Backup / Cloud Sync

* Upload/download backup

### Milestone 7 — QA & Deployment

* Tests, security checks, deploy

---

# 13. Deliverables per Milestone

* M0: scaffolding
* M1: user system working
* M2: transaksi end-to-end
* M3: hutang/piutang working
* M4: export & laporan
* M5: subscription flow ready
* M6: backup cloud
* M7: production deployment

---

# 14. Testing Plan

* Unit tests: service, repo
* Integration tests: controller
* Frontend unit tests: Vitest
* E2E tests: Cypress
* Security tests: unauthorized access, validation
* Manual QA on Android (Capacitor)

---

# 15. Deployment & DevOps

* Docker Compose (backend + db + nginx)
* CI/CD GitHub Actions
* Managed Postgres (DO / AWS / GCP)
* Secure env vars
* Automatic TLS (Let’s Encrypt / Cloudflare)

---

# 16. Monitoring & Maintenance

## 🔍 Logging

### Application Logging
* **Logback** (default Spring Boot) - structured logging
* **SLF4J** - logging abstraction
* **Log levels:** INFO for normal operations, ERROR for exceptions, DEBUG for development
* **Log format:** JSON untuk production (easy parsing)
* **Log rotation:** Daily rotation, keep 30 days

### Centralized Logging (Production)
* **ELK Stack** (Elasticsearch + Logstash + Kibana) - untuk scale besar
* **Loki + Grafana** - alternatif lebih lightweight
* **CloudWatch Logs** (AWS) atau **Cloud Logging** (GCP) - jika deploy di cloud
* **Simple option:** File-based logs + rsync backup

### Log Aggregation Pattern
```java
// Structured logging example
log.info("Transaction created",
    kv("userId", userId),
    kv("amount", amount),
    kv("type", type));
```

## 📊 Monitoring & Metrics

### Application Metrics
* **Spring Boot Actuator** - health checks, metrics endpoints
  - `/actuator/health` - application health
  - `/actuator/metrics` - JVM & custom metrics
  - `/actuator/prometheus` - Prometheus format metrics
* **Micrometer** - metrics collection library (built-in Spring Boot)

### Infrastructure Monitoring
**Option 1: Full-Featured (Recommended)**
* **Prometheus** - metrics collection & storage
* **Grafana** - visualization & dashboards
* **AlertManager** - alerting rules

**Option 2: Cloud-Native**
* **AWS CloudWatch** (if using AWS)
* **Google Cloud Monitoring** (if using GCP)
* **Datadog** - all-in-one (paid, easy setup)

**Option 3: Simple/Budget**
* **Netdata** - real-time monitoring (free, lightweight)
* **Uptime Kuma** - uptime monitoring
* **Grafana Cloud Free Tier** - limited metrics

### Key Metrics to Track
* **Application:**
  - Request rate (requests/second)
  - Response time (p50, p95, p99)
  - Error rate (4xx, 5xx)
  - Active users
  - Transaction creation rate

* **Database:**
  - Connection pool usage
  - Query execution time
  - Slow queries (> 1s)
  - Database size

* **System:**
  - CPU usage
  - Memory usage
  - Disk I/O
  - Network throughput

## 🚨 Error Tracking & Alerting

### Error Tracking
* **Sentry** (Recommended)
  - Automatic error capturing
  - Stack traces & context
  - User feedback
  - Free tier: 5K errors/month

* **Alternatives:**
  - **Rollbar** - similar to Sentry
  - **Bugsnag** - error monitoring
  - **Self-hosted:** Sentry (open source)

### Alerting Rules
* 5xx error rate > 5% in 5 minutes
* Database connection pool > 80%
* Disk usage > 85%
* API response time p95 > 2 seconds
* Failed login attempts > 10 in 1 minute (potential attack)

### Notification Channels
* Email untuk non-critical alerts
* Slack/Discord webhook untuk critical alerts
* SMS/WhatsApp untuk production down

## 🏥 Health Checks

### Endpoints
```java
// Spring Boot Actuator
GET /actuator/health - Overall health
GET /actuator/health/readiness - K8s readiness probe
GET /actuator/health/liveness - K8s liveness probe
```

### Health Indicators
* Database connectivity
* Disk space
* External API availability (payment gateway)
* Cache availability (if using Redis)

## 💾 Backup & Recovery

* **Database Backups:**
  - Automated daily backups (pg_dump)
  - Keep 30 days of backups
  - Test restore quarterly
  - Store in separate location (S3/cloud storage)

* **Application Backups:**
  - Docker images tagged by version
  - Configuration files in git
  - Environment secrets in vault

## 🎯 Recommended Stack for Solo Developer

### MVP/Budget Option:
```yaml
Logging: Logback → Files → Loki (optional)
Monitoring: Spring Actuator + Netdata
Error Tracking: Sentry (free tier)
Uptime: Uptime Kuma
Alerts: Email + Discord webhook
```

### Production Option:
```yaml
Logging: Logback → CloudWatch/Loki
Monitoring: Prometheus + Grafana
Error Tracking: Sentry
APM: Sentry Performance (or New Relic)
Alerts: PagerDuty/Slack
Uptime: Pingdom/UptimeRobot
```

## 📦 Easy Setup with Docker Compose

```yaml
# Add to docker-compose.yml
services:
  prometheus:
    image: prom/prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    volumes:
      - grafana-storage:/var/lib/grafana

  loki:
    image: grafana/loki
    ports:
      - "3100:3100"
```

## 🔧 Dependencies to Add

```gradle
// build.gradle
dependencies {
    // Monitoring & Metrics
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'

    // Error Tracking
    implementation 'io.sentry:sentry-spring-boot-starter:6.x.x'
    implementation 'io.sentry:sentry-logback:6.x.x'

    // Structured Logging
    implementation 'net.logstash.logback:logstash-logback-encoder:7.x.x'
}
```

## 📝 Best Practices

1. **Log sensitive data filtering** - jangan log password, tokens, credit cards
2. **Correlation IDs** - track requests across services
3. **Dashboard untuk bisnis metrics** - total users, revenue, transactions/day
4. **Set up alerts sebelum production** - jangan tunggu down dulu
5. **Regular review logs & metrics** - weekly untuk spot trends

---

# 17. Privacy & Legal

* Privacy policy wajib
* Data export & deletion request
* Minimize PII (email + name only)
* Clear backup policy

---

# 18. UX Guidelines

* Numeric pad besar untuk input rupiah
* Form minimal: amount + category + optional note
* Floating + button
* Ringkasan harian/mingguan
* Tampilan sederhana & readable

---

# 19. README Contents

* Project overview
* Local dev setup
* Environment variables guide
* Database migration instructions
* Running tests
* Deployment instructions

---

# 20. Immediate Next Steps

```bash
# 1. Buat folder project
mkdir expense-tracker && cd expense-tracker

# 2. Generate Spring Boot backend
curl https://start.spring.io/starter.tgz \
  -d dependencies=web,data-jpa,security,validation \
  -d javaVersion=17 | tar -xzvf -

# 3. Init git
git init
git add .
git commit -m "init backend scaffold"

# 4. Buat frontend (Vue)
cd ..
npm init vite@latest frontend -- --template vue
cd frontend
npm install

# 5. Commit initial frontend
git add .
git commit -m "init frontend scaffold"

# 6. Buat PROJECT_PLAN.md dan paste dokumen ini
```

---

# 21. Optional: What I Can Generate Next

* Flyway migration SQL untuk initial schema
* Spring Boot entity + repository + service + controller (full sample)
* Auth module (register/login + JWT)
* Vue Add Transaction modal + Pinia store
* README.md final siap commit
* Docker Compose stack (backend + postgres + pgadmin)

---

# 22. Notes & Assumptions

* Target currency IDR
* Fokus awal web → mobile via Capacitor setelah stabil
* Harga langganan mengikuti pasar lokal Indonesia
* Cloud sync bisa disusul setelah monetisasi berjalan
