- [ ] Flyway Entity Plan
```sql
users (
  id UUID PK,
  email TEXT UNIQUE,
  password_hash TEXT,
  name TEXT,
  locale TEXT,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

wallets (
  id UUID PK,
  user_id UUID FK,
  name TEXT,
  currency VARCHAR(3) DEFAULT 'IDR',
  initial_balance NUMERIC,
  created_at TIMESTAMP
);

categories (
  id UUID PK,
  user_id UUID NULL,
  name TEXT,
  type VARCHAR(10),
  created_at TIMESTAMP
);

transactions (
  id UUID PK,
  user_id UUID FK,
  wallet_id UUID FK,
  category_id UUID FK,
  type VARCHAR(10),
  amount NUMERIC,
  note TEXT,
  date TIMESTAMP,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

debts (
  id UUID PK,
  user_id UUID FK,
  counterparty_name TEXT,
  total_amount NUMERIC,
  remaining_amount NUMERIC,
  due_date DATE,
  status VARCHAR(10),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

debt_payments (
  id UUID PK,
  debt_id UUID FK,
  amount NUMERIC,
  paid_at TIMESTAMP,
  note TEXT
);

subscriptions (
  id UUID PK,
  user_id UUID FK,
  provider TEXT,
  provider_subscription_id TEXT,
  plan TEXT,
  status TEXT,
  started_at TIMESTAMP,
  ended_at TIMESTAMP
);
```

---
- [ ] Explanation of Entities