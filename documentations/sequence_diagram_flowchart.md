# Expense Tracker App

**Document:** Sequence Diagrams & Flowcharts (Mermaid)

**Language:** English

**Purpose:** This document contains UML sequence diagrams and flowcharts written in Mermaid syntax to describe the main flows of the Expense Tracker App. Use these diagrams directly in markdown-capable renderers (e.g., GitHub, GitLab, MkDocs with Mermaid plugin) or paste into Mermaid live editors.

---

## Table of Contents

1. Overview
2. Legend / Components
3. Sequence Diagram — Create Wallet
4. Sequence Diagram — Add Expense (including attachment)
5. Sequence Diagram — Transfer Between Wallets
6. Flowchart — High-level Application Flow
7. Notes & Integration Tips

---

## 1. Overview

This document provides three main sequence diagrams and a high-level flowchart to illustrate how the Expense Tracker App works end-to-end. Diagrams are written in Mermaid syntax so they can be rendered in any Mermaid-capable Markdown renderer.

---

## 2. Legend / Components

* **User** — mobile/web user interacting with frontend.
* **Frontend (Client)** — SPA or native client that calls backend APIs.
* **Auth Service** — handles signup, login, issuing JWTs.
* **API Gateway** — (optional) entry point for routing/ rate-limiting.
* **Wallet Service** — manages wallet resources and balances.
* **Transaction Service** — creates, edits, deletes transactions; enforces business rules and idempotency.
* **Reporting Service** — aggregates data for analytics and charts.
* **Notification Service** — sends emails / push notifications.
* **Database** — persistent storage (wallets, transactions, users, categories).

---

## 3. Sequence Diagram — Create Wallet

```mermaid
sequenceDiagram
    participant U as User
    participant FE as Frontend (Client)
    participant AG as API Gateway
    participant AS as Auth Service
    participant WS as Wallet Service
    participant DB as Database
    participant NS as Notification Service

    U->>FE: Click "Create Wallet" (name, currency, icon)
    FE->>AG: POST /wallets {name, currency, initialBalance?} (Authorization: Bearer JWT)
    AG->>AS: Validate JWT (introspect)  
    AS-->>AG: JWT valid (userId)
    AG->>WS: CreateWallet(userId, payload)
    WS->>DB: INSERT wallets (userId, name, currency, balance)
    DB-->>WS: Wallet created (walletId)
    WS->>NS: Enqueue event "wallet.created" (userId, walletId)
    NS-->>WS: Acknowledged
    WS-->>AG: 201 Created {walletId}
    AG-->>FE: 201 Created {walletId}
    FE-->>U: UI shows new wallet & balance
```

**Explanation:**

* Authentication is validated by the Auth Service (could be done inside API Gateway).
* Wallet creation persists to DB and may emit events (for audit, metrics, or notifications).

---

## 4. Sequence Diagram — Add Expense (with attachment)

```mermaid
sequenceDiagram
    participant U as User
    participant FE as Frontend (Client)
    participant AG as API Gateway
    participant AS as Auth Service
    participant TS as Transaction Service
    participant WS as Wallet Service
    participant DB as Database
    participant SS as Storage Service
    participant RS as Reporting Service

    U->>FE: Add Expense (walletId, amount, category, date, attachment)
    FE->>AG: POST /transactions {payload} (Authorization: Bearer JWT)
    AG->>AS: Validate JWT
    AS-->>AG: JWT valid (userId)
    AG->>TS: CreateTransaction(userId, payload)
    TS->>DB: BEGIN TRANSACTION
    TS->>DB: INSERT transactions (userId, walletId, amount, type=expense, category, date, status=pending)
    DB-->>TS: trans_record (transactionId)
    alt attachment present
        TS->>SS: Upload attachment (binary)
        SS-->>TS: attachmentUrl
        TS->>DB: UPDATE transactions SET attachmentUrl WHERE id=transactionId
    end
    TS->>WS: ApplyTransaction(walletId, -amount, transactionId)  
    WS->>DB: UPDATE wallets SET balance = balance - amount WHERE id = walletId
    DB-->>WS: updated_wallet
    WS-->>TS: wallet updated
    TS->>DB: UPDATE transactions SET status=posted WHERE id=transactionId
    DB-->>TS: transaction posted
    TS->>RS: Publish event "transaction.created" (for aggregation)
    RS-->>TS: acknowledged
    TS->>DB: COMMIT
    DB-->>TS: committed
    TS-->>AG: 201 Created {transactionId}
    AG-->>FE: 201 Created {transactionId}
    FE-->>U: Show updated wallet balance & transaction in list
```

**Notes:**

