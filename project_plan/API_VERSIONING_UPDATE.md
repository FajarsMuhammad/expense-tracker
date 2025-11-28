# API Versioning Update - /api/v1 Prefix

## Summary
All API endpoints have been updated to use the `/api/v1` prefix for consistent API versioning and better organization.

## Changes Made

### Controllers Updated

#### 1. AuthController
- **Old Path**: `/auth/**`
- **New Path**: `/api/v1/auth/**`
- **Endpoints**:
  - POST `/api/v1/auth/register`
  - POST `/api/v1/auth/login`
  - POST `/api/v1/auth/refresh`

#### 2. UserController
- **Old Path**: `/me`
- **New Path**: `/api/v1/me`
- **Endpoints**:
  - GET `/api/v1/me`

#### 3. WalletController
- **Old Path**: `/wallets`
- **New Path**: `/api/v1/wallets`
- **Endpoints**:
  - GET `/api/v1/wallets`
  - POST `/api/v1/wallets`
  - GET `/api/v1/wallets/{id}`
  - PUT `/api/v1/wallets/{id}`
  - DELETE `/api/v1/wallets/{id}`

#### 4. CategoryController
- **Old Path**: `/api/v1/categories` (already correct)
- **New Path**: `/api/v1/categories` (no change)
- **Endpoints**:
  - GET `/api/v1/categories`
  - POST `/api/v1/categories`
  - GET `/api/v1/categories/{id}`
  - PUT `/api/v1/categories/{id}`
  - DELETE `/api/v1/categories/{id}`

#### 5. DashboardController
- **Old Path**: `/dashboard`
- **New Path**: `/api/v1/dashboard`
- **Endpoints**:
  - GET `/api/v1/dashboard/summary`

#### 6. TransactionController
- **Path**: `/api/v1/transactions` (newly implemented)
- **Endpoints**:
  - GET `/api/v1/transactions`
  - POST `/api/v1/transactions`
  - GET `/api/v1/transactions/{id}`
  - PUT `/api/v1/transactions/{id}`
  - DELETE `/api/v1/transactions/{id}`

### Security Configuration Updated

**File**: `src/main/java/com/fajars/expensetracker/common/security/SecurityConfig.java`

Updated the security matcher from:
```java
.requestMatchers("/auth/**").permitAll()
```

To:
```java
.requestMatchers("/api/v1/auth/**").permitAll()
```

### Documentation Updated

**File**: `documentations/api_endpoints.md`

1. **Quick Reference Table**: Updated all base paths to include `/api/v1` prefix
2. **All Endpoint Examples**: Updated HTTP request paths
3. **cURL Examples**: Updated all curl commands with new paths
4. **Error Response Examples**: Updated path fields in error responses
5. **Complete Workflow Example**: Updated all API calls

#### New Transaction Section Added
- Comprehensive documentation for all transaction endpoints
- Request/response examples
- Query parameter documentation
- Validation rules
- Filter and pagination examples

## Benefits

### 1. API Versioning
- Clear version indication in URL path
- Easy to introduce v2 in future without breaking changes
- Industry standard practice

### 2. Consistency
- All endpoints now follow the same pattern
- Predictable URL structure
- Better developer experience

### 3. Organization
- Clear separation from non-API routes (actuator, swagger-ui)
- Easier to apply security rules
- Better API gateway integration

### 4. Documentation
- Complete and up-to-date documentation
- All transaction endpoints documented
- Consistent examples throughout

## Migration Guide for Frontend/API Clients

### Before (Old Paths)
```bash
POST /auth/login
GET /me
GET /wallets
GET /dashboard/summary
```

### After (New Paths)
```bash
POST /api/v1/auth/login
GET /api/v1/me
GET /api/v1/wallets
GET /api/v1/dashboard/summary
```

### Breaking Change Notice
⚠️ **IMPORTANT**: This is a breaking change for existing API clients. All clients must update their base URL to include `/api/v1`.

### Recommended Update Approach
1. Update API base URL configuration in one place
2. Add `/api/v1` prefix to base URL constant
3. Test all endpoints

Example:
```javascript
// Before
const API_BASE_URL = 'http://localhost:8081';

// After
const API_BASE_URL = 'http://localhost:8081/api/v1';
```

## Testing Checklist

- ✅ All controllers updated with new @RequestMapping
- ✅ SecurityConfig updated for auth endpoints
- ✅ Build successful
- ✅ All tests passing
- ✅ Documentation updated
- ✅ cURL examples updated
- ✅ Error response examples updated

## Next Steps

1. Update frontend application to use new API paths
2. Update any API documentation tools (Postman collections, etc.)
3. Communicate changes to all stakeholders
4. Monitor logs for any 404 errors indicating missed endpoints

## Files Modified

### Source Code (7 files)
1. `src/main/java/com/fajars/expensetracker/auth/AuthController.java`
2. `src/main/java/com/fajars/expensetracker/user/UserController.java`
3. `src/main/java/com/fajars/expensetracker/wallet/WalletController.java`
4. `src/main/java/com/fajars/expensetracker/dashboard/DashboardController.java`
5. `src/main/java/com/fajars/expensetracker/category/CategoryController.java` (already correct)
6. `src/main/java/com/fajars/expensetracker/transaction/TransactionController.java` (new)
7. `src/main/java/com/fajars/expensetracker/common/security/SecurityConfig.java`

### Documentation (1 file)
1. `documentations/api_endpoints.md` - Comprehensive update with all new paths and transaction documentation

## Swagger UI

Access the interactive API documentation at:
```
http://localhost:8081/swagger-ui.html
```

All endpoints will automatically show the updated `/api/v1` prefix in the Swagger UI.

## Conclusion

The API versioning update has been successfully completed. All endpoints now use a consistent `/api/v1` prefix, making the API more professional, maintainable, and ready for future evolution.
