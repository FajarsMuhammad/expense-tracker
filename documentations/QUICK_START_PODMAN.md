# Quick Start - Load Testing with Podman

Complete guide untuk menjalankan load test dengan Podman setelah performance optimization.

## Prerequisites

1. **Podman & Podman Compose** installed
2. **k6** installed (untuk load testing)
3. **jq** (optional, untuk parsing JSON)

```bash
# Install Podman (macOS)
brew install podman podman-compose

# Install k6
brew install k6

# Install jq (optional)
brew install jq
```

## Step 1: Start Services dengan Podman

```bash
# Clone/navigate to project
cd expense-tracker

# Build and start all services (app, postgres, prometheus, grafana, loki)
podman-compose up -d --build

# Check if all containers are running
podman ps

# Expected output:
# - expense-tracker-app (port 8081)
# - expense-tracker-db (port 5433)
# - prometheus (port 9090)
# - grafana (port 3000)
# - loki (port 3100)
# - promtail
```

### Verify Services

```bash
# Check application health
curl http://localhost:8081/v1/api/actuator/health

# Expected: {"status":"UP"}

# Check database
podman exec -it expense-tracker-db psql -U postgres -c "SELECT version();"

# View application logs
podman logs -f expense-tracker-app
```

## Step 2: Create Test User

Sebelum load test, buat user untuk mendapatkan token:

```bash
# Register test user
curl -X POST http://localhost:8081/v1/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "name": "Test User"
  }'

# Login and get token
TOKEN=$(curl -X POST http://localhost:8081/v1/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }' | jq -r '.token')

echo "Token: $TOKEN"
```

## Step 3: Test Single Transaction (Verify Performance)

```bash
# Create a wallet first
WALLET_ID=$(curl -X POST http://localhost:8081/v1/api/wallets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Wallet",
    "currency": "IDR",
    "initialBalance": 1000000
  }' | jq -r '.id')

# Create a category
CATEGORY_ID=$(curl -X POST http://localhost:8081/v1/api/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Category",
    "type": "EXPENSE"
  }' | jq -r '.id')

# Test single transaction (measure time)
time curl -X POST http://localhost:8081/v1/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"walletId\": \"$WALLET_ID\",
    \"categoryId\": \"$CATEGORY_ID\",
    \"type\": \"EXPENSE\",
    \"amount\": 50000,
    \"note\": \"Test transaction\",
    \"date\": \"2025-12-17T10:00:00\"
  }"
```

**Expected**: Response time < 100ms (after optimization)

## Step 4: Run Load Test

### Small Test (10 users)

```bash
cd performance-tests

# Edit k6-test.js - change vus to 10
# vus: 10,

k6 run k6-test.js
```

### Full Load Test (100 users)

```bash
cd performance-tests

# Run with 100 users (default)
k6 run k6-test.js
```

**Expected Results**:
- Total duration: ~15-30 minutes
- p95 response time: < 2000ms
- p99 response time: < 5000ms
- Error rate: < 5%
- Total requests: ~1,100,700

## Step 5: Monitor During Test

### Monitor Containers

```bash
# Real-time container stats
podman stats

# Application logs
podman logs -f expense-tracker-app

# Database logs
podman logs -f expense-tracker-db
```

### Monitor Application Metrics

```bash
# HTTP request metrics
curl http://localhost:8081/v1/api/actuator/metrics/http.server.requests | jq

# Connection pool
curl http://localhost:8081/v1/api/actuator/metrics/hikaricp.connections.active | jq
curl http://localhost:8081/v1/api/actuator/metrics/hikaricp.connections.idle | jq

# JVM memory
curl http://localhost:8081/v1/api/actuator/metrics/jvm.memory.used | jq
```

### Monitor Database

```bash
# Connect to database
podman exec -it expense-tracker-db psql -U postgres -d expense_tracker

# Then run:
```

```sql
-- Active connections
SELECT count(*) FROM pg_stat_activity;

-- Active queries
SELECT pid, now() - query_start as duration, state, query
FROM pg_stat_activity
WHERE state = 'active'
ORDER BY duration DESC
LIMIT 10;

-- Table sizes
SELECT
  schemaname,
  tablename,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Index usage
SELECT
  schemaname,
  tablename,
  indexname,
  idx_scan as scans,
  pg_size_pretty(pg_relation_size(indexrelid)) as size
FROM pg_stat_user_indexes
WHERE tablename = 'transactions'
ORDER BY idx_scan DESC;
```

### View Metrics in Grafana

```bash
# Open Grafana
open http://localhost:3000

# Login: admin / admin (default)
# Add Prometheus datasource: http://prometheus:9090
# Import dashboard or create custom queries
```

Example Prometheus queries:
- Request rate: `rate(http_server_requests_seconds_count[1m])`
- Response time p95: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[1m]))`
- Active connections: `hikaricp_connections_active`

## Step 6: Analyze Results

### View k6 Summary

Load test akan menampilkan summary di console:

```
======================================
LOAD TEST SUMMARY - 100 USERS
======================================

