## Categories — Backend Checklist ✅

### Endpoints

* [x] `GET /api/v1/categories` — list default + user categories
* [x] `POST /api/v1/categories` — create user category
* [x] `PUT /api/v1/categories/{id}` — update user category
* [x] `DELETE /api/v1/categories/{id}` — delete user category

### Entity & Repository (interface-level)

* [x] `Category` entity / model (id, userId nullable, name, type (INCOME/EXPENSE), createdAt)
* [x] `CategoryRepository` with:

    * [x] `List<Category> findByUserIdOrUserIdIsNull(UUID userId)`
    * [x] `Optional<Category> findByIdAndUserId(UUID id, UUID userId)`

### Service Layer

* [x] `CategoryService` methods:

    * [x] `List<CategoryDto> listCategories(UUID userId)`
    * [x] `CategoryDto createCategory(UUID userId, CreateCategoryRequest)`
    * [x] `CategoryDto updateCategory(UUID userId, UUID categoryId, UpdateCategoryRequest)`
    * [x] `void deleteCategory(UUID userId, UUID categoryId)`
* [x] Business rules:

    * [x] `name` required
    * [x] `type` must be INCOME or EXPENSE
    * [x] Default categories (userId == null) should be returned but not editable by user

### Controller & DTOs

* [x] `CategoryController` REST endpoints
* [x] DTOs:

    * [x] `CreateCategoryRequest` (name, type)
    * [x] `UpdateCategoryRequest` (name)
    * [x] `CategoryDto` (id, name, type, isDefault)

### Validation & Error Handling

* [x] Input validation for `type` and `name`
* [x] 403 when trying to edit default category or category owned by another user
* [x] 400 for invalid payloads

### Security

* [x] Auth required on create/update/delete
* [x] Read endpoint (`GET /categories`) allowed for authenticated users; returns default + user

### Tests

* [x] Unit tests for `CategoryService`
* [x] Integration tests for `CategoryController`:

    * [x] List includes default and user categories
    * [x] Create category success
    * [x] Create invalid type → 400
    * [x] Edit default category → 403

---



### Security

* [ ] Auth required on all endpoints
* [ ] Ownership enforcement: userId from JWT must match transaction owner

### Tests

* [ ] Unit tests for `TransactionService` (create, list filters, update, delete)
* [ ] Integration tests for `TransactionController`:

    * [ ] Create transaction success
    * [ ] Create with invalid wallet/category → 400/403
    * [ ] List filtering works (date range, wallet)
    * [ ] Delete transaction not owned → 403

---

## 4) Dashboard Summary — Backend Checklist

### Endpoint

* [ ] `GET /api/v1/dashboard/summary?walletId=&from=&to=` — returns summary and recent transactions

### Service Layer

* [ ] `DashboardService` methods:

    * [ ] `DashboardSummaryDto getSummary(UUID userId, Optional<UUID> walletId, LocalDate from, LocalDate to)`
* [ ] Summary contents:

    * [ ] `walletBalance` (computed summation of transactions or fallback to initial balance + transactions)
    * [ ] `todayIncome`, `todayExpense`
    * [ ] `weeklyTrend` or `series` (list of date → income/expense)
    * [ ] `recentTransactions` (limit e.g., 5)

### Implementation notes (backend-focused)

* [ ] Use optimized queries (aggregations) in repository layer for totals and trends (avoid N+1)
* [ ] Support optional `walletId` filter — otherwise summarize across user wallets
* [ ] Ensure timezone-consistent date handling (use UTC + user locale conversion if needed)
* [ ] Cache or memoize expensive aggregations if needed (future optimization)

