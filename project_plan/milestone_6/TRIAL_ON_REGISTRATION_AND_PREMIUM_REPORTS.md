# 🎯 Trial on Registration & Premium Reports - Implementation Plan

**Milestone:** 6
**Created:** 2024-12-09
**Status:** Planning
**Priority:** High

---

## 📋 Executive Summary

**Business Goal:** Improve user activation and conversion by giving new users immediate access to premium features through a 14-day trial, while making reports a premium-only feature to drive subscription revenue.

**Key Changes:**
1. ✅ New users get 14-day PREMIUM trial automatically on registration (not FREE)
2. ✅ After trial expires → downgrade to FREE tier automatically
3. ✅ ALL report endpoints become PREMIUM-only features
4. ✅ Existing premium restrictions remain unchanged (wallets, debts, exports)

**Impact:**
- Better first-time user experience (FTUE)
- Higher feature discovery during trial
- Clearer value proposition for premium
- Stronger conversion funnel

---

## 🎪 User Journey Comparison

### ❌ Current Flow (Before)
```
Register → FREE tier → Limited features → Manually activate trial → Try premium → Subscribe
          (friction)   (confusion)       (extra step)          (14 days)
```

### ✅ New Flow (After)
```
Register → TRIAL tier → Full premium → Trial expires → FREE tier → Subscribe for reports
          (delight!)   (discovery)    (14 days)       (limited)    (conversion)
```

**Key Insight:** Users experience the BEST version of the product first, creating a "loss aversion" effect when trial expires. They know exactly what they're missing.

---

## 📊 Product Strategy Rationale

### Why Trial-First Registration?

**Psychological Principles:**
1. **Endowment Effect** - Users value what they already have more than what they might get
2. **Loss Aversion** - Taking away premium feels worse than never having it
3. **Immediate Gratification** - No barriers to experiencing full product
4. **Social Proof** - "Join thousands of premium users" (everyone starts premium)

**Business Benefits:**
- 🎯 **Higher Engagement:** Users explore all features during trial
- 📈 **Better Retention:** Users see real value before committing
- 💰 **Improved Conversion:** Clear before/after experience drives urgency
- 🚀 **Reduced Support:** No confusion about "how to try premium"

**Competitive Analysis:**
- Netflix, Spotify, Notion - all use trial-first model
- Proven to increase conversion by 20-40% vs. freemium-only

### Why Reports as Premium Feature?

**Strategic Reasoning:**
1. **High Perceived Value** - Business insights = willingness to pay
2. **Natural Paywall** - Users need reports after accumulating data
3. **Usage Trigger** - Monthly/weekly review = subscription reminder
4. **Enterprise Appeal** - Reports are essential for SMB/accounting

**Feature Tiering Philosophy:**
- **FREE = Input & Basic Tracking** (wallet, transactions, debts)
- **PREMIUM = Analysis & Export** (reports, Excel/PDF, advanced analytics)

This creates a "data in free, insights paid" model - users invest time in FREE tier, then must upgrade to extract value.

---

## 🔄 Detailed Flow Diagrams

### Registration Flow (New)

```
┌─────────────────────────────────────────────────────────────────┐
│ User registers with email/password                              │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│ AuthService.register() - @Transactional                         │
│                                                                   │
│  1. Validate email uniqueness                                    │
│  2. Create User entity                                           │
│  3. Create TRIAL subscription (14 days)  ◄── CHANGED            │
│     - tier: PREMIUM                                              │
│     - status: TRIAL                                              │
│     - endedAt: now + 14 days                                     │
│  4. Create default wallet                                        │
│  5. Generate JWT token                                           │
│  6. Log business event: USER_REGISTERED_WITH_TRIAL               │
│  7. Increment metric: user.registered.trial                      │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│ Return AuthResponse with:                                        │
│  - token                                                          │
│  - subscription: { tier: PREMIUM, status: TRIAL, endedAt: ... } │
│  - defaultWallet: { ... }                                        │
└─────────────────────────────────────────────────────────────────┘
```

