## 3) Transactions — Backend Checklist

### Endpoints

* [x] `GET /api/v1/transactions` — list with filters (`from`, `to`, `walletId`, `categoryId`, `type`, pagination)
* [x] `POST /api/v1/transactions` — create transaction
* [x] `GET /api/v1/transactions/{id}` — get details
* [x] `PUT /api/v1/transactions/{id}` — update transaction
* [x] `DELETE /api/v1/transactions/{id}` — delete transaction

### Entity & Repository (interface-level)

* [x] `Transaction` entity / model (id, userId, walletId, categoryId, type, amount, note, date, createdAt, updatedAt)
* [x] `TransactionRepository` with:

    * [x] `Page<Transaction> findByUserIdAndFilters(UUID userId, ...)` with optimized fetch joins
    * [x] `Optional<Transaction> findByIdAndUserId(UUID id, UUID userId)` with fetch joins

### UseCase Layer (following Single Responsibility Principle)

* [x] `FindAllTransactions` interface + `FindAllTransactionsUseCase` implementation:
    * [x] `Page<TransactionResponse> findByUserIdWithFilters(UUID userId, TransactionFilter filter)`
* [x] `FindTransactionById` interface + `FindTransactionByIdUseCase` implementation:
    * [x] `TransactionResponse findByIdAndUserId(UUID txId, UUID userId)`
* [x] `CreateTransaction` interface + `CreateTransactionUseCase` implementation:
    * [x] `TransactionResponse create(UUID userId, CreateTransactionRequest req)`
* [x] `UpdateTransaction` interface + `UpdateTransactionUseCase` implementation:
    * [x] `TransactionResponse update(UUID userId, UUID txId, UpdateTransactionRequest req)`
* [x] `DeleteTransaction` interface + `DeleteTransactionUseCase` implementation:
    * [x] `void delete(UUID userId, UUID txId)`
* [x] Business rules:
    * [x] `amount > 0` - enforced via @Positive validation
    * [x] `type` must be INCOME or EXPENSE - enforced via TransactionType enum
    * [x] `categoryId` either default or belongs to user - validated in use cases
    * [x] `walletId` must belong to user - validated via repository query
    * [x] On create/update: validate and return DTO with computed fields as needed

### Controller & DTOs

* [x] `TransactionController` endpoints with query params mapping
* [x] DTOs:

    * [x] `CreateTransactionRequest` (walletId, categoryId, type, amount, note, date)
    * [x] `UpdateTransactionRequest` (walletId, categoryId, type, amount, note, date)
    * [x] `TransactionResponse` (id, walletId, walletName, categoryId, categoryName, type, amount, note, date, createdAt, updatedAt)
    * [x] `TransactionFilter` (from, to, walletId, categoryId, type, page, size)

### Validation & Error Handling

* [x] Validate required fields and types - @Valid annotations on DTOs
* [x] Return clear error messages for invalid foreign references (category/wallet not found or not owned)
* [x] Handle pagination defaults & limits - max 100 items per page