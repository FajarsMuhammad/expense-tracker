# Stage 1: Build
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy gradle wrapper
COPY gradlew ./
COPY gradle ./gradle

# Copy gradle files for dependency caching
COPY build.gradle settings.gradle ./

# Download dependencies
RUN ./gradlew dependencies --no-daemon || return 0

# Copy source code
COPY src ./src

# Build application
RUN ./gradlew bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:25-jre
WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create logs directory
RUN mkdir -p /app/logs && chmod 755 /app/logs

# Create non-root user (for debian-based image)
RUN groupadd -r spring && useradd -r -g spring spring

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar expense-tracker.jar

# Set ownership
RUN chown -R spring:spring /app

USER spring:spring

# Expose port
EXPOSE 8081

# Health check - actuator endpoints are outside context-path
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run application with Java 25 optimizations
# Virtual threads are enabled via application.yaml (spring.threads.virtual.enabled=true)
# Note: ZGC is generational by default in Java 25, no need for -XX:+ZGenerational
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseZGC", \
  "-Xlog:gc*:file=/app/logs/gc.log:time,uptime:filecount=5,filesize=10M", \
  "-jar", "/app/expense-tracker.jar"]
