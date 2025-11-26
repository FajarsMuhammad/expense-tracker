# Monitoring Stack Setup

This directory contains the configuration for the monitoring stack (Prometheus + Grafana) for the Expense Tracker application.

## 📋 Components

- **Prometheus**: Metrics collection and storage
- **Grafana**: Metrics visualization and dashboards
- **Application**: Spring Boot app with Actuator endpoints

## 🚀 Quick Start

### 1. Copy Environment Variables

```bash
cp .env.example .env
# Edit .env with your configuration
```

### 2. Start the Monitoring Stack

```bash
# Start all services
docker-compose -f docker-compose.yml up -d

# View logs
docker-compose -f docker-compose.yml logs -f

# Stop services
docker-compose -f docker-compose.yml down
```

### 3. Access Services

- **Application**: http://localhost:8081
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (default login: admin/admin)
- **Actuator Endpoints**:
  - Health: http://localhost:8081/actuator/health
  - Metrics: http://localhost:8081/actuator/metrics
  - Prometheus: http://localhost:8081/actuator/prometheus

## 📊 Grafana Dashboards

### Initial Setup

1. Login to Grafana (http://localhost:3000)
   - Username: admin
   - Password: admin (change on first login)

2. Prometheus datasource is automatically configured

3. Import Spring Boot Dashboard:
   - Click **+** → **Import**
   - Enter dashboard ID: **11378** (JVM Micrometer)
   - Or ID: **4701** (JVM Dashboard)
   - Select Prometheus datasource

### Custom Metrics Available

**Counters:**
- `users_registered_total` - Total user registrations
- `login_attempts_total` - Login attempts (tagged: result=success/failure)
- `transactions_created_total` - Total transactions created
- `wallets_created_total` - Total wallets created
- `categories_created_total` - Total categories created

**Timers:**
- `transaction_creation_duration_seconds` - Transaction creation timing
- `dashboard_summary_generation_duration_seconds` - Dashboard generation timing

**JVM Metrics (built-in):**
- `jvm_memory_used_bytes`
- `jvm_gc_pause_seconds`
- `jvm_threads_states_threads`
- `http_server_requests_seconds`

## 🔍 Prometheus Queries

### Example Queries

**Request Rate:**
```promql
rate(http_server_requests_seconds_count[5m])
```

**Error Rate:**
```promql
rate(http_server_requests_seconds_count{status=~"5.."}[5m])
```

**Average Response Time:**
```promql
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])
```

**User Registrations per Hour:**
```promql
increase(users_registered_total[1h])
```

**Login Success Rate:**
```promql
rate(login_attempts_total{result="success"}[5m]) / rate(login_attempts_total[5m])
```

## 🏗️ Directory Structure

```
monitoring/
├── prometheus/
│   └── prometheus.yml          # Prometheus configuration
├── grafana/
│   └── provisioning/
│       ├── datasources/
│       │   └── prometheus.yml  # Grafana datasource config
│       └── dashboards/         # Custom dashboards (add here)
└── README.md                   # This file
```

## 🔧 Configuration

### Prometheus Configuration

Edit `prometheus/prometheus.yml` to:
- Change scrape interval
- Add new targets
- Configure alerting rules

### Grafana Configuration

Grafana is pre-configured with:
- Prometheus datasource
- Auto-provisioning enabled
- Plugin installation on startup

## 📈 Creating Custom Dashboards

1. Create dashboard in Grafana UI
2. Export as JSON
3. Save to `monitoring/grafana/provisioning/dashboards/`
4. Add dashboard provider config (see Grafana docs)

## 🐛 Troubleshooting

### Application Not Showing Metrics

```bash
# Check if application is running
curl http://localhost:8081/actuator/health

# Check Prometheus metrics endpoint
curl http://localhost:8081/actuator/prometheus
```

### Prometheus Can't Scrape Application

```bash
# Check Prometheus targets
# Go to: http://localhost:9090/targets

# Check logs
docker-compose -f docker-compose.yml logs prometheus
```

### Grafana Can't Connect to Prometheus

```bash
# Check datasource connection
# Grafana → Configuration → Data Sources → Prometheus → Test

# Check Prometheus is accessible
docker-compose -f docker-compose.yml exec grafana wget -O- http://prometheus:9090/-/healthy
```

## 🔒 Security Notes

- Change default Grafana password immediately
- Don't expose Prometheus/Grafana ports in production
- Use reverse proxy with authentication
- Configure HTTPS for production

## 📚 Resources

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
