# Building Docker Image on ARM64 (Apple Silicon)

## Problem

When building Docker image on Apple Silicon (M1/M2/M3), you may encounter this error:

```
Error: creating build container: unable to copy from source docker://gradle:8.5-jdk21-alpine:
choosing an image from manifest list docker://gradle:8.5-jdk21-alpine:
no image found in manifest list for architecture "arm64", variant "v8", OS "linux"
```

This happens because some Docker images don't have ARM64 variants available.

---

## Solution: Use Eclipse Temurin Base Images

Eclipse Temurin provides official OpenJDK images with multi-architecture support including ARM64.

### Updated Dockerfile

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build
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
FROM eclipse-temurin:21-jre
WORKDIR /app

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

# Health check (using curl instead of wget for debian-based image)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "/app/expense-tracker.jar"]
```

### Key Changes

1. **Build Stage**:
   - Changed from `gradle:8.5-jdk21-alpine` to `eclipse-temurin:21-jdk`
   - Added gradlew copy to use the Gradle wrapper
   - Use `./gradlew` instead of global `gradle` command

2. **Runtime Stage**:
   - Changed from `eclipse-temurin:21-jre-alpine` to `eclipse-temurin:21-jre`
   - Changed user creation commands for Debian-based image:
     - Alpine: `addgroup -S` / `adduser -S`
     - Debian: `groupadd -r` / `useradd -r`
   - Changed healthcheck from `wget` to `curl` (Debian default)
   - Added logs directory creation

---

## Building the Image

### Using Podman

```bash
# Build image
podman build -t expense-tracker:latest .

# Verify image was built
podman images | grep expense-tracker

# Run container
podman run -d \
  --name expense-tracker-app \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/expense_tracker \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  expense-tracker:latest
```

### Using Docker Compose

```bash
# Build and start all services
podman-compose -f docker-compose.yml up -d --build

# Or just build without starting
podman-compose -f docker-compose.yml build
```

---

## Alternative Solutions

### Option 1: Use Platform Flag (Multi-arch)

If you still want to use Alpine-based images:

```dockerfile
FROM --platform=linux/amd64 gradle:8.5-jdk21-alpine AS build
```

**Note**: This will use emulation (slower) and may not work on all systems.

### Option 2: Build Locally (Current Solution)

Use Eclipse Temurin images which natively support ARM64. This is the recommended approach.

### Option 3: Use Pre-built JAR

Skip the build stage entirely if you build locally:

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy pre-built JAR
COPY build/libs/*.jar expense-tracker.jar

# ... rest of configuration
```

Then build JAR locally first:
```bash
./gradlew clean bootJar
podman build -t expense-tracker:latest .
```

---

## Troubleshooting

### Issue: Build is very slow

**Solution**: The first build will be slow because it downloads dependencies. Subsequent builds will be faster due to Docker layer caching.

### Issue: "gradlew: Permission denied"

**Solution**: Make sure gradlew is executable:
```bash
chmod +x gradlew
git add gradlew
git commit -m "Make gradlew executable"
```

### Issue: Out of memory during build

**Solution**: Increase Docker/Podman memory limit:
```bash
# For Podman
podman machine set --memory 4096

# Or limit Gradle memory in gradle.properties
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
```

### Issue: Health check fails

**Solution**: Make sure curl is available in the image, or change health check to use Java:

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD java -cp /app/expense-tracker.jar org.springframework.boot.loader.JarLauncher --spring.main.web-application-type=none --spring.application.name=healthcheck || exit 1
```

Or install curl:
```dockerfile
# After base image
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
```

---

## Verification

After building, verify the image:

```bash
# Check image details
podman inspect expense-tracker:latest

# Check architecture
podman inspect expense-tracker:latest | grep Architecture

# Test run
podman run --rm expense-tracker:latest java -version

# Check layers
podman history expense-tracker:latest
```

---

## Image Size Comparison

- **Alpine-based** (if available): ~150-200 MB
- **Debian-based (Temurin)**: ~250-300 MB

The Debian-based image is slightly larger but provides better compatibility and native ARM64 support.

---

## Best Practices

1. **Multi-stage builds**: Separate build and runtime stages to minimize final image size
2. **Use specific versions**: Avoid `latest` tag in production
3. **Layer caching**: Order COPY commands from least to most frequently changing
4. **Security**: Run as non-root user
5. **Health checks**: Always include health checks for production
6. **Logs**: Create logs directory with proper permissions

---

**Last Updated**: November 25, 2025
**Tested On**: macOS (ARM64) with Podman