* All balance updates happen in the Wallet Service under DB transaction to keep consistency.
* Storage Service (SS) could be S3 or other object store; attachments are uploaded and referenced.
* Transaction Service emits events for reporting and eventual consistency where needed.

---

## 5. Sequence Diagram — Transfer Between Wallets

```mermaid
sequenceDiagram
    participant U as User
    participant FE as Frontend
    participant AG as API Gateway
    participant AS as Auth Service
    participant TS as Transaction Service
    participant WS as Wallet Service
    participant DB as Database
    participant RS as Reporting Service

    U->>FE: Initiate Transfer (fromWalletId, toWalletId, amount)
    FE->>AG: POST /transfers {from, to, amount} (Authorization: Bearer JWT)
    AG->>AS: Validate JWT
    AS-->>AG: JWT valid (userId)

    AG->>TS: CreateTransfer(userId, payload)
    TS->>DB: BEGIN TRANSACTION
    TS->>DB: INSERT transactions (debit record for fromWallet, type=transfer_out)
    DB-->>TS: debit_tx
    TS->>DB: INSERT transactions (credit record for toWallet, type=transfer_in)
    DB-->>TS: credit_tx
    TS->>WS: ApplyTransaction(fromWalletId, -amount, debit_tx.id)
    WS->>DB: UPDATE wallets SET balance = balance - amount WHERE id = fromWalletId
    DB-->>WS: updated_from_wallet
    WS->>TS: from wallet updated
    TS->>WS: ApplyTransaction(toWalletId, +amount, credit_tx.id)
    WS->>DB: UPDATE wallets SET balance = balance + amount WHERE id = toWalletId
    DB-->>WS: updated_to_wallet
    WS-->>TS: to wallet updated
    TS->>DB: UPDATE transactions SET status=posted WHERE id IN (debit_tx.id, credit_tx.id)
    DB-->>TS: transactions posted
    TS->>RS: Publish event "transfer.completed"
    RS-->>TS: acknowledged
    TS->>DB: COMMIT
    DB-->>TS: committed
    TS-->>AG: 201 Created {transferId}
    AG-->>FE: 201 Created {transferId}
    FE-->>U: Show updated balances for both wallets
```

**Key points:**

* Transfer is implemented as two transactions (debit + credit) within a single DB transaction to maintain consistency and atomicity.
* Use idempotency keys to avoid double-processing if the client retries.

---

## 6. Flowchart — High-level Application Flow

```mermaid
flowchart TD
    A[Start: Open App or Web] --> B{User Logged In?}
    B -- No --> C[Show Login or Signup]
    C --> D[Authenticate via Auth Service]
    D --> B
    B -- Yes --> E[Show Dashboard]
    E --> F[Select Wallet]
    F --> G{Action}
    G -- Create Wallet --> H[POST /wallets]
    G -- Add Expense or Income --> I[POST /transactions]
    G -- Transfer --> J[POST /transfers]
    H --> K[Wallet Service persists wallet]
    I --> L[Transaction Service validates and persists]
    J --> L
    L --> M[Wallet Service updates balances]
    M --> N[Reporting Service aggregates and updates charts]
    N --> O[Frontend refreshes UI and cache]
    O --> P[End: user can repeat actions]

    D -- Invalid --> Q[Show error and retry login]
    L -- Insufficient Funds --> R[Return error to Frontend]
    R --> O
```

**Flowchart description:**

* This high-level flowchart shows the main user choices and the backend services involved.
* Error paths (like authentication failure or insufficient funds) are visualized.

---

## 7. Notes & Integration Tips

* **Idempotency:** For endpoints that change balances (transfers & transaction creation), require an idempotency key from the client. Store the key and response to avoid double-processing.
* **ACID guarantees:** Use DB transactions for multi-step updates (transfer) to keep data consistent.
* **Eventing:** Emit domain events (e.g., `transaction.created`, `wallet.created`) for reporting, notifications, and external integrations. Consider a message broker (Kafka/RabbitMQ/SQS).
* **Storage attachments:** Upload attachments directly from client to signed URLs (S3 pre-signed) to avoid passing binary through backend if you prefer scale.
* **Testing:** Add integration tests for the transactional flows (transfer, edit, delete) and concurrency tests for simultaneous transactions on same wallet.
* **Monitoring:** Track metrics like balance drift, failed transactions, and event lag.

---

### Ready-to-use Mermaid blocks

You can copy & paste the mermaid code blocks from above into your markdown files or renderers. Example renderers: GitHub (with mermaid enabled), GitLab, Mermaid Live Editor, or static site generators with Mermaid plugin.

---

If you'd like, I can:

* Add a separate sequence diagram per microservice (internal calls) with more detail.
* Produce a PNG/SVG export of each diagram.
* Convert flowchart into PlantUML or draw.io format.

Tell me which next step you want.
