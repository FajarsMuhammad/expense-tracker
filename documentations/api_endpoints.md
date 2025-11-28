# Expense Tracker API Endpoints

## 📚 Interactive Documentation

**🎯 Try out the APIs interactively with Swagger UI:**

```
http://localhost:8081/swagger-ui.html
```

Swagger provides a web interface to test all APIs without writing code!
See [SWAGGER_DOCUMENTATION.md](swagger_documentation.md) for detailed instructions.

---

## Quick Reference

### Available Endpoints

| Feature | Endpoints | Base Path | Authentication |
|---------|-----------|-----------|----------------|
| **Authentication** | Register, Login, Refresh Token | `/api/v1/auth` | Public |
| **User** | Get Current User | `/api/v1/me` | Required |
| **Wallets** | CRUD operations | `/api/v1/wallets` | Required |
| **Categories** | CRUD operations, Filter by type | `/api/v1/categories` | Required |
| **Dashboard** | Summary, Weekly trends | `/api/v1/dashboard` | Required |
| **Transactions** | CRUD operations, Filter & pagination | `/api/v1/transactions` | Required |

### Quick Links
- [Authentication](#authentication) - Register, Login, Refresh
- [User](#user) - Get profile
- [Wallets](#wallets) - Manage wallets
- [Categories](#categories) - Manage income/expense categories
- [Transactions](#transactions) - Manage income and expense transactions
- [Dashboard](#dashboard) - View summaries and trends
- [Error Responses](#error-responses) - Status codes and formats
- [Testing with cURL](#testing-with-curl) - Command-line examples

---

## Authentication

### Register
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

**Validations:**
- Email: required, valid email format, unique
- Password: required, minimum 6 characters
- Name: required, not blank

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": "uuid",
  "email": "user@example.com",
  "name": "John Doe"
}
```

### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:** `200 OK`
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
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsNewRefreshedToken...",
  "userId": "uuid",
  "email": "user@example.com",
  "name": "John Doe"
}
```

## User

### Get Current User
```http
GET /api/v1/me
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
GET /api/v1/wallets
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
GET /api/v1/wallets/{id}
Authorization: Bearer {token}
```

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "name": "Main Wallet",
  "currency": "IDR",
  "initialBalance": 1000000.0,
  "currentBalance": 1500000.0,
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:00:00Z"
}
```

### Create Wallet
```http
POST /api/v1/wallets
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
PUT /api/v1/wallets/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Updated Wallet Name",
  "currency": "USD",
  "initialBalance": 10000.0
}
```

**Validations:**
- Same as Create Wallet
- User must own the wallet

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "name": "Updated Wallet Name",
  "currency": "USD",
  "initialBalance": 10000.0,
  "currentBalance": 10000.0,
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T15:00:00Z"
}
```

### Delete Wallet
```http
DELETE /api/v1/wallets/{id}
Authorization: Bearer {token}
```

**Response:** `204 No Content`

## Categories

### List All Categories
```http
GET /api/v1/categories?type={optional}
Authorization: Bearer {token}
```

**Query Parameters:**
- `type` (optional): Filter by type. Values: `INCOME` or `EXPENSE`

**Response:**
```json
[
  {
    "id": "uuid",
    "name": "Salary",
    "type": "INCOME",
    "isDefault": true,
    "createdAt": "2025-11-24T10:00:00Z"
  },
  {
    "id": "uuid",
    "name": "Food",
    "type": "EXPENSE",
    "isDefault": true,
    "createdAt": "2025-11-24T10:00:00Z"
  },
  {
    "id": "uuid",
    "name": "Custom Category",
    "type": "EXPENSE",
    "isDefault": false,
    "createdAt": "2025-11-24T10:00:00Z"
  }
]
```

**Notes:**
- Returns both default system categories and user's custom categories
- Default categories cannot be modified or deleted
- Free users can see default categories without creating custom ones

### Get Single Category
```http
GET /api/v1/categories/{id}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "id": "uuid",
  "name": "Food",
  "type": "EXPENSE",
  "isDefault": true,
  "createdAt": "2025-11-24T10:00:00Z"
}
```

### Create Category
```http
POST /api/v1/categories
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Entertainment",
  "type": "EXPENSE"
}
```

**Validations:**
- Name: required, not blank
- Type: required, must be `INCOME` or `EXPENSE`

**Response:** `201 Created`
```json
{
  "id": "uuid",
  "name": "Entertainment",
  "type": "EXPENSE",
  "isDefault": false,
  "createdAt": "2025-11-24T10:00:00Z"
}
```

### Update Category
```http
PUT /api/v1/categories/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Updated Category Name"
}
```

**Validations:**
- Name: required, not blank
- Cannot update default system categories
- User must own the category

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "name": "Updated Category Name",
  "type": "EXPENSE",
  "isDefault": false,
  "createdAt": "2025-11-24T10:00:00Z"
}
```

### Delete Category
```http
DELETE /api/v1/categories/{id}
Authorization: Bearer {token}
```

**Constraints:**
- Cannot delete default system categories
- User must own the category
- Category must not be in use by any transactions

**Response:** `204 No Content`

## Dashboard

### Get Summary
```http
GET /api/v1/dashboard/summary?walletId={optional}
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
    },
    {
      "date": "2025-11-20T00:00:00Z",
      "income": 100000.0,
      "expense": 45000.0
    }
  ],
  "recentTransactions": [
    {
      "id": "uuid",
      "walletId": "uuid",
      "walletName": "Main Wallet",
      "categoryId": "uuid",
      "categoryName": "Food",
      "type": "EXPENSE",
      "amount": 50000.0,
      "note": "Lunch",
      "date": "2025-11-24T12:00:00Z",
      "createdAt": "2025-11-24T12:00:00Z",
      "updatedAt": "2025-11-24T12:00:00Z"
    }
  ]
}
```

**Weekly Trend:** Last 7 days including today

## Transactions

### List All Transactions
```http
GET /api/v1/transactions?walletId={optional}&categoryId={optional}&type={optional}&from={optional}&to={optional}&page={optional}&size={optional}
Authorization: Bearer {token}
```

**Query Parameters:**
- `walletId` (optional): Filter by wallet ID (UUID)
- `categoryId` (optional): Filter by category ID (UUID)
- `type` (optional): Filter by type. Values: `INCOME` or `EXPENSE`
- `from` (optional): Filter transactions from date (format: `yyyy-MM-dd`)
- `to` (optional): Filter transactions to date (format: `yyyy-MM-dd`)
- `page` (optional): Page number, 0-based (default: `0`)
- `size` (optional): Page size, max 100 (default: `20`)

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "uuid",
      "walletId": "uuid",
      "walletName": "Main Wallet",
      "categoryId": "uuid",
      "categoryName": "Food",
      "type": "EXPENSE",
      "amount": 50000.0,
      "note": "Lunch at restaurant",
      "date": "2025-11-24T12:00:00Z",
      "createdAt": "2025-11-24T12:00:00Z",
      "updatedAt": "2025-11-24T12:00:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true
}
```

### Get Single Transaction
```http
GET /api/v1/transactions/{id}
Authorization: Bearer {token}
```

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "walletId": "uuid",
  "walletName": "Main Wallet",
  "categoryId": "uuid",
  "categoryName": "Food",
  "type": "EXPENSE",
  "amount": 50000.0,
  "note": "Lunch at restaurant",
  "date": "2025-11-24T12:00:00Z",
  "createdAt": "2025-11-24T12:00:00Z",
  "updatedAt": "2025-11-24T12:00:00Z"
}
```

### Create Transaction
```http
POST /api/v1/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
  "walletId": "uuid",
  "categoryId": "uuid",
  "type": "EXPENSE",
  "amount": 50000.0,
  "note": "Lunch at restaurant",
  "date": "2025-11-24T12:00:00.000Z"
}
```

**Validations:**
- Wallet ID: required, must belong to user
- Category ID: required, must be default or belong to user
- Type: required, must be `INCOME` or `EXPENSE`
- Amount: required, must be positive (> 0)
- Note: optional, max 500 characters
- Date: required

**Response:** `201 Created`
```json
{
  "id": "uuid",
  "walletId": "uuid",
  "walletName": "Main Wallet",
  "categoryId": "uuid",
  "categoryName": "Food",
  "type": "EXPENSE",
  "amount": 50000.0,
  "note": "Lunch at restaurant",
  "date": "2025-11-24T12:00:00Z",
  "createdAt": "2025-11-24T12:00:00Z",
  "updatedAt": "2025-11-24T12:00:00Z"
}
```

### Update Transaction
```http
PUT /api/v1/transactions/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "walletId": "uuid",
  "categoryId": "uuid",
  "type": "EXPENSE",
  "amount": 55000.0,
  "note": "Lunch at restaurant (updated)",
  "date": "2025-11-24T12:00:00.000Z"
}
```

**Validations:**
- Same as Create Transaction
- User must own the transaction

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "walletId": "uuid",
  "walletName": "Main Wallet",
  "categoryId": "uuid",
  "categoryName": "Food",
  "type": "EXPENSE",
  "amount": 55000.0,
  "note": "Lunch at restaurant (updated)",
  "date": "2025-11-24T12:00:00Z",
  "createdAt": "2025-11-24T12:00:00Z",
  "updatedAt": "2025-11-24T15:00:00Z"
}
```

