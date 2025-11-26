# K6 Performance Testing for Expense Tracker API

This directory contains K6 load testing scripts for the Expense Tracker REST API.

## Overview

The test simulates **10 virtual users**, each making approximately **100 requests** across different API endpoints over a 5-minute period.

### Test Scenarios

1. **User Registration & Authentication** (10%)
   - Register new user
   - Login to obtain JWT token

2. **Wallet CRUD Operations** (30%)
   - Create wallets
   - List all wallets
   - Get specific wallet
   - Update wallet

3. **Category CRUD Operations** (25%)
   - Create categories (INCOME and EXPENSE types)
   - List all categories
   - Get specific category
   - Update category

4. **Transaction Operations** (30%)
   - Create transactions
   - List transactions
   - Update transactions

5. **Dashboard & Analytics** (15%)
   - Get dashboard summary
   - Retrieve current user profile

## Prerequisites

### 1. Install K6

**macOS:**
```bash
brew install k6
```

**Windows:**
```bash
choco install k6
```

**Linux:**
```bash
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

**Verify installation:**
```bash
k6 version
```

### 2. Start the Application

Make sure the Expense Tracker application is running on `http://localhost:8081`:

**Option 1: Using Gradle**
```bash
./gradlew bootRun
```

**Option 2: Using Docker Compose**
```bash
podman-compose -f docker-compose-monitoring.yml up -d
```

**Verify application is running:**
```bash
curl http://localhost:8081/actuator/health
```

Expected response: `{"status":"UP"}`

### 3. Ensure Database is Running

PostgreSQL must be running and accessible:

```bash
# Check if PostgreSQL is running
lsof -i :5432

# Or with Docker/Podman
podman ps | grep postgres
```

## Running the Tests

### Basic Run

```bash
cd performance-tests
k6 run k6-test.js
```

### Run with JSON Output

```bash
k6 run --out json=reports/results.json k6-test.js
```

### Run with Custom Configuration

```bash
# Custom base URL
BASE_URL=http://localhost:8080 k6 run k6-test.js

# Custom number of VUs and duration
k6 run --vus 20 --duration 10m k6-test.js

# Custom stages
k6 run --stage 20s:10,10m:10,20s:0 k6-test.js
```

### Run with Verbose Output

```bash
k6 run --http-debug k6-test.js
```

## Understanding the Output

### Console Output

K6 provides real-time metrics during the test:

```
scenarios: (100.00%) 1 scenario, 10 max VUs, 5m30s max duration
          ✓ registration status is 200 or 409 (already exists)
          ✓ login status is 200
          ✓ login returns token
          ✓ create wallet status is 201
          ✓ list wallets status is 200
          ...

checks.........................: 99.85% ✓ 10234     ✗ 15
data_received..................: 15 MB  50 kB/s
data_sent......................: 8.2 MB 27 kB/s
http_req_blocked...............: avg=156.43µs min=1.8µs   med=4.6µs   max=85.36ms  p(90)=6.8µs   p(95)=8.4µs
http_req_connecting............: avg=51.81µs  min=0s      med=0s      max=39.94ms  p(90)=0s      p(95)=0s
http_req_duration..............: avg=245.67ms min=5.02ms  med=189ms   max=2.87s    p(90)=456ms   p(95)=612ms
http_req_failed................: 0.15%  ✓ 15        ✗ 10234
http_req_receiving.............: avg=154.34µs min=20.4µs  med=87.5µs  max=13.45ms  p(90)=243µs   p(95)=354µs
http_req_sending...............: avg=43.76µs  min=8.9µs   med=28.1µs  max=4.21ms   p(90)=72.4µs  p(95)=99.2µs
http_req_tls_handshaking.......: avg=0s       min=0s      med=0s      max=0s       p(90)=0s      p(95)=0s
http_req_waiting...............: avg=245.47ms min=4.95ms  med=188ms   max=2.87s    p(90)=456ms   p(95)=611ms
http_reqs......................: 10249  34.164/s
iteration_duration.............: avg=17.5s    min=14.2s   med=17.3s   max=25.1s    p(90)=19.8s   p(95)=21.2s
iterations.....................: 102    0.34/s
vus............................: 1      min=1       max=10
vus_max........................: 10     min=10      max=10
```

### Key Metrics

**Response Times:**
- `http_req_duration`: Time from start of request to end of response
- `p(95)`: 95th percentile - 95% of requests are faster than this
- `p(99)`: 99th percentile - 99% of requests are faster than this

**Throughput:**
- `http_reqs`: Total number of HTTP requests made
- `rate`: Requests per second

**Errors:**
- `http_req_failed`: Percentage of failed requests (4xx, 5xx status codes)
- `checks`: Percentage of assertion checks that passed

