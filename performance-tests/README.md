# K6 Load Testing - Expense Tracker

Load testing script untuk Expense Tracker API menggunakan k6.

## Overview

Test ini mensimulasikan **100 concurrent users**, dimana setiap user melakukan operasi yang sama secara paralel.

### Test Scenarios

Setiap user akan melakukan:

1. **Registrasi** - Mendaftar akun baru
2. **Login** - Mendapatkan JWT token
3. **Membuat 10 Wallets** - Setiap user membuat 10 wallet dengan currency random
4. **Membuat 7 Categories** - 5 expense categories + 2 income categories
5. **Membuat 10,000 Transactions** - Transaksi random di berbagai wallet dan category
6. **Membuat 1,000 Debts** - 500 PAYABLE + 500 RECEIVABLE
7. **Export Data** - Export ke CSV dan Excel (akan terblokir jika bukan premium user)

### Total Expected Operations

- **100 users** × 10 wallets = **1,000 wallets**
- **100 users** × 10,000 transactions = **1,000,000 transactions**
- **100 users** × 1,000 debts = **100,000 debts**
- **100 users** × 2 exports = **200 export requests**

**Total HTTP requests**: **~1,100,700 requests**

## Prerequisites

### 1. Install k6

**macOS:**
```bash
brew install k6
```

**Linux:**
```bash
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

**Windows:**
```bash
choco install k6
```

**Docker:**
```bash
docker pull grafana/k6
```

### 2. Start Application

Pastikan aplikasi Expense Tracker sudah running di `http://localhost:8081`

```bash
./gradlew bootRun
```

Atau dengan Docker:
```bash
docker-compose up -d
```

Verify:
```bash
curl http://localhost:8081/v1/api/actuator/health
```

## Running Tests

### Basic Test (default localhost)

```bash
cd performance-tests
k6 run k6-test.js
```

### Test with Custom URL

```bash
k6 run -e BASE_URL=http://localhost:8081 k6-test.js
```

### Test with Different User Count

Edit file `k6-test.js` di bagian options:

```javascript
export const options = {
  scenarios: {
    load_test: {
      executor: 'per-vu-iterations',
      vus: 50,  // Ubah jumlah user di sini
      iterations: 1,
      maxDuration: '2h',
    },
  },
};
```

### Run with Docker

```bash
docker run --rm -i --network="host" \
  -v $(pwd):/app \
  grafana/k6 run /app/k6-test.js
```

## Test Configuration

### Scenario Executor

- **Type**: `per-vu-iterations`
- **Virtual Users (VUs)**: 100
- **Iterations per VU**: 1
- **Max Duration**: 2 hours

### Thresholds

- **p95 Response Time**: < 2000ms
- **p99 Response Time**: < 5000ms
- **HTTP Failure Rate**: < 5%
- **Error Rate**: < 5%

## Expected Performance

### Timing Estimates

Per user:
- Registration + Login: ~2 seconds
- 10 Wallets: ~10-20 seconds
- 7 Categories: ~7-14 seconds
- 10,000 Transactions: ~10-20 minutes
- 1,000 Debts: ~2-5 minutes
- 2 Exports: ~2-5 seconds

**Total per user**: ~15-30 minutes
**Total for 100 users**: ~15-30 minutes (concurrent)

### Resource Requirements

**Database**:
- Expect ~1M transaction records
- Expect ~100K debt records
- Ensure sufficient disk space and connection pool

**Application**:
- Monitor heap usage during test
- Recommended: `-Xmx2g -Xms1g`

## Monitoring During Test

### Database Connections

```bash
# PostgreSQL
SELECT count(*) FROM pg_stat_activity;
```

### Application Logs

```bash
tail -f logs/application.log
```

### System Resources

```bash
# CPU and Memory
htop

# Docker stats
docker stats
```

## Output & Reports

### Console Output

Test akan menampilkan progress untuk setiap user:

```
[VU 1] Starting test - Registering user loadtest-user-1@example.com
[VU 1] Registration successful
[VU 1] Login successful
[VU 1] Creating 10 wallets...
[VU 1] Created 5/10 wallets
[VU 1] Created 10/10 wallets
[VU 1] Creating 10,000 transactions...
[VU 1] Created 1000/10000 transactions (Success: 1000, Errors: 0)
...
```

### Summary Report

Di akhir test, akan menampilkan summary:

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

### JSON Report

Report detail disimpan di:
```
performance-tests/reports/summary.json
```

## Troubleshooting

### 1. Connection Refused

**Problem**: `dial: connection refused`

**Solution**:
- Pastikan aplikasi running di `http://localhost:8081`
- Check dengan: `curl http://localhost:8081/v1/api/actuator/health`

### 2. Database Connection Pool Exhausted

**Problem**: `HikariPool - Connection is not available`

**Solution**:
```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
```

### 3. Out of Memory

**Problem**: `java.lang.OutOfMemoryError: Java heap space`

**Solution**:
```bash
./gradlew bootRun -Dspring-boot.run.jvmArguments="-Xmx4g -Xms2g"
```

### 4. Export Returns 403

**Expected**: Export adalah premium feature, user yang baru register akan mendapat 403.

Untuk test export, buat user dengan premium tier terlebih dahulu atau ubah script untuk skip export.

### 5. Test Too Slow

**Solution**:
- Reduce user count (ubah `vus: 100` menjadi `vus: 10`)
- Reduce transactions per user (ubah loop 10000 menjadi 1000)
- Enable database indexing
- Scale up application resources

## Tips untuk Production Load Test

1. **Warm-up Database**: Jalankan test kecil dulu untuk warm-up connection pool
2. **Monitor Resources**: Gunakan monitoring tools (Grafana, Prometheus)
3. **Incremental Load**: Test dengan 10, 50, 100 users secara bertahap
4. **Database Tuning**:
   - Enable connection pooling
   - Add proper indexes
   - Tune PostgreSQL settings
5. **Cleanup**: Hapus test data setelah selesai

## Cleanup Test Data

Setelah test selesai, hapus test data:

```sql
-- Delete test users and related data
DELETE FROM users WHERE email LIKE 'loadtest-user-%@example.com';
-- Cascade delete akan menghapus semua related records
```

Atau reset database:

```bash
./gradlew flywayClean flywayMigrate
```

## Notes

- Test ini akan membuat data dalam jumlah sangat besar di database
- Pastikan backup database sebelum menjalankan test
- Monitor disk space selama test
- Export feature memerlukan premium subscription, expect 403 response untuk free users

---

**Last Updated:** December 17, 2025
**K6 Version:** v0.48+
**Target API:** Expense Tracker REST API with context path `/v1/api/`