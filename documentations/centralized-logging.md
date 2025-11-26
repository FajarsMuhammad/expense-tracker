# Centralized Logging with Loki + Promtail

## 📋 Overview

Implementasi centralized logging menggunakan **Grafana Loki** sebagai log aggregation system dan **Promtail** sebagai log shipper. Logs dari aplikasi Spring Boot dikumpulkan, diindeks, dan dapat di-query melalui Grafana.

---

## 🏗️ Architecture

```
┌─────────────────┐
│  Spring Boot    │
│   Application   │ ──> Writes JSON logs ──> logs/application.log
└─────────────────┘                          logs/error.log
                                                    │
                                                    │ Scrapes
                                                    ▼
                                            ┌──────────────┐
                                            │   Promtail   │
                                            │ (Log Shipper)│
                                            └──────────────┘
                                                    │
                                                    │ Pushes
                                                    ▼
                                            ┌──────────────┐
                                            │     Loki     │
                                            │ (Log Storage)│
                                            └──────────────┘
                                                    │
                                                    │ Queries
                                                    ▼
                                            ┌──────────────┐
                                            │   Grafana    │
                                            │  (Explore)   │
                                            └──────────────┘
```

---

## 🚀 Components

### 1. Loki (Log Aggregation)
- **Port**: 3100
- **Storage**: Filesystem (`/tmp/loki`)
- **Schema**: v13 with TSDB indexing
- **Retention**: 30 days (720 hours)
- **Config**: `monitoring/loki/loki-config.yaml`

### 2. Promtail (Log Shipper)
- **Port**: 9080 (HTTP), 33537 (gRPC)
- **Config**: `monitoring/promtail/promtail-config.yaml`
- **Jobs**:
  - `expense-tracker-app`: Scrapes `logs/application.log`
  - `expense-tracker-errors`: Scrapes `logs/error.log`

### 3. Logback Configuration
- **JSON Logs**: `logs/application.log`
- **Error Logs**: `logs/error.log`
- **Format**: JSON with LogstashEncoder
- **Fields**: timestamp, level, logger, thread, message, correlation_id, user_email, etc.

---

## 📦 Logs Structure

### Application Log (JSON Format)

```json
{
  "@timestamp": "2025-11-25T15:24:07.412Z",
  "level": "INFO",
  "thread": "http-nio-8081-exec-1",
  "logger": "com.fajars.expensetracker.auth.AuthService",
  "message": "User registered successfully",
  "correlation_id": "2ac36242-6de6-4f02-89b5-9b0693d92fc6",
  "user_email": "testuser@example.com"
}
```

### Error Log (JSON Format)

```json
{
  "@timestamp": "2025-11-25T15:24:29.809Z",
  "level": "ERROR",
  "thread": "http-nio-8081-exec-5",
  "logger": "com.fajars.expensetracker.common.exception.GlobalExceptionHandler",
  "message": "Internal server error occurred",
  "correlation_id": "1b37d471-139e-4783-b3aa-d06dac8fb9b2",
  "exception": "java.lang.NullPointerException",
  "stack_trace": "..."
}
```

---

## 🔍 Querying Logs in Grafana

### Access Grafana Explore

1. Open Grafana: http://localhost:3000
2. Login: **admin** / **admin**
3. Click **Explore** (compass icon)
4. Select **Loki** datasource

### Basic Queries

#### 1. All Application Logs
```logql
{job="expense-tracker"}
```

#### 2. Filter by Log Level
```logql
{job="expense-tracker", level="ERROR"}
{job="expense-tracker", level="INFO"}
{job="expense-tracker", level="DEBUG"}
```

#### 3. Filter by Correlation ID
```logql
{job="expense-tracker"} | json | correlation_id="2ac36242-6de6-4f02-89b5-9b0693d92fc6"
```

#### 4. Filter by User Email
```logql
{job="expense-tracker"} | json | user_email="testuser@example.com"
```

#### 5. Search for Specific Keywords
```logql
{job="expense-tracker"} |= "authentication"
{job="expense-tracker"} |= "ERROR"
{job="expense-tracker"} |~ "login|authentication|register"
```

#### 6. Exclude Certain Logs
```logql
{job="expense-tracker"} != "actuator"
```

#### 7. Error Logs Only
```logql
{job="expense-tracker-errors"}
```

#### 8. Show Only Message Field
```logql
{job="expense-tracker"} | json | line_format "{{.message}}"
```

#### 9. Filter by Logger
```logql
{job="expense-tracker"} | json | logger=~".*AuthService.*"
```

#### 10. Count Logs by Level (Last 5 Minutes)
```logql
sum by (level) (count_over_time({job="expense-tracker"}[5m]))
```

---

## 📊 LogQL Advanced Queries

### Rate Calculations

```logql
# Error rate per second
rate({job="expense-tracker", level="ERROR"}[5m])

# Total log volume
sum(rate({job="expense-tracker"}[5m]))
```

### Pattern Extraction

```logql
# Extract specific fields
{job="expense-tracker"}
  | json
  | line_format "{{.timestamp}} [{{.level}}] {{.logger}}: {{.message}}"
```

### Aggregations

```logql
# Count errors by logger
sum by (logger) (count_over_time({job="expense-tracker", level="ERROR"}[1h]))

# Count logs by correlation_id
topk(10, sum by (correlation_id) (count_over_time({job="expense-tracker"}[1h])))
```

