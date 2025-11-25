## 2️⃣ Error Tracking & Monitoring

### Sentry Integration

* [ ] Add Sentry dependencies
  ```gradle
  implementation 'io.sentry:sentry-spring-boot-starter:6.34.0'
  implementation 'io.sentry:sentry-logback:6.34.0'
  ```
* [ ] Create Sentry account (free tier)
* [ ] Get DSN from Sentry dashboard
* [ ] Configure Sentry in `application.yml`
    * [ ] Set DSN
    * [ ] Set environment (dev/staging/prod)
    * [ ] Set traces sample rate
    * [ ] Configure release versioning
* [ ] Add Sentry to Logback appender
* [ ] Test error capture with dummy exception
* [ ] Configure error filtering
    * [ ] Ignore health check failures
    * [ ] Ignore expected business exceptions
* [ ] Set up error alerts
    * [ ] Email for critical errors
    * [ ] Slack webhook for production errors
* [ ] Configure user context
    * [ ] Capture user ID
    * [ ] Capture user email (hashed)
    * [ ] Add custom tags (plan type, wallet count)

### Sentry Features to Configure

* [ ] Performance monitoring (APM)
    * [ ] Enable traces
    * [ ] Monitor slow transactions
    * [ ] Track database queries
* [ ] Release tracking
    * [ ] Tag releases with git commit
    * [ ] Track errors per release
* [ ] User feedback
    * [ ] Allow users to report issues
    * [ ] Include screenshot capability

---