### Trial Expiration Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ Scheduled Job: ProcessExpiredSubscriptionsJob                    │
│ Runs: Daily at 00:00 Asia/Jakarta                               │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│ Find subscriptions where:                                        │
│  - status = TRIAL                                                │
│  - endedAt < now()                                               │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│ For each expired trial:                                          │
│  1. Mark subscription as EXPIRED                                 │
│  2. Create new FREE subscription                                 │
│     - tier: FREE                                                 │
│     - status: ACTIVE                                             │
│     - endedAt: null (no expiry)                                  │
│  3. Log event: TRIAL_EXPIRED_TO_FREE                             │
│  4. Send email: "Your trial has ended" (optional)                │
└─────────────────────────────────────────────────────────────────┘
```

### Report Access Flow (New)

```
┌─────────────────────────────────────────────────────────────────┐
│ User calls: GET /api/v1/reports/summary                         │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│ ReportController.getFinancialSummary()                           │
│                                                                   │
│  1. Extract userId from JWT                                      │
│  2. Check premium status ◄── NEW                                │
│     if (!isPremiumUser(userId)) {                                │
│       throw BusinessException.forbidden(                         │
│         "Reports are premium-only. Upgrade to access insights"   │
│       );                                                          │
│     }                                                             │
│  3. Generate report (if premium)                                 │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│ Return:                                                           │
│  - Premium user: 200 OK with report data                         │
│  - FREE user: 403 Forbidden with upgrade message                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🛠 Implementation Details

### 1. Modify Registration Flow

**File:** `src/main/java/com/fajars/expensetracker/auth/AuthService.java`

**Current Code (line 88):**
```java
// Step 3: Create FREE subscription
Subscription subscription = createFreeSubscription.createFree(user.getId());
```

**New Code:**
```java
// Step 3: Create TRIAL subscription (14 days premium)
Subscription subscription = createTrialSubscription.createTrial(user.getId());
log.info("User {} registered with 14-day TRIAL subscription", user.getId());
```

**Dependency Injection Required:**
```java
private final CreateTrialSubscription createTrialSubscription;

public AuthService(
    // ... existing params
    CreateTrialSubscription createTrialSubscription
) {
    this.createTrialSubscription = createTrialSubscription;
}
```

**Business Event Change:**
```java
// Line 100: Update event name
businessEventLogger.logUserRegistration(user.getEmail(), ipAddress, "TRIAL");
metricsService.incrementCounter("user.registered.trial"); // Changed from "with_subscription"
```

---

### 2. Modify Trial Eligibility Check

**File:** `src/main/java/com/fajars/expensetracker/subscription/usecase/CheckTrialEligibilityUseCase.java`

**Current Logic (line 41-48):**
```java
// Check if user has already used trial (via payment OR manual activation)
boolean hasSuccessfulPayment = paymentRepository.hasSuccessfulPayment(userId);
boolean hasHadPremium = subscriptionRepository.hasHadPremiumSubscription(userId);

if (hasSuccessfulPayment || hasHadPremium) {
    throw BusinessException.forbidden("User has already used trial period");
}
```

**New Logic:**
```java
// Check if user has EVER had a trial (including registration trial)
// Users can only trial once in lifetime - even at registration
boolean hasEverHadTrial = subscriptionRepository.hasHadTrialSubscription(userId);

if (hasEverHadTrial) {
    throw BusinessException.forbidden(
        "Trial already used. You had 14 days of premium access when you registered."
    );
}
```

**New Repository Method Required:**
```java
// In SubscriptionRepository.java
@Query("""
    SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
    FROM Subscription s
    WHERE s.user.id = :userId
    AND s.status = 'TRIAL'
    """)
boolean hasHadTrialSubscription(@Param("userId") UUID userId);
```

**Rationale:** Prevent users from getting trial twice (registration trial + manual trial activation).

---

### 3. Make Reports Premium-Only

**File:** `src/main/java/com/fajars/expensetracker/report/ReportController.java`

**Changes Required in 3 Endpoints:**

#### 3.1 Financial Summary (line 58-111)

**Remove (lines 74, 88-93):**
```java
// DELETE: checkReportFrequencyLimit(userId);
// DELETE: Date range validation based on tier
```

**Add (after line 71):**
```java
// Premium-only feature
if (!subscriptionHelper.isPremiumUser(userId)) {
    throw BusinessException.forbidden(
        "Financial reports are available for PREMIUM users only. " +
        "Upgrade your subscription to access detailed insights and analytics."
    );
}
```

#### 3.2 Trend Data (line 122-174)

