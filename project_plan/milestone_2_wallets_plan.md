 # ✅ Wallets Checklist (Milestone 2)

## 🛠 1. Backend — Entity & Repository

* [x] Create `Wallet` entity
* [x] Add enum/constant for currency (optional)
* [x] Create `WalletRepository`

    * [x] Method: `findByUserId(UUID userId)`
    * [x] Method: `existsByUserId(UUID userId)` (for free-user limit)

---

## 💼 2. Backend — Service Layer

* [x] Create `WalletService`

    * [x] `listWallets(userId)`
    * [x] `createWallet(userId, data)`
    * [x] `updateWallet(id, userId, data)`
    * [x] `deleteWallet(id, userId)`
* [x] Validation rules:

    * [x] Name must not be empty
    * [x] initialBalance ≥ 0
    * [x] Free users can only have 1 wallet (prepare hook for premium check)
* [x] Ownership check:

    * [x] Wallet.userId must match userId from JWT

---

## 🌐 3. Backend — Controller (REST API)

Required endpoints:

* [x] `GET /wallets`

    * [x] Return all wallets belonging to the user
* [x] `POST /wallets`

    * [x] Validate payload
    * [x] Check wallet limit for free user
* [x] `PUT /wallets/{id}`

    * [x] Ensure the wallet belongs to the user
* [x] `DELETE /wallets/{id}`

    * [x] Soft/hard delete (based on architecture preference)

### Dashboard Summary (Backend)


* [x] `GET /dashboard/summary?walletId=optional`

Return:
```json
{
"walletBalance": 1200000,
"todayIncome": 150000,
"todayExpense": 50000,
"weeklyTrend": [
{ "date": "2025-11-01", "income": 50000, "expense": 30000 },
...
],
"recentTransactions": [ ...5 terbaru... ]
}
```

---

## 🧪 4. Backend — Testing

Unit & integration tests:

* [x] Test create wallet
* [x] Test update wallet
* [x] Test delete wallet
* [x] Test get wallet list
* [x] Error tests:

    * [x] Create wallet without name
    * [x] Access wallet owned by another user → 403
    * [x] Free user creating second wallet → 400

---

## 🚀 5. Deliverables — Wallet Module

* [x] Wallet CRUD + backend validation
* [ ] State management (Pinia) - Frontend not yet implemented
* [ ] End-to-end API integration - Frontend not yet implemented
* [x] Unit & integration tests
* [ ] Manual QA checklist passed - Requires manual testing

---