---

## 🐳 Container Management

### Start Containers

```bash
# Start Loki
podman run -d \
  --name loki \
  --network monitoring-network \
  -p 3100:3100 \
  -v $(pwd)/monitoring/loki/loki-config.yaml:/etc/loki/local-config.yaml:ro \
  grafana/loki:latest \
  -config.file=/etc/loki/local-config.yaml

# Start Promtail
podman run -d \
  --name promtail \
  --network monitoring-network \
  -v $(pwd)/monitoring/promtail/promtail-config.yaml:/etc/promtail/config.yml:ro \
  -v $(pwd)/logs:/var/log/app:ro \
  grafana/promtail:latest \
  -config.file=/etc/promtail/config.yml
```

### Check Status

```bash
# Check if containers are running
podman ps --filter "name=loki" --filter "name=promtail"

# Check Loki health
curl http://localhost:3100/ready

# Check Promtail logs
podman logs promtail --tail 50

# Check Loki logs
podman logs loki --tail 50
```

### Stop/Restart Containers

```bash
# Stop containers
podman stop loki promtail

# Start containers
podman start loki promtail

# Restart containers
podman restart loki promtail

# Remove containers
podman rm -f loki promtail
```

---

## 📝 Configuration Files

### 1. Loki Configuration (`monitoring/loki/loki-config.yaml`)

```yaml
auth_enabled: false

server:
  http_listen_port: 3100

common:
  path_prefix: /tmp/loki
  storage:
    filesystem:
      chunks_directory: /tmp/loki/chunks
      rules_directory: /tmp/loki/rules
  replication_factor: 1
  ring:
    instance_addr: 127.0.0.1
    kvstore:
      store: inmemory

schema_config:
  configs:
    - from: 2020-10-24
      store: tsdb
      object_store: filesystem
      schema: v13
      index:
        prefix: index_
        period: 24h

limits_config:
  retention_period: 720h  # 30 days
  allow_structured_metadata: true
  max_query_length: 721h

compactor:
  working_directory: /tmp/loki/compactor
  delete_request_store: filesystem
  compaction_interval: 10m
  retention_enabled: true
  retention_delete_delay: 2h
  retention_delete_worker_count: 150
```

### 2. Promtail Configuration (`monitoring/promtail/promtail-config.yaml`)

```yaml
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: expense-tracker-app
    static_configs:
      - targets:
          - localhost
        labels:
          job: expense-tracker
          environment: development
          __path__: /var/log/app/*.log
    pipeline_stages:
      - json:
          expressions:
            timestamp: timestamp
            level: level
            thread: thread
            logger: logger
            message: message
            correlation_id: correlation_id
            user_email: user_email
      - timestamp:
          source: timestamp
          format: RFC3339Nano
      - labels:
          level:
          correlation_id:
      - output:
          source: message

  - job_name: expense-tracker-errors
    static_configs:
      - targets:
          - localhost
        labels:
          job: expense-tracker-errors
          environment: development
          __path__: /var/log/app/error.log
    pipeline_stages:
      - json:
          expressions:
            timestamp: timestamp
            level: level
            thread: thread
            logger: logger
            message: message
            correlation_id: correlation_id
            exception: exception
      - timestamp:
          source: timestamp
          format: RFC3339Nano
      - labels:
          level:
          correlation_id:
      - output:
          source: message
```

---

## 🔧 Troubleshooting

### Problem: Logs not appearing in Loki

**Check 1**: Verify application is writing logs
```bash
ls -lh logs/
cat logs/application.log
```

**Check 2**: Verify Promtail is running and scraping
```bash
podman logs promtail --tail 50
```

**Check 3**: Verify Loki is ready
```bash
curl http://localhost:3100/ready
```

**Check 4**: Check if Loki has received logs
```bash
curl -s "http://localhost:3100/loki/api/v1/label" | jq
```

### Problem: Promtail container failed

**Solution**: Check Promtail configuration syntax
```bash
podman logs promtail
```

### Problem: Loki container failed

**Solution**: Check Loki configuration
```bash
podman logs loki
```

Common issues:
- Invalid schema version
- Missing storage configuration
- Invalid retention settings

---

## 📈 Best Practices

1. **Use Structured Logging**
   - Always use JSON format for production logs
   - Include correlation IDs for request tracing
   - Include user context (email, ID) when available

2. **Log Retention**
   - Current: 30 days
   - Adjust based on compliance requirements
   - Monitor disk usage

3. **Query Performance**
   - Use label filters before parsing: `{job="expense-tracker", level="ERROR"}`
   - Limit time ranges for queries
   - Use stream selectors efficiently

4. **Security**
   - Do NOT log sensitive data (passwords, tokens, credit cards)
   - Use SensitiveDataFilter utility
   - Mask PII in logs

5. **Monitoring**
   - Monitor Loki disk usage
   - Set up alerts for log ingestion failures
   - Track log volume metrics

---

## 🔗 Useful Links

- [Loki Documentation](https://grafana.com/docs/loki/latest/)
- [Promtail Documentation](https://grafana.com/docs/loki/latest/clients/promtail/)
- [LogQL Query Language](https://grafana.com/docs/loki/latest/logql/)
- [LogQL Examples](https://grafana.com/docs/loki/latest/logql/query_examples/)

---

**Last Updated:** November 25, 2025
**Status:** ✅ Fully Operational
