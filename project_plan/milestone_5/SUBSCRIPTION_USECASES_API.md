# Subscription Use Cases - API Documentation

**Document Version**: 1.0
**Created**: 2024-12-06
**Author**: Expense Tracker Development Team

## Overview

This document provides API documentation for the new subscription use case architecture. All subscription-related operations now follow the **Use Case Pattern** for better maintainability, testability, and separation of concerns.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Use Cases List](#use-cases-list)
3. [API Endpoints](#api-endpoints)
4. [DTOs and Models](#dtos-and-models)
5. [Error Handling](#error-handling)
6. [Migration Guide](#migration-guide)

---

## Architecture Overview

### Use Case Pattern

All subscription business logic is now organized into dedicated use cases:

```
subscription/
├── usecase/
│   ├── CreateFreeSubscription.java             (Interface)
│   ├── CreateFreeSubscriptionUseCase.java      (Implementation)
│   ├── CreateTrialSubscription.java            (Interface)
│   ├── CreateTrialSubscriptionUseCase.java     (Implementation)
│   ├── ActivateSubscription.java               (Interface)
│   ├── ActivateSubscriptionUseCase.java        (Implementation)
│   ├── CancelSubscription.java                 (Interface)
│   ├── CancelSubscriptionUseCase.java          (Implementation)
│   ├── GetUserSubscription.java                (Interface)
│   ├── GetUserSubscriptionUseCase.java         (Implementation)
│   ├── CheckTrialEligibility.java              (Interface)
│   └── CheckTrialEligibilityUseCase.java       (Implementation)
├── SubscriptionHelper.java                     (Domain service for utilities)
└── SubscriptionService.java                    (@Deprecated - for backward compatibility)
```

### Benefits

✅ **Single Responsibility**: Each use case handles one specific operation
✅ **Testability**: Easy to mock and test in isolation
✅ **Maintainability**: Changes to one operation don't affect others
✅ **Consistency**: Same pattern as payment module

---

## Use Cases List

### 1. CreateFreeSubscription

**Purpose**: Create a FREE tier subscription for new users.

**Interface**:
```java
public interface CreateFreeSubscription {
    Subscription createFree(UUID userId);
}
```

**When to use**:
- During user registration
- When premium subscription expires without renewal
- Manual downgrade to FREE tier

**Business Rules**:
- FREE tier has no expiry date
- No payment provider information
- Status is always ACTIVE

---

### 2. CheckTrialEligibility

**Purpose**: Check if user is eligible for 14-day free trial.

**Interface**:
```java
public interface CheckTrialEligibility {
    boolean isEligible(UUID userId);
}
```

**Eligibility Criteria**:
- User has never had a premium subscription before
- User has never made a successful payment

**Returns**:
- `true` if eligible
- `false` if not eligible

---

### 3. CreateTrialSubscription

**Purpose**: Activate 14-day free trial for eligible users.

**Interface**:
```java
public interface CreateTrialSubscription {
    Subscription createTrial(UUID userId);
}
```

**When to use**:
- User clicks "Start Trial" button
- Auto-upgrade flow for eligible users

**Business Rules**:
- Checks eligibility using `CheckTrialEligibility`
- Trial duration: 14 days
- Tier: PREMIUM
- Status: TRIAL
- Cancels any existing FREE subscription

**Throws**:
- `BusinessException` with status 403 if user is not eligible
- `BusinessException` with status 404 if user not found

---

### 4. ActivateSubscription

**Purpose**: Activate or extend premium subscription after successful payment.

**Interface**:
```java
public interface ActivateSubscription {
    Subscription activateOrExtend(UUID userId, UUID paymentId, int days);
}
```

**When to use**:
- After successful payment webhook from Midtrans
- Manual subscription activation (admin)

**Business Logic**:
1. If user has active PREMIUM subscription → **extend** it
2. If user has FREE or no subscription → **create** new PREMIUM
3. If user has expired subscription → cancel it and **create** new PREMIUM

**Parameters**:
- `userId`: User ID
- `paymentId`: Payment transaction ID (for reference)
- `days`: Number of days to activate/extend

**Returns**:
- Activated or extended `Subscription`

---

### 5. CancelSubscription

**Purpose**: Cancel premium subscription (user-initiated).

**Interface**:
```java
public interface CancelSubscription {
    void cancel(UUID userId);
}
```

**When to use**:
- User clicks "Cancel Subscription" button
- User requests cancellation via support

**Business Rules**:
- Can only cancel PREMIUM or TRIAL subscriptions
- Cannot cancel FREE tier
- Subscription remains active until `endedAt` date
- After expiry, user is downgraded to FREE tier

**Throws**:
- `BusinessException` with status 404 if no active subscription
- `BusinessException` with status 400 if trying to cancel FREE tier

---

### 6. GetUserSubscription

**Purpose**: Retrieve user's active subscription.

**Interface**:
```java
public interface GetUserSubscription {
    Subscription getSubscription(UUID userId);
}
```

**When to use**:
- GET `/subscriptions/status` endpoint
- Checking subscription tier before feature access
- Displaying subscription info in UI

**Returns**:
- Active `Subscription` for the user

**Throws**:
- `BusinessException` with status 404 if no active subscription found

---

### 7. SubscriptionHelper

**Purpose**: Domain service for subscription-related utility functions.

**Methods**:
```java
public class SubscriptionHelper {
    boolean isPremiumUser(UUID userId);
    SubscriptionTier getUserTier(UUID userId);
    int getExportLimit(UUID userId);
    int getDateRangeLimit(UUID userId);
}
```

**When to use**:
- Checking feature access (premium vs free)
- Getting tier-based limits
- Validation before operations

**Not for**:
- Business logic orchestration (use Use Cases instead)
- Complex subscription operations (use Use Cases instead)

---

## API Endpoints

### 1. GET /subscriptions/status

**Description**: Get current user's subscription status

**Authentication**: Required (JWT)

**Request**:
```http
GET /api/v1/subscriptions/status
Authorization: Bearer {jwt_token}
```

**Response** (200 OK):
```json
{
  "tier": "PREMIUM",
  "status": "ACTIVE",
  "isPremium": true,
  "isTrial": false,
  "startedAt": "2024-12-01T00:00:00",
  "endedAt": "2025-01-01T00:00:00"
}
```

**Implementation**:
```java
@GetMapping("/status")
public ResponseEntity<SubscriptionStatusResponse> getStatus(
    @AuthenticationPrincipal UserDetails userDetails
) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    Subscription subscription = getUserSubscription.getSubscription(userId);
    // ... map to response
}
```

---

### 2. POST /subscriptions/trial

**Description**: Start 14-day free trial

**Authentication**: Required (JWT)

**Request**:
```http
POST /api/v1/subscriptions/trial
Authorization: Bearer {jwt_token}
```

**Response** (200 OK):
```json
{
  "tier": "PREMIUM",
  "status": "TRIAL",
  "isPremium": true,
  "isTrial": true,
  "startedAt": "2024-12-06T10:00:00",
  "endedAt": "2024-12-20T10:00:00"
}
```

**Error** (403 Forbidden) - Not eligible:
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "User is not eligible for trial",
  "timestamp": "2024-12-06T10:00:00"
}
```

**Implementation**:
```java
@PostMapping("/trial")
public ResponseEntity<SubscriptionStatusResponse> startTrial(
    @AuthenticationPrincipal UserDetails userDetails
) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    Subscription subscription = createTrialSubscription.createTrial(userId);
    // ... map to response
}
```

---

### 3. POST /subscriptions/cancel

**Description**: Cancel premium subscription

**Authentication**: Required (JWT)

**Request**:
```http
POST /api/v1/subscriptions/cancel
Authorization: Bearer {jwt_token}
```

**Response** (200 OK):
```json
{
  "tier": "PREMIUM",
  "status": "CANCELLED",
  "isPremium": true,
  "isTrial": false,
  "startedAt": "2024-12-01T00:00:00",
  "endedAt": "2025-01-01T00:00:00"
}
```

**Error** (400 Bad Request) - Cannot cancel FREE:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot cancel FREE tier subscription",
  "timestamp": "2024-12-06T10:00:00"
}
```

**Implementation**:
```java
@PostMapping("/cancel")
public ResponseEntity<SubscriptionStatusResponse> cancel(
    @AuthenticationPrincipal UserDetails userDetails
) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    cancelSubscription.cancel(userId);
    Subscription subscription = getUserSubscription.getSubscription(userId);
    // ... map to response
}
```

---

## DTOs and Models

### SubscriptionStatusResponse

```java
public record SubscriptionStatusResponse(
    SubscriptionTier tier,
    SubscriptionStatus status,
    boolean isPremium,
    boolean isTrial,
    LocalDateTime startedAt,
    LocalDateTime endedAt
) {}
```

### SubscriptionTier (Enum)

```java
public enum SubscriptionTier {
    FREE,
    PREMIUM
}
```

### SubscriptionStatus (Enum)

```java
public enum SubscriptionStatus {
    ACTIVE,      // Currently active
    TRIAL,       // Trial period
    EXPIRED,     // Expired (past endedAt)
    CANCELLED,   // User cancelled
    PENDING      // Waiting for payment
}
```

---

## Error Handling

### Common Error Responses

| Status Code | Error Type | When |
|-------------|-----------|------|
| 400 | Bad Request | Invalid input, cannot cancel FREE tier |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Not eligible for trial |
| 404 | Not Found | User or subscription not found |
| 500 | Internal Server Error | Unexpected server error |

### Error Response Format

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No active subscription found",
  "timestamp": "2024-12-06T10:00:00",
  "path": "/api/v1/subscriptions/status"
}
```

---

## Migration Guide

### From SubscriptionService to Use Cases

**Before** (Deprecated):
```java
@RestController
public class MyController {
    private final SubscriptionService subscriptionService; // ❌ Deprecated

    @PostMapping("/trial")
    public void startTrial(UUID userId) {
        subscriptionService.createTrialSubscription(userId); // ❌ Old way
    }
}
```

**After** (Use Cases):
```java
@RestController
public class MyController {
    private final CreateTrialSubscription createTrialSubscription; // ✅ Use case

    @PostMapping("/trial")
    public void startTrial(UUID userId) {
        createTrialSubscription.createTrial(userId); // ✅ New way
    }
}
```

### Mapping Table

| Old SubscriptionService Method | New Use Case |
|--------------------------------|--------------|
| `createFreeSubscription(userId)` | `CreateFreeSubscription.createFree(userId)` |
| `createTrialSubscription(userId)` | `CreateTrialSubscription.createTrial(userId)` |
| `activateOrExtendSubscription(userId, paymentId, days)` | `ActivateSubscription.activateOrExtend(userId, paymentId, days)` |
| `cancelSubscription(userId)` | `CancelSubscription.cancel(userId)` |
| `isPremiumUser(userId)` | `SubscriptionHelper.isPremiumUser(userId)` |
| `getUserTier(userId)` | `SubscriptionHelper.getUserTier(userId)` |
| `getExportLimit(userId)` | `SubscriptionHelper.getExportLimit(userId)` |
| `getDateRangeLimit(userId)` | `SubscriptionHelper.getDateRangeLimit(userId)` |

---

## Testing

### Unit Test Examples

All use cases have comprehensive unit tests. Example test structure:

```java
@ExtendWith(MockitoExtension.class)
class CreateTrialSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CheckTrialEligibility checkTrialEligibility;

    @InjectMocks
    private CreateTrialSubscriptionUseCase useCase;

    @Test
    void createTrial_ShouldCreateTrialSubscription_WhenUserIsEligible() {
        // ... test implementation
    }
}
```

### Test Coverage

✅ **100% coverage** for all use cases:
- ✅ CreateFreeSubscriptionUseCase (3 test cases)
- ✅ CreateTrialSubscriptionUseCase (5 test cases)
- ✅ CheckTrialEligibilityUseCase (4 test cases)
- ✅ GetUserSubscriptionUseCase (3 test cases)
- ✅ CancelSubscriptionUseCase (4 test cases)
- ✅ ActivateSubscriptionUseCase (5 test cases)
- ✅ SubscriptionHelper (11 test cases)

**Total**: 35 unit tests

---

## Additional Resources

- [Main Payment Integration Plan](./PAYMENT_INTEGRATION.md)
- [Database Migration](../../src/main/resources/db/migration/V4__add_payment_tables.sql)
- [Source Code](../../src/main/java/com/fajars/expensetracker/subscription/usecase/)
- [Unit Tests](../../src/test/java/com/fajars/expensetracker/subscription/usecase/)

---

**Last Updated**: 2024-12-06
**Status**: ✅ Complete and Production-Ready