**Remove (lines 138, 152-157):**
```java
// DELETE: checkReportFrequencyLimit(userId);
// DELETE: Date range validation based on tier
```

**Add (after line 135):**
```java
// Premium-only feature
if (!subscriptionHelper.isPremiumUser(userId)) {
    throw BusinessException.forbidden(
        "Trend analytics are available for PREMIUM users only. " +
        "Upgrade to visualize your financial patterns over time."
    );
}
```

#### 3.3 Category Breakdown (line 186-248)

**Remove (lines 206, 220-225):**
```java
// DELETE: checkReportFrequencyLimit(userId);
// DELETE: Date range validation based on tier
```

**Add (after line 203):**
```java
// Premium-only feature
if (!subscriptionHelper.isPremiumUser(userId)) {
    throw BusinessException.forbidden(
        "Category analysis is available for PREMIUM users only. " +
        "Upgrade to understand your spending patterns by category."
    );
}
```

**Remove Helper Method (lines 257-272):**
```java
// DELETE: private void checkReportFrequencyLimit(UUID userId)
// No longer needed - reports are premium-only, no frequency limit
```

---

### 4. Clean Up Unused Components

#### 4.1 ReportFrequencyLimiter (OPTIONAL - Keep for Future)

**File:** `src/main/java/com/fajars/expensetracker/common/ratelimit/ReportFrequencyLimiter.java`

**Decision:** **Keep but deprecate** - May be useful for future per-premium-tier limiting

**Add Deprecation:**
```java
/**
 * @deprecated Reports are now premium-only. This limiter is no longer used.
 *             Kept for potential future use (e.g., limiting premium tier users).
 */
@Deprecated(since = "Milestone 6", forRemoval = false)
@Component
public class ReportFrequencyLimiter {
    // ... existing code
}
```

#### 4.2 DateRangeValidator (Keep - Still Used)

**File:** `src/main/java/com/fajars/expensetracker/common/validation/DateRangeValidator.java`

**Status:** **Keep unchanged** - Still validates export date ranges for premium users

**Reason:** Export endpoints still use tier-based validation:
- FREE: 90-day range, CSV only
- PREMIUM: 365-day range, CSV/Excel/PDF

---

### 5. Update Frontend Error Handling

**Recommendation for Frontend Team:**

```typescript
// When calling report APIs
try {
  const response = await api.get('/reports/summary');
  // Handle success
} catch (error) {
  if (error.status === 403) {
    // Show upgrade modal
    showUpgradeModal({
      title: 'Premium Feature',
      message: error.message, // Backend provides friendly message
      features: [
        'Financial summary reports',
        'Trend analytics & charts',
        'Category breakdown insights',
        'Unlimited export formats'
      ],
      cta: 'Upgrade to Premium'
    });
  }
}
```

---

## 📦 Database Changes

### No Schema Changes Required

All required tables and columns already exist:
- ✅ `subscriptions.status` already supports `TRIAL` enum
- ✅ `subscriptions.plan` already has `PREMIUM` tier
- ✅ `subscriptions.ended_at` already tracks expiration

### New Repository Query

**File:** `src/main/java/com/fajars/expensetracker/subscription/SubscriptionRepository.java`

```java
/**
 * Check if user has ever had a trial subscription (including registration trial).
 * Used to enforce one-time trial policy.
 */
@Query("""
    SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
    FROM Subscription s
    WHERE s.user.id = :userId
    AND s.status = 'TRIAL'
    """)
boolean hasHadTrialSubscription(@Param("userId") UUID userId);
```

---

## 🧪 Testing Strategy

### Unit Tests

#### AuthServiceTest - Trial Registration
```java
@Test
void register_ShouldCreateTrialSubscription() {
    // Arrange
    RegisterRequest request = new RegisterRequest("test@example.com", "pass", "Test");

    // Act
    AuthResponse response = authService.register(request);

    // Assert
    assertThat(response.subscription().tier()).isEqualTo(SubscriptionTier.PREMIUM);
    assertThat(response.subscription().status()).isEqualTo(SubscriptionStatus.TRIAL);

    // Verify trial duration = 14 days
    Subscription sub = subscriptionRepository.findActiveSubscriptionByUserId(response.userId());
    assertThat(sub.getEndedAt()).isEqualToIgnoringNanos(
        LocalDateTime.now().plusDays(14)
    );
}

@Test
void register_ShouldRollbackIfTrialCreationFails() {
    // Simulate trial creation failure
    when(createTrialSubscription.createTrial(any()))
        .thenThrow(new RuntimeException("DB error"));

    // Act & Assert
    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(RuntimeException.class);

    // Verify user was NOT created (transaction rolled back)
    assertThat(userRepository.findByEmail("test@example.com")).isEmpty();
}
```