Test Configuration:
  Virtual Users: 100
  Per User Tasks:
    - 1 Registration
    - 10 Wallets
    - 10,000 Transactions
    - 1,000 Debts
    - 2 Exports (CSV + Excel)

HTTP Metrics:
  Total Requests: 1,100,700
  Requests/sec: 250.00
  Error Rate: 0.05%

Response Times:
  Average: 150.00ms
  Median (p50): 120.00ms
  p95: 500.00ms
  p99: 1200.00ms
  Max: 3000.00ms
```

### Check for Errors

```bash
# Application errors
podman logs expense-tracker-app | grep -i error | tail -50

# Database errors
podman logs expense-tracker-db | grep -i error | tail -50

# Failed requests in k6 output
# Look for http_req_failed percentage
```

### Database Stats

```bash
podman exec -it expense-tracker-db psql -U postgres -d expense_tracker
```

```sql
-- Count records
SELECT
  'users' as table_name, count(*) as count FROM users
UNION ALL
SELECT 'wallets', count(*) FROM wallets
UNION ALL
SELECT 'categories', count(*) FROM categories
UNION ALL
SELECT 'transactions', count(*) FROM transactions
UNION ALL
SELECT 'debts', count(*) FROM debts;

-- Expected after 100 user test:
-- users: 100
-- wallets: 1,000
-- transactions: 1,000,000
-- debts: 100,000
```

## Step 7: Cleanup Test Data

### Option 1: Delete Test Users

```sql
-- Connect to database
podman exec -it expense-tracker-db psql -U postgres -d expense_tracker

-- Delete all loadtest users (cascade will delete related data)
DELETE FROM users WHERE email LIKE 'loadtest-user-%@example.com';

-- Verify
SELECT count(*) FROM transactions;
SELECT count(*) FROM debts;
```

### Option 2: Reset Database Completely

```bash
# Stop containers
podman-compose down

# Remove volumes (deletes all data)
podman volume rm expense-tracker_postgres-data

# Or use compose command
podman-compose down -v

# Restart (will create fresh database)
podman-compose up -d
```

### Option 3: Keep Data for Analysis

If you want to analyze the data later, don't delete. Just be aware of disk space.

```bash
# Check database size
podman exec -it expense-tracker-db psql -U postgres -d expense_tracker \
  -c "SELECT pg_size_pretty(pg_database_size('expense_tracker'));"
```

## Troubleshooting

### Container Won't Start

```bash
# Check logs
podman logs expense-tracker-app
podman logs expense-tracker-db

# Check if ports are available
lsof -i :8081
lsof -i :5433

# Restart services
podman-compose restart
```

### Slow Performance

```bash
# Check container resources
podman stats expense-tracker-app

# Check if batch processing is enabled
podman logs expense-tracker-app | grep "batch"

# Verify indexes exist
podman exec -it expense-tracker-db psql -U postgres -d expense_tracker \
  -c "\d transactions"
```

### Connection Pool Exhausted

```bash
# Check active connections
curl http://localhost:8081/v1/api/actuator/metrics/hikaricp.connections.active

# Check if connection limit is hit
podman exec -it expense-tracker-db psql -U postgres -d expense_tracker \
  -c "SELECT count(*) FROM pg_stat_activity;"

# Increase pool size in application.yaml and restart
```

### Out of Memory

```bash
# Check container memory
podman stats expense-tracker-app

# Increase container memory limit in docker-compose.yml:
# deploy:
#   resources:
#     limits:
#       memory: 4G

# Rebuild
podman-compose down
podman-compose up -d --build
```

## Performance Checklist

Before running load test, verify:

- ✅ Hibernate batch processing enabled (`batch_size: 50`)
- ✅ Open-in-view disabled (`open-in-view: false`)
- ✅ Connection pool tuned (`maximum-pool-size: 20`)
- ✅ Logging set to INFO (not DEBUG)
- ✅ JVM heap size configured (2GB+)
- ✅ Database indexes exist (check with `\d transactions`)
- ✅ Virtual threads enabled
- ✅ Response built inside transaction

## Next Steps

1. **Baseline Test**: Run with 10 users first to establish baseline
2. **Incremental Load**: Test with 25, 50, 75, 100 users
3. **Monitor Resources**: Watch for bottlenecks (CPU, memory, disk I/O)
4. **Tune Parameters**: Adjust batch size, pool size based on results
5. **Production Planning**: Calculate required resources for prod load

## Useful Commands Reference

```bash
# Start everything
podman-compose up -d --build

# Stop everything
podman-compose down

# Restart app only
podman-compose restart app

# View logs
podman logs -f expense-tracker-app

# Shell into app container
podman exec -it expense-tracker-app /bin/sh

# Connect to database
podman exec -it expense-tracker-db psql -U postgres -d expense_tracker

# Container stats
podman stats

# Cleanup everything
podman-compose down -v
podman system prune -af
```

---

**Happy Load Testing!** 🚀
