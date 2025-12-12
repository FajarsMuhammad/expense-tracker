Cara Mengakses & Monitoring

1. Prometheus - http://localhost:9090

- Buka browser dan akses http://localhost:9090
- Untuk cek apakah aplikasi ter-scrape:
    - Klik Status → Targets
    - Cari job expense-tracker, seharusnya status UP
- Coba query metrics:
    - Masuk ke Graph tab
    - Ketik: http_server_requests_seconds_count
    - Klik Execute

2. Grafana - http://localhost:3000

- Login credentials:
    - Username: admin
    - Password: admin
- Tambah Data Source Prometheus:
  a. Klik ⚙️ (Configuration) → Data Sources
  b. Klik Add data source
  c. Pilih Prometheus
  d. URL: http://prometheus:9090 (atau http://host.docker.internal:9090)
  e. Klik Save & Test
- Import Dashboard (opsional):
  a. Klik + → Import
  b. ID: 4701 (JVM Micrometer)
  c. Pilih Prometheus data source
  d. Klik Import

3. Aplikasi Metrics - http://localhost:8081/actuator/prometheus

- Endpoint ini menampilkan semua metrics dalam format Prometheus

Metrics Penting yang Bisa Dimonitor

Di Prometheus/Grafana, Anda bisa query metrics seperti:
- http_server_requests_seconds_count - Total HTTP requests
- jvm_memory_used_bytes - JVM memory usage
- process_cpu_usage - CPU usage
- jdbc_connections_active - Active database connections
- logback_events_total - Log events

Cara Membuat Dashboard di Grafana:

Opsi 1: Buat Dashboard Manual (Recommended untuk Learning)

1. Login ke Grafana: http://localhost:3000 (admin/admin)
2. Buat Dashboard Baru:
   - Klik "+" → Dashboard → Add visualization
   - Pilih Prometheus data source
3. Panel 1: HTTP Request Rate
   - Query:

rate(http_server_requests_seconds_count[5m])
- Legend: {{uri}} - {{method}} - {{status}}
- Panel title: "HTTP Request Rate (req/s)"
- Visualization: Time series
4. Panel 2: Total HTTP Requests
   - Klik Add → Visualization
   - Query:
   sum(http_server_requests_seconds_count)
   - Panel title: "Total HTTP Requests"
   - Visualization: Stat
5. Panel 3: HTTP Status Codes
   - Klik Add → Visualization
   - Query:
   sum by(status) (http_server_requests_seconds_count)
   - Panel title: "Requests by Status Code"
   - Visualization: Pie chart
   - Legend: {{status}}
6. Panel 4: JVM Memory Usage
   - Klik Add → Visualization
   - Query:
   jvm_memory_used_bytes{area="heap"}
   - Panel title: "JVM Heap Memory"
   - Visualization: Time series
   - Unit: bytes (SI)
7. Panel 5: Response Time (P95)
   - Klik Add → Visualization
   - Query:
   histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, uri))
   - Panel title: "Response Time (95th Percentile)"
   - Visualization: Time series
   - Unit: seconds
8. Simpan Dashboard:
   - Klik 💾 Save (icon di pojok kanan atas)
   - Nama: "Expense Tracker Monitoring"
   - Klik Save

Opsi 2: Import Dashboard Siap Pakai (Quick Start)

1. Login ke Grafana: http://localhost:3000
2. Klik "+" → Import
3. Masukkan ID: 4701 (Spring Boot 2.1 Statistics)
4. Klik Load
5. Pilih Prometheus data source
6. Klik Import

  ---
Query Prometheus yang Berguna:

Anda bisa coba query-query ini di Prometheus (http://localhost:9090):

# Total requests per endpoint
sum by(uri, method) (http_server_requests_seconds_count)

# Request rate (last 5 minutes)
rate(http_server_requests_seconds_count[5m])

# Error rate (4xx + 5xx)
sum(rate(http_server_requests_seconds_count{status=~"4..|5.."}[5m]))

# JVM memory usage
jvm_memory_used_bytes / jvm_memory_max_bytes

# Database connections
hikaricp_connections_active

Perintah Manajemen Container

# Lihat logs
podman logs prometheus
podman logs grafana

# Restart container
podman restart prometheus
podman restart grafana

# Stop containers
podman stop prometheus grafana

# Start containers (jika sudah dibuat)
podman start prometheus grafana

# Remove containers
podman rm -f prometheus grafana