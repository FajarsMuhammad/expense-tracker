# API Configuration Improvement - Centralized Context Path

## Summary
Improved the API versioning implementation by centralizing the `/api/v1` prefix in the application configuration instead of hardcoding it in every controller.

## Changes Made

### 1. Application Configuration (application.yaml)

**File**: `src/main/resources/application.yaml`

Added centralized context-path configuration:
```yaml
server:
  port: 8081
  servlet:
    context-path: /api/v1
```

### 2. Controllers Simplified (6 controllers)

All controllers now use simple resource paths without the `/api/v1` prefix:

#### Before (Hardcoded Prefix)
```java
@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController { }
```

#### After (Clean Resource Path)
```java
@RestController
@RequestMapping("/wallets")
public class WalletController { }
```

### Controllers Updated:
1. **AuthController**: `@RequestMapping("/auth")`
2. **UserController**: `@RequestMapping` (empty for `/me` endpoint)
3. **WalletController**: `@RequestMapping("/wallets")`
4. **CategoryController**: `@RequestMapping("/categories")`
5. **TransactionController**: `@RequestMapping("/transactions")`
6. **DashboardController**: `@RequestMapping("/dashboard")`

### 3. Security Configuration Simplified

**File**: `SecurityConfig.java`

```java
.requestMatchers("/auth/**").permitAll()  // Clean path
```

The context-path is automatically applied by Spring Boot, so security matchers use clean paths.

## Benefits

### 1. Single Source of Truth
- API version is configured in **ONE place**: `application.yaml`
- Easy to update version across entire application
- No need to modify multiple controller files

### 2. Environment-Specific Configuration
Can easily override in different environments:

```yaml
# application-dev.yaml
server:
  servlet:
    context-path: /api/v1

# application-staging.yaml
server:
  servlet:
    context-path: /api/v2

# application-production.yaml
server:
  servlet:
    context-path: /api/v1
```

### 3. Cleaner Code
- Controllers focus on resource paths only
- More readable and maintainable
- Follows Spring Boot best practices

### 4. Easy Version Migration
To upgrade to v2:
1. Change **one line** in `application.yaml`
2. No controller modifications needed
3. All endpoints automatically updated

### 5. Testing Benefits
- Tests use relative paths
- Context-path applied automatically by Spring Boot test framework
- More portable tests

## How It Works

### Request Flow
```
Client Request: http://localhost:8081/api/v1/wallets
                                      ↓
Server Context Path: /api/v1 (configured in application.yaml)
                                      ↓
Controller Mapping: /wallets (clean resource path)
                                      ↓
Final Endpoint: /api/v1/wallets
```

### URL Structure
```
http://localhost:8081/api/v1/resource
│                    │         │
├─ Host & Port       │         └─ Resource (Controller @RequestMapping)
│                    │
│                    └─ Context Path (application.yaml)
│
└─ Protocol & Domain
```

## Configuration Reference

### Full application.yaml Server Section
```yaml
server:
  port: 8081
  servlet:
    context-path: /api/v1
```

### Alternative Configurations

#### No Versioning (Direct Resources)
```yaml
server:
  port: 8081
  # No context-path = resources at root
```
Result: `http://localhost:8081/wallets`

#### Different Version
```yaml
server:
  port: 8081
  servlet:
    context-path: /api/v2
```
Result: `http://localhost:8081/api/v2/wallets`

#### Custom Prefix
```yaml
server:
  port: 8081
  servlet:
    context-path: /expense-api
```
Result: `http://localhost:8081/expense-api/wallets`

## Accessing the API

### All Endpoints Automatically Prefixed
- `http://localhost:8081/api/v1/auth/login`
- `http://localhost:8081/api/v1/auth/register`
- `http://localhost:8081/api/v1/me`
- `http://localhost:8081/api/v1/wallets`
- `http://localhost:8081/api/v1/categories`
- `http://localhost:8081/api/v1/transactions`
- `http://localhost:8081/api/v1/dashboard/summary`

### Special Endpoints (Outside Context Path)
Some endpoints bypass the context-path and remain at root:
- `http://localhost:8081/actuator/**` - Health checks and metrics
- `http://localhost:8081/swagger-ui.html` - API documentation UI
- `http://localhost:8081/v3/api-docs/**` - OpenAPI specification

These are configured in `SecurityConfig` to be accessible at root level.

## Migration from Hardcoded Prefix

### What Changed
- ✅ Removed `/api/v1` from all `@RequestMapping` annotations
- ✅ Added `context-path: /api/v1` to `application.yaml`
- ✅ Updated `SecurityConfig` to use clean paths
- ✅ All endpoints still accessible at same URLs

### What Stayed the Same
- ✅ External API URLs unchanged
- ✅ Frontend doesn't need updates
- ✅ Documentation URLs still valid
- ✅ All tests still pass

## Environment Variables Override

Can override via environment variable:
```bash
SERVER_SERVLET_CONTEXT_PATH=/api/v2 java -jar expense-tracker.jar
```

Or via command-line argument:
```bash
java -jar expense-tracker.jar --server.servlet.context-path=/api/v2
```

## Testing Checklist

- ✅ Build successful
- ✅ All tests passing
- ✅ Controllers use clean paths
- ✅ SecurityConfig updated
- ✅ context-path configured in application.yaml
- ✅ No hardcoded `/api/v1` in controllers
- ✅ Application compiles without errors

## Files Modified

### Configuration (1 file)
1. `src/main/resources/application.yaml` - Added `server.servlet.context-path`

### Controllers (6 files)
1. `AuthController.java` - Simplified to `/auth`
2. `UserController.java` - Simplified to empty (for `/me`)
3. `WalletController.java` - Simplified to `/wallets`
4. `CategoryController.java` - Simplified to `/categories`
5. `TransactionController.java` - Simplified to `/transactions`
6. `DashboardController.java` - Simplified to `/dashboard`

### Security (1 file)
1. `SecurityConfig.java` - Updated matchers to clean paths

## Best Practices Followed

1. ✅ **DRY (Don't Repeat Yourself)** - Version defined once
2. ✅ **Configuration over Code** - Settings in YAML, not Java
3. ✅ **Separation of Concerns** - Controllers focus on business logic
4. ✅ **Easy to Test** - Clean paths in tests
5. ✅ **Easy to Maintain** - Change version in one place

## Troubleshooting

### If Endpoints Return 404
Check that:
1. `application.yaml` has correct `context-path` setting
2. Controllers use clean paths (no `/api/v1` prefix)
3. Application restarted after configuration change

### To Verify Configuration
Access Swagger UI at: `http://localhost:8081/swagger-ui.html`
All endpoints should show with `/api/v1` prefix automatically.

## Conclusion

The API versioning is now properly configured at the application level, making it easier to maintain, test, and evolve. This follows Spring Boot best practices and provides a professional, maintainable codebase structure.

### Key Takeaway
**One configuration change updates all endpoints** - This is the power of centralized configuration!
