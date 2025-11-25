## 3) Transactions — Backend Checklist

### Endpoints

* [ ] `GET /api/v1/transactions` — list with filters (`from`, `to`, `walletId`, `categoryId`, `type`, pagination)
* [ ] `POST /api/v1/transactions` — create transaction
* [ ] `GET /api/v1/transactions/{id}` — get details
* [ ] `PUT /api/v1/transactions/{id}` — update transaction (optional)
* [ ] `DELETE /api/v1/transactions/{id}` — delete transaction

### Entity & Repository (interface-level)

* [ ] `Transaction` entity / model (id, userId, walletId, categoryId, type, amount, note, date, createdAt, updatedAt)
* [ ] `TransactionRepository` with:

    * [ ] `Page<Transaction> findByUserIdAndFilters(UUID userId, ...)` (or multiple query methods)
    * [ ] `Optional<Transaction> findByIdAndUserId(UUID id, UUID userId)`

### UseCase Layer (following Single Responsibility Principle)

* [ ] `FindAllTransactions` interface + `FindAllTransactionsUseCase` implementation:
    * [ ] `Page<TransactionDto> findByUserIdWithFilters(UUID userId, TransactionFilter filter, Pageable pg)`
* [ ] `FindTransactionById` interface + `FindTransactionByIdUseCase` implementation:
    * [ ] `TransactionDto findByIdAndUserId(UUID txId, UUID userId)`
* [ ] `CreateTransaction` interface + `CreateTransactionUseCase` implementation:
    * [ ] `TransactionDto create(UUID userId, CreateTransactionRequest req)`
* [ ] `UpdateTransaction` interface + `UpdateTransactionUseCase` implementation:
    * [ ] `TransactionDto update(UUID userId, UUID txId, UpdateTransactionRequest req)`
* [ ] `DeleteTransaction` interface + `DeleteTransactionUseCase` implementation:
    * [ ] `void delete(UUID userId, UUID txId)`
* [ ] Business rules:
    * [ ] `amount > 0`
    * [ ] `type` must be INCOME or EXPENSE
    * [ ] `categoryId` either default or belongs to user
    * [ ] `walletId` must belong to user
    * [ ] On create/update: validate and return DTO with computed fields as needed

### Controller & DTOs

* [ ] `TransactionController` endpoints with query params mapping
* [ ] DTOs:

    * [ ] `CreateTransactionRequest` (walletId, categoryId, type, amount, note, date)
    * [ ] `TransactionDto` (id, walletId, categoryId, type, amount, note, date, createdAt)
    * [ ] `TransactionFilter` (from, to, walletId, categoryId, type, page, size)

### Validation & Error Handling

* [ ] Validate required fields and types
* [ ] Return clear error messages for invalid foreign references (category/wallet not found or not owned)
* [ ] Handle pagination defaults & limits