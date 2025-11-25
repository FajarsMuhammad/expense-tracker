# Swagger/OpenAPI Documentation

## 📚 Accessing API Documentation

The Expense Tracker API now includes interactive Swagger/OpenAPI documentation!

### Swagger UI

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/swagger-ui/index.html
```

Or the shorter URL:

```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON Specification

The raw OpenAPI 3.0 specification in JSON format is available at:

```
http://localhost:8080/v3/api-docs
```

## 🔐 Authentication in Swagger

### How to Authenticate:

1. **Get a JWT Token**:
   - First, use the `/auth/login` or `/auth/register` endpoint in Swagger UI
   - Click "Execute" to get your response
   - Copy the `token` value from the response

2. **Set Authorization**:
   - Click the **"Authorize"** button at the top right of Swagger UI
   - Enter: `Bearer {your-token-here}` (without the curly braces)
   - Click "Authorize"
   - Click "Close"

3. **Make Authenticated Requests**:
   - All subsequent requests will now include the JWT token
   - You can now test protected endpoints like `/wallets` and `/dashboard/summary`

### Example:

```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 📋 Available API Groups

### 1. **Authentication** (`/auth`)
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token
- `POST /auth/refresh` - Refresh JWT token

### 2. **User** (`/me`)
- `GET /me` - Get current authenticated user info

### 3. **Wallets** (`/wallets`)
- `GET /wallets` - List all user wallets
- `GET /wallets/{id}` - Get specific wallet
- `POST /wallets` - Create new wallet
- `PUT /wallets/{id}` - Update wallet
- `DELETE /wallets/{id}` - Delete wallet

### 4. **Dashboard** (`/dashboard`)
- `GET /dashboard/summary` - Get financial summary with analytics

## 🎯 Key Features

### 📝 Interactive Documentation
- **Try It Out**: Execute API calls directly from the browser
- **Request/Response Examples**: See actual JSON examples
- **Schema Definitions**: View all DTOs and their structures
- **Validation Rules**: See required fields and constraints

### 🔍 Request Examples

Swagger UI provides example request bodies for each endpoint. For example:

**Create Wallet Request:**
```json
{
  "name": "My Savings",
  "currency": "IDR",
  "initialBalance": 5000000
}
```

### ✅ Response Examples

You can see example responses for each status code:

**200 OK - Wallet Response:**
```json
{
  "id": "uuid",
  "name": "My Savings",
  "currency": "IDR",
  "initialBalance": 5000000.0,
  "currentBalance": 5000000.0,
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:00:00Z"
}
```

**400 Bad Request - Validation Error:**
```json
{
  "timestamp": "2025-11-24T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Wallet name must not be empty"
}
```

## 🛠️ Technical Details

### Configuration

The Swagger configuration is located at:
```
src/main/java/com/fajars/expensetracker/common/config/OpenApiConfig.java
```

### Dependencies

The project uses SpringDoc OpenAPI v2.3.0:
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

### Security Configuration

Swagger endpoints are whitelisted in Spring Security:
```java
.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
```

### Annotations Used

#### Controller Level:
- `@Tag` - Groups endpoints together
- `@SecurityRequirement` - Indicates JWT authentication required

#### Method Level:
- `@Operation` - Describes the endpoint
- `@ApiResponses` - Lists possible response codes
- `@ApiResponse` - Describes individual response
- `@Parameter` - Describes path/query parameters
- `@RequestBody` - Describes request body schema

## 📊 Supported Currencies

The API supports the following currencies:

| Code | Currency | Symbol |
|------|----------|--------|
| IDR | Indonesian Rupiah | Rp |
| USD | US Dollar | $ |
| EUR | Euro | € |
| GBP | British Pound | £ |
| JPY | Japanese Yen | ¥ |
| SGD | Singapore Dollar | S$ |
| MYR | Malaysian Ringgit | RM |

## 🚀 Quick Start Guide

### 1. Start the Application
```bash
./gradlew bootRun
```

### 2. Open Swagger UI
Navigate to: http://localhost:8080/swagger-ui.html

### 3. Register a New User
1. Expand `Authentication` section
2. Click on `POST /auth/register`
3. Click "Try it out"
4. Fill in the request body:
```json
{
  "email": "test@example.com",
  "password": "password123",
  "name": "Test User"
}
```
5. Click "Execute"
6. Copy the `token` from the response

### 4. Authorize
1. Click the **"Authorize"** button (lock icon)
2. Enter: `Bearer {your-token}`
3. Click "Authorize"

### 5. Create a Wallet
1. Expand `Wallets` section
2. Click on `POST /wallets`
3. Click "Try it out"
4. Fill in the request body:
```json
{
  "name": "My First Wallet",
  "currency": "IDR",
  "initialBalance": 1000000
}
```
5. Click "Execute"

### 6. View Dashboard
1. Expand `Dashboard` section
2. Click on `GET /dashboard/summary`
3. Click "Try it out"
4. Click "Execute"

## 🎨 Benefits of Using Swagger

### For Developers:
- ✅ Interactive API testing without Postman
- ✅ Auto-generated documentation
- ✅ Always up-to-date with code
- ✅ Clear request/response examples
- ✅ Validation rules visible

### For Frontend Developers:
- ✅ Complete API reference
- ✅ Easy to test endpoints
- ✅ Clear data structures
- ✅ Error response examples
- ✅ Can generate client code

### For QA/Testing:
- ✅ Manual API testing
- ✅ Test different scenarios
- ✅ Verify error responses
- ✅ No additional tools needed

## 🔧 Customization

### Changing API Info

Edit `OpenApiConfig.java` to customize:
- Title
- Description
- Version
- Contact information
- License

### Adding More Annotations

Add more Swagger annotations to your controllers for:
- More detailed descriptions
- Example values
- Additional response codes
- Security requirements
- Deprecation warnings

## 📱 Export Options

Swagger UI allows you to:
1. **Download OpenAPI Spec**: Get the JSON/YAML specification
2. **Generate Client Code**: Use tools like OpenAPI Generator
3. **Import to Postman**: Import the spec directly

## 🌐 Production Considerations

### Disable in Production (Optional)

Add to `application.properties`:
```properties
# Disable Swagger in production
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

### Secure Swagger UI

In production, you may want to:
1. Add authentication to Swagger UI itself
2. Restrict access to specific IPs
3. Use environment-based configuration

---

**Happy API Testing! 🚀**
