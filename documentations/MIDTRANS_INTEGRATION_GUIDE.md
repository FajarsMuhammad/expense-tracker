# 🚀 Panduan Integrasi Midtrans Payment Gateway

## 📋 Daftar Isi
1. [Persiapan Account Midtrans](#1-persiapan-account-midtrans)
2. [Konfigurasi Backend](#2-konfigurasi-backend)
3. [Testing dengan Sandbox](#3-testing-dengan-sandbox)
4. [API Endpoints](#4-api-endpoints)
5. [Payment Flow](#5-payment-flow)
6. [Webhook Setup](#6-webhook-setup)
7. [Testing Checklist](#7-testing-checklist)
8. [Production Deployment](#8-production-deployment)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. Persiapan Account Midtrans

### Step 1.1: Daftar di Midtrans
1. Kunjungi https://dashboard.midtrans.com/register
2. Daftar dengan email bisnis Anda
3. Verifikasi email
4. Login ke dashboard

### Step 1.2: Dapatkan API Keys (Sandbox)
1. Login ke https://dashboard.sandbox.midtrans.com/
2. Klik **Settings** → **Access Keys**
3. Copy credentials:
   - **Server Key**: `SB-Mid-server-xxxxxxxxxxxxxxx` (RAHASIA!)
   - **Client Key**: `SB-Mid-client-xxxxxxxxxxxxxxx` (Public)

**⚠️ PENTING:**
- Server Key = Backend only, JANGAN expose ke frontend
- Client Key = Frontend safe, boleh di-expose

---

## 2. Konfigurasi Backend

### Step 2.1: Set Environment Variables

Buat file `.env` atau set di system:

```bash
# Midtrans Sandbox Configuration
MIDTRANS_SERVER_KEY=SB-Mid-server-xxxxxxxxxxxxxxx
MIDTRANS_CLIENT_KEY=SB-Mid-client-xxxxxxxxxxxxxxx
MIDTRANS_API_URL=https://app.sandbox.midtrans.com
MIDTRANS_IS_PRODUCTION=false
```

### Step 2.2: Verifikasi Konfigurasi di application.yaml

File sudah ada di: `src/main/resources/application.yaml`

```yaml
midtrans:
  server-key: ${MIDTRANS_SERVER_KEY:SB-Mid-server-YOUR_SANDBOX_SERVER_KEY}
  client-key: ${MIDTRANS_CLIENT_KEY:SB-Mid-client-YOUR_SANDBOX_CLIENT_KEY}
  api-url: ${MIDTRANS_API_URL:https://app.sandbox.midtrans.com}
  is-production: ${MIDTRANS_IS_PRODUCTION:false}
```

### Step 2.3: Start Application

```bash
# Export environment variables (Linux/Mac)
export MIDTRANS_SERVER_KEY="SB-Mid-server-xxxxxxxxxxxxxxx"
export MIDTRANS_CLIENT_KEY="SB-Mid-client-xxxxxxxxxxxxxxx"

# Or dengan inline (Linux/Mac)
MIDTRANS_SERVER_KEY="SB-Mid-server-xxx" \
MIDTRANS_CLIENT_KEY="SB-Mid-client-xxx" \
./gradlew bootRun

# Windows PowerShell
$env:MIDTRANS_SERVER_KEY="SB-Mid-server-xxx"
$env:MIDTRANS_CLIENT_KEY="SB-Mid-client-xxx"
./gradlew bootRun
```

---

## 3. Testing dengan Sandbox

### Step 3.1: Register User Baru

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "Test123!",
    "name": "Test User"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGc...",
  "userId": "uuid",
  "email": "testuser@example.com",
  "name": "Test User",
  "subscription": {
    "id": "uuid",
    "tier": "FREE",
    "status": "ACTIVE"
  },
  "defaultWallet": {
    "id": "uuid",
    "name": "My Wallet",
    "currency": "IDR",
    "initialBalance": 0.0
  }
}
```

**✅ Simpan JWT token untuk request selanjutnya!**

### Step 3.2: Create Payment (Upgrade ke Premium)

```bash
curl -X POST http://localhost:8081/api/v1/payments/subscription \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Idempotency-Key: unique-key-123"
```

**Response:**
```json
{
  "paymentId": "uuid",
  "orderId": "ORDER-userid-timestamp",
  "amount": 25000.00,
  "currency": "IDR",
  "status": "PENDING",
  "snapToken": "abc123...",
  "snapRedirectUrl": "https://app.sandbox.midtrans.com/snap/v3/redirection/abc123..."
}
```

### Step 3.3: Buka Payment Page

1. **Option A - Browser:** Copy `snapRedirectUrl` dan buka di browser
2. **Option B - Frontend:** Gunakan Snap.js dengan `snapToken`

**Snap.js Integration (Frontend):**
```html
<!-- Include Snap.js -->
<script src="https://app.sandbox.midtrans.com/snap/snap.js"
        data-client-key="YOUR_CLIENT_KEY"></script>

<script>
// Trigger payment
snap.pay('SNAP_TOKEN_FROM_BACKEND', {
  onSuccess: function(result) {
    console.log('Payment success:', result);
    // Redirect ke success page
  },
  onPending: function(result) {
    console.log('Payment pending:', result);
  },
  onError: function(result) {
    console.log('Payment error:', result);
  },
  onClose: function() {
    console.log('Payment popup closed');
  }
});
</script>
```

### Step 3.4: Test Payment dengan Test Cards

Midtrans Sandbox menyediakan test cards:

| Card Number | Status | CVV | Expiry |
|-------------|--------|-----|--------|
| 4811 1111 1111 1114 | Success | 123 | 01/25 |
| 4911 1111 1111 1113 | Challenge (3DS) | 123 | 01/25 |
| 4411 1111 1111 1118 | Denied | 123 | 01/25 |

**Test dengan Success Card:**
1. Pilih payment method: **Credit Card**
2. Masukkan: `4811 1111 1111 1114`
3. CVV: `123`
4. Expiry: `01/25`
5. Klik **Pay**

---

## 4. API Endpoints

### 4.1 Create Payment
**Endpoint:** `POST /api/v1/payments/subscription`

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
X-Idempotency-Key: {UNIQUE_KEY} (optional)
```

**Response:**
```json
{
  "paymentId": "uuid",
  "orderId": "ORDER-123...",
  "amount": 25000.00,
  "currency": "IDR",
  "status": "PENDING",
  "snapToken": "...",
  "snapRedirectUrl": "https://..."
}
```

### 4.2 Webhook (Midtrans → Backend)
**Endpoint:** `POST /api/v1/payments/webhook/midtrans`

**No Auth Required** (Public endpoint for Midtrans)

**Body (from Midtrans):**
```json
{
  "transaction_status": "settlement",
  "order_id": "ORDER-123...",
  "gross_amount": "25000.00",
  "signature_key": "...",
  "payment_type": "credit_card",
  "transaction_id": "..."
}
```

**What happens:**
- Verifies signature (SHA-512)
- Updates payment status
- Activates PREMIUM subscription (30 days)
- Logs business event

---

## 5. Payment Flow

```
┌─────────────┐
│   User      │
│  (Frontend) │
└──────┬──────┘
       │
       │ 1. POST /payments/subscription
       │    Headers: Authorization: Bearer {token}
       │
       ▼
┌─────────────────┐
│   Backend       │
│ PaymentController│
└──────┬──────────┘
       │
       │ 2. CreateSubscriptionPaymentUseCase
       │    - Create PaymentTransaction (PENDING)
       │    - Build Midtrans Snap Request
       │
       ▼
┌─────────────────┐
│ MidtransClient  │
│ (WebClient)     │
└──────┬──────────┘
       │
       │ 3. POST /snap/v1/transactions
       │    Auth: Basic {base64(serverKey:)}
       │    Body: {transaction_details, customer_details, ...}
       │
       ▼
┌─────────────────┐
│   Midtrans      │
│   Snap API      │
└──────┬──────────┘
       │
       │ 4. Response: {token, redirect_url}
       │
       ▼
┌─────────────────┐
│   Backend       │
│ Save: snapToken │
│       snapUrl   │
└──────┬──────────┘
       │
       │ 5. Return to Frontend
       ▼
┌─────────────────┐
│   Frontend      │
│ Open Snap.js    │
│ or redirect_url │
└──────┬──────────┘
       │
       │ 6. User pays via Snap UI
       │    (Credit Card, E-Wallet, Bank Transfer, etc.)
       │
       ▼
┌─────────────────┐
│   Midtrans      │
│ Process Payment │
└──────┬──────────┘
       │
       │ 7. Webhook: POST /webhook/midtrans
       │    Body: {transaction_status, order_id, ...}
       │
       ▼
┌─────────────────┐
│   Backend       │
│ ProcessPayment  │
│ WebhookUseCase  │
└──────┬──────────┘
       │
       │ 8a. Verify signature (SHA-512)
       │ 8b. Update payment status
       │ 8c. Activate PREMIUM subscription
       │ 8d. Log events & metrics
       │
       ▼
┌─────────────────┐
│   Database      │
│ - payment_transactions (COMPLETED)
│ - subscriptions (PREMIUM, ACTIVE, 30 days)
└─────────────────┘
```

---

## 6. Webhook Setup

### Step 6.1: Configure di Midtrans Dashboard

1. Login ke https://dashboard.sandbox.midtrans.com/
2. Klik **Settings** → **Configuration**
3. Set **Payment Notification URL**:
   ```
   https://your-domain.com/api/v1/payments/webhook/midtrans
   ```

4. **Development/Local Testing:**
   - Use **ngrok** untuk expose localhost:
   ```bash
   ngrok http 8081
   # Copy URL: https://abc123.ngrok.io
   # Set webhook: https://abc123.ngrok.io/api/v1/payments/webhook/midtrans
   ```

### Step 6.2: Webhook Security

Backend automatically verifies:
1. **Signature verification** (SHA-512)
   ```java
   String expectedSignature = sha512(orderId + statusCode + grossAmount + serverKey);
   if (!expectedSignature.equals(payload.signatureKey())) {
       throw BusinessException.forbidden("Invalid signature");
   }
   ```

2. **Idempotency** - Prevents duplicate processing

---

## 7. Testing Checklist

### ✅ Sandbox Testing

- [ ] **User Registration**
  - [ ] Register user baru
  - [ ] Dapat FREE subscription + default wallet
  - [ ] JWT token valid

- [ ] **Create Payment**
  - [ ] POST /payments/subscription dengan valid JWT
  - [ ] Dapat snapToken dan snapRedirectUrl
  - [ ] Payment status = PENDING di database
  - [ ] Idempotency key prevents duplicate

- [ ] **Payment Success Flow**
  - [ ] Buka Snap UI (via redirect_url)
  - [ ] Bayar dengan test card: `4811 1111 1111 1114`
  - [ ] Webhook dipanggil oleh Midtrans
  - [ ] Payment status → COMPLETED
  - [ ] Subscription → PREMIUM (30 days)
  - [ ] User dapat unlimited features

- [ ] **Payment Failed Flow**
  - [ ] Test dengan denied card: `4411 1111 1111 1118`
  - [ ] Payment status → FAILED
  - [ ] Subscription tetap FREE

- [ ] **Premium Features Active**
  - [ ] Create unlimited wallets (> 1)
  - [ ] Create unlimited debts (> 10)
  - [ ] Generate unlimited reports (> 10/day)
  - [ ] Export Excel & PDF (not only CSV)

### ✅ Edge Cases

- [ ] Duplicate payment request (same idempotency key)
- [ ] Webhook dipanggil multiple times (idempotency check)
- [ ] Invalid signature di webhook
- [ ] User sudah PREMIUM coba bayar lagi
- [ ] Payment expired (24 hours)

---

## 8. Production Deployment

### Step 8.1: Switch ke Production Keys

1. Login ke https://dashboard.midtrans.com/ (PRODUCTION)
2. Upload business documents (KTP, NPWP, etc.)
3. Wait for approval (~1-3 business days)
4. Get production keys dari **Settings** → **Access Keys**

### Step 8.2: Update Environment Variables

```bash
# Production
MIDTRANS_SERVER_KEY=Mid-server-PRODUCTION_KEY
MIDTRANS_CLIENT_KEY=Mid-client-PRODUCTION_KEY
MIDTRANS_API_URL=https://app.midtrans.com
MIDTRANS_IS_PRODUCTION=true
```

### Step 8.3: Webhook Configuration

Set production webhook URL:
```
https://api.yourdomain.com/api/v1/payments/webhook/midtrans
```

**⚠️ PENTING:**
- Use HTTPS (SSL certificate required)
- Whitelist Midtrans IPs if using firewall
- Monitor webhook logs

### Step 8.4: Security Checklist

- [ ] Server Key tidak di-expose
- [ ] HTTPS enabled
- [ ] Signature verification active
- [ ] Rate limiting enabled
- [ ] Monitoring & alerting setup

---

## 9. Troubleshooting

### ❌ Error: "Midtrans server key is required"

**Solution:**
```bash
# Check environment variable
echo $MIDTRANS_SERVER_KEY

# Or set explicitly
export MIDTRANS_SERVER_KEY="SB-Mid-server-xxx"
./gradlew bootRun
```

### ❌ Error: "401 Unauthorized" from Midtrans

**Penyebab:**
- Server Key salah
- Pakai production key di sandbox URL (atau sebaliknya)

**Solution:**
1. Verify server key di dashboard
2. Pastikan URL match dengan environment (sandbox/production)

### ❌ Webhook tidak dipanggil

**Troubleshooting:**
1. **Check webhook URL** di Midtrans dashboard
2. **Local development:** Pakai ngrok
   ```bash
   ngrok http 8081
   # Update webhook URL di dashboard dengan ngrok URL
   ```
3. **Check firewall:** Pastikan port 8081 accessible
4. **Check logs:**
   ```bash
   tail -f logs/application.log | grep "webhook"
   ```

### ❌ Payment status tidak update

**Check:**
1. Webhook dipanggil? (check logs)
2. Signature valid? (check WebhookVerifier logs)
3. Database update berhasil? (check payment_transactions table)

**Debug query:**
```sql
SELECT * FROM payment_transactions
WHERE order_id = 'ORDER-xxx'
ORDER BY created_at DESC;

SELECT * FROM subscriptions
WHERE user_id = 'user-uuid'
ORDER BY started_at DESC;
```

---

## 📞 Support & Resources

### Official Midtrans Documentation
- Snap API: https://docs.midtrans.com/reference/snap-api
- Webhook: https://docs.midtrans.com/reference/notification-webhooks
- Test Cards: https://docs.midtrans.com/docs/testing-payment

### Internal Code References
- **PaymentController**: `src/main/java/.../payment/PaymentController.java`
- **MidtransClient**: `src/main/java/.../payment/midtrans/MidtransClient.java`
- **Webhook Handler**: `src/main/java/.../payment/usecase/ProcessPaymentWebhookUseCase.java`

### Contact
- Midtrans Support: support@midtrans.com
- Technical Issues: developer@midtrans.com

---

## ✨ Quick Start Summary

```bash
# 1. Get Midtrans keys dari dashboard sandbox

# 2. Set environment variables
export MIDTRANS_SERVER_KEY="SB-Mid-server-xxx"
export MIDTRANS_CLIENT_KEY="SB-Mid-client-xxx"

# 3. Start application
./gradlew bootRun

# 4. Register user
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test123!","name":"Test"}'

# 5. Create payment (use JWT token from step 4)
curl -X POST http://localhost:8081/api/v1/payments/subscription \
  -H "Authorization: Bearer YOUR_TOKEN"

# 6. Open snapRedirectUrl in browser

# 7. Pay with test card: 4811 1111 1111 1114

# 8. Check subscription upgraded to PREMIUM!
```

---

**Status:** ✅ Integration Ready
**Version:** 1.0
**Last Updated:** 2024-12-08