**Custom Metrics:**
- `auth_success`: Authentication success rate
- `wallet_operations`: Total wallet CRUD operations
- `category_operations`: Total category CRUD operations
- `transaction_operations`: Total transaction operations
- `dashboard_requests`: Total dashboard/analytics requests

### Success Criteria

✅ **Pass:** Error rate < 1%
✅ **Pass:** p95 response time < 500ms
✅ **Pass:** p99 response time < 1000ms
✅ **Pass:** Auth success rate > 99%

## Test Configuration

### Load Profile

| Stage | Duration | Target VUs | Purpose |
|-------|----------|------------|---------|
| Ramp-up | 10s | 0 → 10 | Gradually increase load |
| Sustain | 5m | 10 | Maintain steady load |
| Ramp-down | 10s | 10 → 0 | Gracefully decrease load |

**Total Duration:** ~5 minutes 20 seconds

### Request Distribution

Approximate requests per user (per iteration):

- Authentication: 1 login
- Wallet Operations: 13 requests
- Category Operations: 18 requests
- Transaction Operations: 23 requests
- Dashboard/Analytics: 15 requests

**Total per user:** ~70-100 requests (varies due to random sleep times)

### Test Data

**Users:**
- Email pattern: `loadtest-user-{1-10}@example.com`
- Password: `LoadTest123!`
- Name: `Load Test User {1-10}`

**Wallets:**
- Name: Random 5-character string
- Currency: IDR, USD, or EUR (random)
- Initial balance: Random 100,000 - 10,000,000

**Categories:**
- Expense types: Groceries, Transport, Entertainment, Bills, Shopping
- Income types: Salary, Bonus, Investment
- Names include random 3-4 character suffix

**Transactions:**
- Amount: Random 10,000 - 1,000,000
- Date: Random within last 30 days
- Type: INCOME or EXPENSE (random)

## Troubleshooting

### Error: "connection refused"

**Solution:** Ensure application is running on port 8081

```bash
lsof -i :8081
curl http://localhost:8081/actuator/health
```

### Error: High failure rate

**Possible causes:**
1. Database connection issues
2. Application not fully started
3. Insufficient resources (CPU/Memory)

**Solution:**
- Check application logs
- Verify database connectivity
- Reduce number of VUs or duration

### Error: "registration failed"

**Expected behavior:** Registration may fail with 409 (Conflict) after first run because users already exist. This is normal and handled by the script.

**Clean up test data (optional):**
```sql
DELETE FROM users WHERE email LIKE 'loadtest-user-%';
```

### Slow Performance

**Tips to improve:**
1. Run test on same machine as application (reduce network latency)
2. Ensure database has proper indexes
3. Check system resources (CPU, memory, disk I/O)
4. Review application logs for slow queries

## Analyzing Results

### JSON Report

The test generates a detailed JSON report in `reports/summary.json`:

```bash
# View summary
jq '.metrics | keys' reports/summary.json

# View HTTP metrics
jq '.metrics.http_req_duration' reports/summary.json

# View error rate
jq '.metrics.http_req_failed' reports/summary.json
```

### Grafana Integration

If you have Grafana + Prometheus running, you can view real-time metrics:

1. Access Grafana: http://localhost:3000
2. Navigate to Explore
3. Query: `rate(http_server_requests_seconds_count[1m])`

## Advanced Usage

### Environment Variables

```bash
# Custom base URL
export BASE_URL=http://api.example.com
k6 run k6-test.js

# Multiple environment variables
BASE_URL=http://localhost:8080 k6 run k6-test.js
```

### Cloud Execution

Run test in K6 Cloud for distributed load testing:

```bash
# Login to K6 Cloud
k6 login cloud

# Run in cloud
k6 cloud k6-test.js
```

### CI/CD Integration

**GitHub Actions example:**

```yaml
name: Performance Tests

on: [push, pull_request]

jobs:
  performance:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Run K6 test
        uses: grafana/k6-action@v0.3.0
        with:
          filename: performance-tests/k6-test.js
          flags: --out json=reports/results.json
```

## Best Practices

1. **Run on isolated environment:** Avoid running on production
2. **Start small:** Begin with fewer VUs, increase gradually
3. **Monitor resources:** Watch CPU, memory, database connections
4. **Analyze trends:** Run regularly to detect performance regression
5. **Set realistic thresholds:** Based on your SLA requirements

## Support

For issues or questions:
- Check application logs: `logs/application.log`
- Check K6 documentation: https://k6.io/docs/
- Review test output for specific error messages

---

**Last Updated:** November 25, 2025
**K6 Version:** v0.48+
**Target API:** Expense Tracker REST API v1.0