### Delete Transaction
```http
DELETE /api/v1/transactions/{id}
Authorization: Bearer {token}
```

**Constraints:**
- User must own the transaction

**Response:** `204 No Content`

## Error Responses

All error responses follow a consistent format with the `ErrorResponse` DTO structure. The API uses proper HTTP status codes and provides descriptive error messages.

### Common Status Codes
- `400 Bad Request` - Invalid input, validation failures, business rule violations
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - Authenticated but lacking permission to access resource
- `404 Not Found` - Resource doesn't exist
- `409 Conflict` - Duplicate resource or constraint violation
- `500 Internal Server Error` - Unexpected server error

### 400 Bad Request - Validation Error
```json
{
  "timestamp": "2025-11-24T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Wallet name must not be empty",
  "path": "/api/v1/wallets",
  "correlationId": "abc-123-def",
  "validationErrors": {
    "name": "must not be blank"
  }
}
```

**Common validation scenarios:**
- Empty required fields
- Invalid email format
- Password too short (minimum 6 characters)
- Invalid currency code
- Negative balance amounts
- Invalid transaction type (must be INCOME or EXPENSE)

### 401 Unauthorized
```json
{
  "timestamp": "2025-11-24T10:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "User not authenticated",
  "path": "/api/v1/wallets",
  "correlationId": "abc-123-def"
}
```

