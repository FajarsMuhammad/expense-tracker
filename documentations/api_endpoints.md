# Expense Tracker API Endpoints

## 📚 Interactive Documentation

**🎯 Try out the APIs interactively with Swagger UI:**

```
http://localhost:8080/swagger-ui.html
```

Swagger provides a web interface to test all APIs without writing code!
See [SWAGGER_DOCUMENTATION.md](swagger_documentation.md) for detailed instructions.

---

## Authentication

### Register
```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": "uuid",
  "email": "user@example.com",
  "name": "John Doe"
}
```

### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

## User

### Get Current User
```http
GET /me
Authorization: Bearer {token}
```

**Response:**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "name": "John Doe"
}
```

## Wallets

### List All Wallets
```http
GET /wallets
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "id": "uuid",
    "name": "Main Wallet",
    "currency": "IDR",
    "initialBalance": 1000000.0,
    "currentBalance": 1500000.0,
    "createdAt": "2025-11-24T10:00:00Z",
    "updatedAt": "2025-11-24T10:00:00Z"
  }
]
```

### Get Single Wallet
```http
GET /wallets/{id}
Authorization: Bearer {token}
```

### Create Wallet
```http
POST /wallets
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Savings Account",
  "currency": "IDR",
  "initialBalance": 5000000.0
}
```

**Supported Currencies:**
- `IDR` - Indonesian Rupiah
- `USD` - US Dollar
- `EUR` - Euro
- `GBP` - British Pound
- `JPY` - Japanese Yen
- `SGD` - Singapore Dollar
- `MYR` - Malaysian Ringgit

**Validations:**
- Name: required, not blank
- Currency: required, valid enum
- Initial Balance: required, >= 0
- Free users: max 1 wallet

**Response:** `201 Created`
```json
{
  "id": "uuid",
  "name": "Savings Account",
  "currency": "IDR",
  "initialBalance": 5000000.0,
  "currentBalance": 5000000.0,
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:00:00Z"
}
```

### Update Wallet
```http
PUT /wallets/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Updated Wallet Name",
  "currency": "USD",
  "initialBalance": 10000.0
}
```

**Response:** `200 OK`

### Delete Wallet
```http
DELETE /wallets/{id}
Authorization: Bearer {token}
```

**Response:** `204 No Content`

## Dashboard

### Get Summary
```http
GET /dashboard/summary?walletId={optional}
Authorization: Bearer {token}
```

**Query Parameters:**
- `walletId` (optional): Filter by specific wallet. If omitted, shows all wallets.

**Response:**
```json
{
  "walletBalance": 1200000.0,
  "todayIncome": 150000.0,
  "todayExpense": 50000.0,
  "weeklyTrend": [
    {
      "date": "2025-11-18T00:00:00Z",
      "income": 50000.0,
      "expense": 30000.0
    },
    {
      "date": "2025-11-19T00:00:00Z",
      "income": 0.0,
      "expense": 20000.0
    }
  ],
  "recentTransactions": []
}
```

**Weekly Trend:** Last 7 days including today

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2025-11-24T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Wallet name must not be empty"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2025-11-24T10:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "User not authenticated"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2025-11-24T10:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Wallet not found or access denied"
}
```

### 400 Wallet Limit Exceeded
```json
{
  "timestamp": "2025-11-24T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Free users can only create 1 wallet. Upgrade to premium for unlimited wallets."
}
```

## Testing with cURL

### Login and Save Token
```bash
# Login
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}' \
  | jq -r '.token')

echo $TOKEN
```

### Create Wallet
```bash
curl -X POST http://localhost:8080/wallets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Wallet",
    "currency": "IDR",
    "initialBalance": 1000000
  }'
```

### List Wallets
```bash
curl -X GET http://localhost:8080/wallets \
  -H "Authorization: Bearer $TOKEN"
```

### Get Dashboard Summary
```bash
curl -X GET http://localhost:8080/dashboard/summary \
  -H "Authorization: Bearer $TOKEN"
```

---

**Base URL:** `http://localhost:8080`
**Authentication:** Bearer Token (JWT)
**Content-Type:** `application/json`