#### ReportControllerTest - Premium Access
```java
@Test
void getFinancialSummary_FreeUser_ShouldReturn403() {
    // Arrange
    UUID freeUserId = createFreeUser();
    when(subscriptionHelper.isPremiumUser(freeUserId)).thenReturn(false);

    // Act & Assert
    mockMvc.perform(get("/api/v1/reports/summary")
            .header("Authorization", "Bearer " + generateToken(freeUserId)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(containsString("PREMIUM users only")));
}

@Test
void getFinancialSummary_TrialUser_ShouldReturn200() {
    // Arrange
    UUID trialUserId = createTrialUser();
    when(subscriptionHelper.isPremiumUser(trialUserId)).thenReturn(true);

    // Act & Assert
    mockMvc.perform(get("/api/v1/reports/summary")
            .header("Authorization", "Bearer " + generateToken(trialUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalIncome").exists());
}
```

#### CheckTrialEligibilityTest
```java
@Test
void checkEligibility_UserRegisteredWithTrial_ShouldBeIneligible() {
    // Arrange - user registered 5 days ago with trial
    UUID userId = createUserWithRegistrationTrial(5);

    // Act & Assert
    assertThatThrownBy(() -> checkTrialEligibility.check(userId))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Trial already used");
}
```

### Integration Tests

#### Full Registration Flow
```java
@Test
void fullRegistrationFlow_ShouldCreateTrialAndWallet() {
    // Act
    ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "newuser@test.com",
              "password": "SecurePass123!",
              "name": "New User"
            }
            """));

    // Assert
    result.andExpect(status().isOk())
        .andExpect(jsonPath("$.subscription.tier").value("PREMIUM"))
        .andExpect(jsonPath("$.subscription.status").value("TRIAL"));

    // Verify database state
    User user = userRepository.findByEmail("newuser@test.com").orElseThrow();
    Subscription sub = subscriptionRepository.findActiveSubscriptionByUserId(user.getId()).orElseThrow();

    assertThat(sub.getPlan()).isEqualTo(SubscriptionTier.PREMIUM);
    assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.TRIAL);
    assertThat(sub.isTrial()).isTrue();

    // Verify trial period
    long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDateTime.now(), sub.getEndedAt());
    assertThat(daysUntilExpiry).isEqualTo(14);
}
```

#### Trial Expiration Job
```java
@Test
void processExpiredTrials_ShouldDowngradeToFree() {
    // Arrange - create trial subscription that expired yesterday
    UUID userId = createTrialUser();
    Subscription trial = subscriptionRepository.findActiveSubscriptionByUserId(userId).orElseThrow();
    trial.setEndedAt(LocalDateTime.now().minusDays(1));
    subscriptionRepository.save(trial);

    // Act
    subscriptionService.processExpiredSubscriptions();

    // Assert
    Subscription current = subscriptionRepository.findActiveSubscriptionByUserId(userId).orElseThrow();
    assertThat(current.getPlan()).isEqualTo(SubscriptionTier.FREE);
    assertThat(current.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    assertThat(current.getEndedAt()).isNull(); // FREE never expires
}
```

### Manual Testing Checklist

- [ ] Register new user → verify TRIAL subscription in response
- [ ] Trial user can access all report endpoints
- [ ] Trial user can export Excel/PDF
- [ ] Trial user has unlimited wallets/debts
- [ ] After 14 days → scheduled job downgrades to FREE
- [ ] FREE user gets 403 on report endpoints
- [ ] FREE user still has all transaction data (read-only reports)
- [ ] FREE user can still CRUD transactions/debts/wallets
- [ ] Trial user cannot activate trial again (already used at registration)
- [ ] Frontend shows upgrade CTA when accessing premium features

---

## 📋 Deployment Checklist

### Pre-Deployment