**Occurs when:**
- Missing `Authorization` header
- Invalid or expired JWT token
- Malformed bearer token

### 403 Forbidden
```json
{
  "timestamp": "2025-11-24T10:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Wallet not found or access denied",
  "path": "/api/v1/wallets/uuid",
  "correlationId": "abc-123-def"
}
```

**Occurs when:**
- Accessing another user's wallet
- Accessing another user's custom category
- Attempting to modify default system categories

### 400 Business Rule Violation - Wallet Limit
```json
{
  "timestamp": "2025-11-24T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Free users can only create 1 wallet. Upgrade to premium for unlimited wallets.",
  "path": "/api/v1/wallets",
  "correlationId": "abc-123-def"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-11-24T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Category not found",
  "path": "/api/v1/categories/invalid-uuid",
  "correlationId": "abc-123-def"
}
```

### 409 Conflict - Duplicate Resource
```json
{
  "timestamp": "2025-11-24T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered",
  "path": "/api/v1/auth/register",
  "correlationId": "abc-123-def"
}
```

**Note:** The `correlationId` field helps trace requests in logs for debugging purposes.

## Testing with cURL

### Prerequisites
Install `jq` for JSON parsing:
```bash
# macOS
brew install jq

# Ubuntu/Debian
sudo apt-get install jq
```

