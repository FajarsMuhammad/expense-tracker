# 🚀 Midtrans Quick Start - 5 Menit Setup

## 1️⃣ Dapatkan API Keys

**Dashboard Sandbox:** https://dashboard.sandbox.midtrans.com/

```
Settings → Access Keys

✅ Server Key: SB-Mid-server-xxxxxxxxxxxxxxx
✅ Client Key: SB-Mid-client-xxxxxxxxxxxxxxx
```

## 2️⃣ Set Environment Variables

**Linux/Mac:**
```bash
export MIDTRANS_SERVER_KEY="SB-Mid-server-xxxxxxxxxxxxxxx"
export MIDTRANS_CLIENT_KEY="SB-Mid-client-xxxxxxxxxxxxxxx"
```

**Windows PowerShell:**
```powershell
$env:MIDTRANS_SERVER_KEY="SB-Mid-server-xxxxxxxxxxxxxxx"
$env:MIDTRANS_CLIENT_KEY="SB-Mid-client-xxxxxxxxxxxxxxx"
```

## 3️⃣ Start Application

```bash
./gradlew bootRun
```

✅ Check logs: "Midtrans configuration loaded successfully"

## 4️⃣ Test API

### Register User
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "name": "Test User"
  }'
```

**Copy JWT token dari response!**

### Create Payment
```bash
curl -X POST http://localhost:8081/api/v1/payments/subscription \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Response:**
```json
{
  "snapRedirectUrl": "https://app.sandbox.midtrans.com/snap/v3/..."
}
```

### Pay!
1. Copy `snapRedirectUrl`
2. Buka di browser
3. Pilih **Credit Card**
4. Card: `4811 1111 1111 1114`
5. CVV: `123`
6. Expiry: `01/25`
7. Klik **Pay**

## 5️⃣ Verify Premium Active

```bash
# Check subscription status
curl http://localhost:8081/api/v1/subscriptions/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Should return:**
```json
{
  "tier": "PREMIUM",
  "status": "ACTIVE",
  "endedAt": "2025-01-07T..." // 30 days from now
}
```

---

## 🧪 Test Cards (Sandbox)

| Card Number | Result | CVV | Expiry |
|-------------|--------|-----|--------|
| **4811 1111 1111 1114** | ✅ Success | 123 | 01/25 |
| 4911 1111 1111 1113 | ⚠️ Challenge (3DS) | 123 | 01/25 |
| 4411 1111 1111 1118 | ❌ Denied | 123 | 01/25 |

---

## 📍 Webhook Setup (Local Testing)

### Install ngrok
```bash
# Download dari https://ngrok.com/download
# Or dengan package manager:
brew install ngrok  # Mac
choco install ngrok # Windows
```

### Expose localhost
```bash
ngrok http 8081
```

**Copy URL:** `https://abc123.ngrok.io`

### Set di Midtrans Dashboard
```
Dashboard → Settings → Configuration
Payment Notification URL: https://abc123.ngrok.io/api/v1/payments/webhook/midtrans
```

---

## 🔍 Troubleshooting

### ❌ "Server key is required"
```bash
# Verify environment variable
echo $MIDTRANS_SERVER_KEY

# Re-export if empty
export MIDTRANS_SERVER_KEY="SB-Mid-server-xxx"
```

### ❌ "401 Unauthorized"
- Server key salah
- Atau pakai production key di sandbox URL

**Solution:** Re-check keys di dashboard

### ❌ Webhook tidak dipanggil
1. Pakai **ngrok** untuk local testing
2. Update webhook URL di dashboard Midtrans
3. Check logs: `tail -f logs/application.log | grep webhook`

---

## ✅ Success Checklist

- [ ] API keys configured
- [ ] Application starts without errors
- [ ] User registration works (GET JWT token)
- [ ] Payment creation works (GET snapRedirectUrl)
- [ ] Can open Snap payment page
- [ ] Test payment success with card `4811 1111 1111 1114`
- [ ] Subscription upgraded to PREMIUM
- [ ] Can create unlimited wallets/debts
- [ ] Can export Excel/PDF

---

## 📚 Full Documentation

**Lengkap:** `MIDTRANS_INTEGRATION_GUIDE.md`

**Midtrans Docs:** https://docs.midtrans.com/

**Support:** support@midtrans.com

---

## 💡 Pro Tips

1. **Idempotency Key** - Cegah duplicate payment:
   ```bash
   -H "X-Idempotency-Key: unique-key-$(date +%s)"
   ```

2. **Monitor Logs** - Real-time debugging:
   ```bash
   tail -f logs/application.log | grep -E "payment|webhook|midtrans"
   ```

3. **Database Check** - Verify payment status:
   ```sql
   SELECT * FROM payment_transactions ORDER BY created_at DESC LIMIT 5;
   SELECT * FROM subscriptions WHERE status = 'ACTIVE';
   ```

4. **Frontend Integration** - Gunakan Snap.js:
   ```html
   <script src="https://app.sandbox.midtrans.com/snap/snap.js"></script>
   <script>
   snap.pay(snapToken, {
     onSuccess: (result) => console.log('Success:', result)
   });
   </script>
   ```

---

**Ready to Go!** 🚀

Selamat mencoba! Kalau ada masalah, check **MIDTRANS_INTEGRATION_GUIDE.md** untuk detail lengkap.