- [ ] Update API documentation (Swagger) with new premium restrictions
- [ ] Update frontend to handle 403 errors on report endpoints
- [ ] Update onboarding UI to highlight "You're on trial!" messaging
- [ ] Configure email template for trial expiration notification
- [ ] Test scheduled job in staging environment
- [ ] Update pricing page to mention trial benefits

### Deployment Steps

1. **Backend Deployment**
   ```bash
   # Compile and test
   ./gradlew clean build
   ./gradlew test

   # Deploy new version
   docker build -t expense-tracker:trial-v1 .
   docker-compose up -d backend
   ```

2. **Verify Deployment**
   ```bash
   # Health check
   curl http://localhost:8081/actuator/health

   # Test registration returns trial
   curl -X POST http://localhost:8081/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"Test123!","name":"Test"}'

   # Expected: subscription.tier = "PREMIUM", status = "TRIAL"
   ```

3. **Frontend Deployment**
   - Deploy updated frontend with upgrade modal
   - Test report access with FREE and PREMIUM users
   - Verify trial badge appears for new users

### Post-Deployment Monitoring

**Key Metrics to Track:**
- `user.registered.trial` - Registration rate with trial
- `subscription.trial_started` - Should match registration count
- `subscription.trial_expired` - Track expiration rate
- `api.reports.forbidden` - FREE user attempts (conversion signal)
- `subscription.activated` - Trial → Paid conversion rate

**Alerts to Set:**
- 🚨 Trial creation failure rate > 1%
- 🚨 Scheduled job not running for 25 hours
- 🚨 Report endpoint errors > 5%

---

## 💡 Product Owner Recommendations

### A/B Testing Opportunities

**Test Hypothesis:** Does trial duration affect conversion?

**Variants:**
- Control: 14 days (current)
- Variant A: 7 days (shorter, more urgent)
- Variant B: 30 days (longer, more habit-forming)

**Measure:** Trial → Paid conversion rate at day 30

### Future Enhancements

#### 1. Trial Reminder Emails
```
Day 1: "Welcome! Here's how to get started" (onboarding)
Day 7: "You're halfway through your trial" (engagement)
Day 12: "2 days left - don't lose your insights!" (urgency)
Day 14: "Your trial has ended" (conversion)
Day 16: "Miss your reports? Upgrade now" (win-back)
```

#### 2. In-App Trial Counter
Show remaining days in dashboard:
```
┌─────────────────────────────────────┐
│ ⏰ 8 days left in your PREMIUM trial│
│ Upgrade now to keep unlimited access │
│ [View Plans]                         │
└─────────────────────────────────────┘
```

#### 3. Feature Usage Analytics
Track which premium features trial users engage with:
- Report generation frequency
- Export format preferences (Excel vs PDF)
- Multiple wallet usage
- Debt tracking adoption

Use data to personalize upgrade messaging:
- Heavy report users → "Keep your insights with Premium"
- Multi-wallet users → "Manage all your wallets with Premium"

#### 4. Trial Extension for High-Value Users
Automatically extend trial for users who:
- Generated > 50 transactions
- Used all premium features
- Invited friends (referral program)

#### 5. Freemium Comparison Table
Show feature comparison DURING trial:

| Feature | You (Trial) | After Trial | Premium |
|---------|-------------|-------------|---------|
| Reports | ✅ Unlimited | ❌ Blocked | ✅ Unlimited |
| Wallets | ✅ Unlimited | ✅ 1 only | ✅ Unlimited |
| Exports | ✅ Excel/PDF | ✅ CSV only | ✅ All formats |

### Pricing Strategy Recommendations

**Current Premium Features:**
- Unlimited wallets
- Unlimited debts
- Excel/PDF export
- **Reports (NEW)**

**Suggested Pricing Tiers:**

#### Tier 1: FREE (Post-Trial)
- 1 wallet
- Unlimited transactions
- 10 active debts
- CSV export (100 records max)
- ❌ No reports

#### Tier 2: PREMIUM (Monthly)
Price: IDR 25,000/month (~$1.60 USD)
- Everything in FREE
- Unlimited wallets
- Unlimited debts
- Excel/PDF export (10,000 records)
- ✅ All reports & analytics
- Priority support

