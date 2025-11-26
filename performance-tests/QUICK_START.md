# K6 Performance Test - Quick Start Guide

## TL;DR - Run the Test

```bash
cd performance-tests
./run-test.sh
```

## Prerequisites Check

### 1. Install K6

```bash
# macOS
brew install k6

# Verify
k6 version
```

### 2. Start Application

```bash
# Option 1: Gradle
./gradlew bootRun

# Option 2: Check if already running
curl http://localhost:8081/actuator/health
```

## Run Options

### Using the Helper Script (Recommended)
```bash
cd performance-tests
./run-test.sh
```

### Manual Run
```bash
cd performance-tests
k6 run k6-test.js
```

### With Custom Config
```bash
# Different base URL
BASE_URL=http://localhost:8080 k6 run k6-test.js

# More users, longer duration
k6 run --vus 20 --duration 10m k6-test.js

# Save JSON report
k6 run --out json=reports/my-test.json k6-test.js
```

## What the Test Does

**10 Virtual Users** simulating real user behavior:

1. **Register & Login** → Get JWT token
2. **Create 3 Wallets** → Different currencies
3. **Create 3 Categories** → EXPENSE and INCOME types
4. **Create 10 Transactions** → Random amounts and dates
5. **List & Update** → CRUD operations on all resources
6. **Dashboard Access** → Analytics and summaries

**Each user makes ~100 requests over 5 minutes**

## Understanding Results

### Good Performance ✅
```
✓ http_req_duration.............: avg=245ms p(95)=456ms p(99)=612ms
✓ http_req_failed...............: 0.15%
✓ http_reqs.....................: 10249 (34 req/s)
```

### Issues to Watch ⚠️
```
✗ http_req_duration.............: p(95)=1.2s    ← TOO SLOW
✗ http_req_failed...............: 5.3%          ← TOO MANY ERRORS
✗ checks........................: 85.2%          ← SHOULD BE >99%
```

## Thresholds

| Metric | Target | Status |
|--------|--------|--------|
| p95 response time | < 500ms | ✅ Pass |
| p99 response time | < 1000ms | ✅ Pass |
| Error rate | < 1% | ✅ Pass |
| Auth success rate | > 99% | ✅ Pass |

## Common Issues

### "connection refused"
```bash
# Check if app is running
lsof -i :8081

# Start app
./gradlew bootRun
```

### "registration failed 409"
**Normal!** Test users already exist from previous run. Test handles this.

### High error rate
```bash
# Check application logs
tail -f logs/application.log

# Check database
lsof -i :5432
```

## Clean Up Test Data (Optional)

```sql
-- Connect to database
psql -U postgres -d expense_tracker

-- Delete test users
DELETE FROM users WHERE email LIKE 'loadtest-user-%';
```

## Files

```
performance-tests/
├── k6-test.js          ← Main test script (502 lines)
├── README.md           ← Full documentation
├── QUICK_START.md      ← This file
├── run-test.sh         ← Helper script
├── .gitignore          ← Ignore reports
└── reports/            ← Generated reports
    └── results_*.json
```

## Next Steps

1. ✅ Run the test: `./run-test.sh`
2. 📊 Check metrics in console output
3. 📈 View trends in Grafana: http://localhost:3000
4. 🔍 Analyze JSON report: `cat reports/results_*.json | jq`

## Support

- Full documentation: [README.md](README.md)
- K6 docs: https://k6.io/docs/
- Application logs: `../logs/application.log`

---

**Happy Load Testing! 🚀**