### Authentication

#### Register New User
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123",
    "name": "New User"
  }'
```

#### Login and Save Token
```bash
TOKEN=$(curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}' \
  | jq -r '.token')

echo "Logged in with token: $TOKEN"
```

#### Refresh Token
```bash
NEW_TOKEN=$(curl -X POST http://localhost:8081/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\"}" \
  | jq -r '.token')

TOKEN=$NEW_TOKEN
echo "Token refreshed: $TOKEN"
```

### Wallets

#### List All Wallets
```bash
curl -X GET http://localhost:8081/api/v1/wallets \
  -H "Authorization: Bearer $TOKEN"
```

#### Create Wallet
```bash
curl -X POST http://localhost:8081/api/v1/wallets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Wallet",
    "currency": "IDR",
    "initialBalance": 1000000
  }'
```

#### Get Specific Wallet
```bash
WALLET_ID="your-wallet-uuid"
curl -X GET http://localhost:8081/api/v1/wallets/$WALLET_ID \
  -H "Authorization: Bearer $TOKEN"
```

#### Update Wallet
```bash
WALLET_ID="your-wallet-uuid"
curl -X PUT http://localhost:8081/api/v1/wallets/$WALLET_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Wallet",
    "currency": "USD",
    "initialBalance": 5000
  }'
```

#### Delete Wallet
```bash
WALLET_ID="your-wallet-uuid"
curl -X DELETE http://localhost:8081/api/v1/wallets/$WALLET_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Categories

#### List All Categories
```bash
# All categories
curl -X GET http://localhost:8081/api/v1/categories \
  -H "Authorization: Bearer $TOKEN"

# Filter by type
curl -X GET "http://localhost:8081/api/v1/categories?type=EXPENSE" \
  -H "Authorization: Bearer $TOKEN"
```

#### Create Custom Category
```bash
curl -X POST http://localhost:8081/api/v1/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Entertainment",
    "type": "EXPENSE"
  }'
```

#### Get Specific Category
```bash
CATEGORY_ID="your-category-uuid"
curl -X GET http://localhost:8081/api/v1/categories/$CATEGORY_ID \
  -H "Authorization: Bearer $TOKEN"
```

#### Update Category
```bash
CATEGORY_ID="your-category-uuid"
curl -X PUT http://localhost:8081/api/v1/categories/$CATEGORY_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Category Name"
  }'
```

#### Delete Category
```bash
CATEGORY_ID="your-category-uuid"
curl -X DELETE http://localhost:8081/api/v1/categories/$CATEGORY_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Dashboard

#### Get Dashboard Summary
```bash
# All wallets summary
curl -X GET http://localhost:8081/dashboard/summary \
  -H "Authorization: Bearer $TOKEN"

# Specific wallet summary
WALLET_ID="your-wallet-uuid"
curl -X GET "http://localhost:8081/api/v1/dashboard/summary?walletId=$WALLET_ID" \
  -H "Authorization: Bearer $TOKEN"
```

### Complete Workflow Example
```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}' \
  | jq -r '.token')

# 2. Create a wallet
WALLET_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/wallets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Wallet","currency":"IDR","initialBalance":1000000}')

WALLET_ID=$(echo $WALLET_RESPONSE | jq -r '.id')
echo "Created wallet: $WALLET_ID"

# 3. List categories
curl -s -X GET http://localhost:8081/api/v1/categories \
  -H "Authorization: Bearer $TOKEN" | jq

# 4. Get dashboard summary
curl -s -X GET "http://localhost:8081/api/v1/dashboard/summary?walletId=$WALLET_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

**Base URL:** `http://localhost:8081`
**Authentication:** Bearer Token (JWT)
**Content-Type:** `application/json`