#### Tier 3: BUSINESS (Annual)
Price: IDR 250,000/year (~$16 USD, 2 months free)
- Everything in PREMIUM
- Multi-user access (up to 3 users)
- Advanced analytics
- API access
- Dedicated support

**Positioning:**
- FREE = Individual casual users
- PREMIUM = Active users & small businesses
- BUSINESS = Growing businesses & accountants

### User Communication Plan

#### Trial Start (Registration Email)
```
Subject: Welcome! Your 14-day PREMIUM trial has started 🎉

Hi [Name],

Thanks for joining Expense Tracker! You now have full PREMIUM access for 14 days.

Here's what you can do:
✓ Create unlimited wallets for different accounts
✓ Track all your debts and receivables
✓ Generate detailed financial reports
✓ Export to Excel/PDF for accounting

Get started: [Dashboard Link]

Your trial ends on [Date]. Upgrade anytime to keep these features.

Happy tracking!
```

#### Trial Expiration Warning (Day 12)
```
Subject: ⏰ 2 days left in your PREMIUM trial

Hi [Name],

Your trial ends in 2 days (on [Date]).

You've generated [X] reports and managed [Y] wallets during your trial.

Don't lose access to:
• Financial reports and insights
• Unlimited wallets and debts
• Excel/PDF export

Upgrade now: [Pricing Page Link]

Questions? Reply to this email.
```

#### Trial Expired (Day 14 + 1 hour)
```
Subject: Your trial has ended - Here's what's next

Hi [Name],

Your 14-day PREMIUM trial ended today.

You still have access to:
✓ All your transactions and data
✓ Create/edit transactions
✓ Basic wallet management
✓ CSV export (up to 100 records)

What you'll miss:
✗ Financial reports and analytics ← Most popular feature
✗ Unlimited wallets
✗ Excel/PDF export

Upgrade to PREMIUM for just IDR 25,000/month: [Upgrade Link]

30-day money-back guarantee. Cancel anytime.
```

---

## 🎯 Success Metrics & KPIs

### Trial Activation
- **Target:** 100% of new registrations start with trial
- **Measure:** `user.registered.trial / total_registrations`

### Trial Engagement
- **Target:** >80% of trial users generate at least 1 report
- **Measure:** `users_who_generated_report / total_trial_users`

### Trial-to-Paid Conversion
- **Target:** >15% conversion within 30 days post-trial
- **Measure:** `paid_subscriptions / expired_trials` (30-day window)
- **Benchmark:** Industry standard = 10-20%

### Feature Discovery
- **Target:** >60% of trial users try multiple premium features
- **Measure:** Track usage of reports, multiple wallets, Excel/PDF export

### Retention After Downgrade
- **Target:** >50% of downgraded users remain active (1 transaction/week)
- **Measure:** `active_free_users / total_downgraded_users`

---

## 📂 Files to Modify

### Core Implementation (8 files)

| File | Lines | Changes | Complexity |
|------|-------|---------|------------|
| `AuthService.java` | ~10 | Inject `CreateTrialSubscription`, change subscription creation, update metrics | Low |
| `CheckTrialEligibilityUseCase.java` | ~15 | Update eligibility logic to check for any past trial | Low |
| `SubscriptionRepository.java` | ~8 | Add `hasHadTrialSubscription()` query | Low |
| `ReportController.java` | ~30 | Add premium checks, remove frequency limits, remove date validation | Medium |
| `ReportFrequencyLimiter.java` | ~5 | Add `@Deprecated` annotation | Trivial |

### Testing (6 new test files)

| Test File | Purpose |
|-----------|---------|
| `AuthServiceTest.java` | Verify trial creation on registration |
| `ReportControllerTest.java` | Verify premium-only access |
| `CheckTrialEligibilityTest.java` | Verify one-time trial enforcement |
| `AuthControllerIntegrationTest.java` | End-to-end registration flow |
| `SubscriptionExpirationJobTest.java` | Verify trial expiration process |
| `ReportAccessIntegrationTest.java` | Verify report restrictions |

---

## ⚠️ Risks & Mitigation

### Risk 1: Users Feel "Tricked" (Bait & Switch)

**Scenario:** Users love trial, get downgraded, feel cheated

**Mitigation:**
- ✅ Clear communication: "14-day trial" in registration
- ✅ Trial counter in dashboard (days remaining)
- ✅ Email reminders (day 7, day 12, day 14)
- ✅ Soft downgrade: keep all data, just block reports

