CREATE TABLE users (
  id UUID PRIMARY KEY,
  email TEXT UNIQUE,
  password_hash TEXT,
  name TEXT,
  locale TEXT,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE TABLE wallets (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  name TEXT,
  currency VARCHAR(3) DEFAULT 'IDR',
  initial_balance NUMERIC,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE TABLE categories (
  id UUID PRIMARY KEY,
  user_id UUID,
  name TEXT,
  type VARCHAR(10),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE transactions (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  wallet_id UUID REFERENCES wallets(id),
  category_id UUID REFERENCES categories(id),
  type VARCHAR(10),
  amount NUMERIC,
  note TEXT,
  date TIMESTAMP,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE TABLE debts (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  counterparty_name TEXT,
  total_amount NUMERIC,
  remaining_amount NUMERIC,
  due_date DATE,
  status VARCHAR(10),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE TABLE debt_payments (
  id UUID PRIMARY KEY,
  debt_id UUID REFERENCES debts(id),
  amount NUMERIC,
  paid_at TIMESTAMP,
  note TEXT
);

CREATE TABLE subscriptions (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  provider TEXT,
  provider_subscription_id TEXT,
  plan TEXT,
  status TEXT,
  started_at TIMESTAMP,
  ended_at TIMESTAMP
);