### Risk 2: Lower Initial Engagement

**Scenario:** Users skip onboarding, assuming they have 14 days

**Mitigation:**
- ✅ Onboarding email with "quick start" guide
- ✅ In-app tutorial on first login
- ✅ "Complete your profile" checklist

### Risk 3: Trial Expiration Job Fails

**Scenario:** Job doesn't run, trials continue indefinitely

**Mitigation:**
- ✅ Monitor job execution (alert if skipped)
- ✅ Add retry logic with exponential backoff
- ✅ Manual admin tool to process missed expirations
- ✅ Backup cron job on separate server

### Risk 4: Revenue Impact (Short-Term)

**Scenario:** Users who would pay immediately now wait 14 days

**Mitigation:**
- ✅ Track trial-to-paid time (optimize trial length)
- ✅ Allow immediate upgrade during trial (skip to annual plan)
- ✅ A/B test trial duration (7 vs 14 vs 30 days)

---

## 🚀 Implementation Timeline

### Phase 1: Core Changes (Week 1)
- Day 1-2: Modify registration flow
- Day 3: Update report endpoints
- Day 4: Update trial eligibility checks
- Day 5: Write unit tests

### Phase 2: Testing & Integration (Week 2)
- Day 1-2: Integration tests
- Day 3: Manual QA testing
- Day 4: Frontend integration
- Day 5: Staging deployment

### Phase 3: Production Rollout (Week 3)
- Day 1: Production deployment
- Day 2-5: Monitor metrics
- Day 5: Gather user feedback

### Phase 4: Optimization (Ongoing)
- Week 4+: A/B testing trial duration
- Week 6+: Implement email sequences
- Week 8+: Build trial analytics dashboard

---

## 📞 Stakeholder Communication

### For Engineering Team
"We're simplifying user onboarding by automatically starting trials at registration. This removes friction and increases feature discovery. Backend changes are minimal - just swap FREE for TRIAL subscription creation."

### For Product/Marketing Team
"We're implementing a trial-first model proven to increase conversion by 20-40%. Users experience the best version first, creating urgency to upgrade when trial expires. Reports become our primary conversion driver."

### For Support Team
"New users automatically get 14-day trials. After expiration, they keep their data but lose report access. Direct them to upgrade page for premium. FAQs updated with trial policies."

### For Users (Public Messaging)
"Great news! All new users now get 14 days of PREMIUM access - free. Experience unlimited wallets, advanced reports, and Excel/PDF export. No credit card required."

---

## ✅ Definition of Done

- [ ] Registration creates TRIAL subscription (not FREE)
- [ ] Trial duration is exactly 14 days
- [ ] All report endpoints require PREMIUM tier
- [ ] Trial users can access all reports
- [ ] FREE users get 403 error with upgrade message
- [ ] Trial eligibility prevents second trial
- [ ] Expiration job downgrades TRIAL → FREE
- [ ] All existing premium features unchanged
- [ ] Unit tests pass (>90% coverage)
- [ ] Integration tests pass
- [ ] Frontend handles 403 errors gracefully
- [ ] Metrics tracking trial conversion
- [ ] Documentation updated
- [ ] Staging tested successfully
- [ ] Production deployment successful
- [ ] Zero downtime during deployment
- [ ] Rollback plan tested

---

## 📖 Additional Resources

### Internal Documentation
- [Subscription Architecture](../milestone_5/SUBSCRIPTION_ARCHITECTURE.md)
- [Premium Feature Impact Analysis](../milestone_5/PREMIUM_FEATURE_IMPACT_ANALYSIS.md)
- [Midtrans Integration Guide](../../documentations/MIDTRANS_INTEGRATION_GUIDE.md)

### External References
- [Trial vs Freemium: Which Converts Better?](https://www.profitwell.com/recur/all/freemium-vs-free-trial)
- [Optimizing Trial Length for SaaS](https://www.priceintelligently.com/blog/saas-free-trial-length)
- [Trial Expiration Best Practices](https://www.intercom.com/blog/free-trial-best-practices/)

---

**Last Updated:** 2024-12-09
**Next Review:** After implementation (2024-12-23)
**Owner:** Product Team
**Status:** Ready for Implementation ✅
