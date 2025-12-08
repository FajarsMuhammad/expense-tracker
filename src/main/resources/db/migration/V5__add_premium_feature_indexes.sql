-- Migration V5: Add Premium Feature Indexes
-- Purpose: Ensure indexes exist for fast subscription and debt lookups
-- Date: 2024-12-08

-- Most of these indexes were already created in V3__add_reporting_indexes.sql
-- This migration verifies their existence and adds any missing ones

-- Index for fast subscription lookup by user and status
-- Used for isPremiumUser() checks
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_status
ON subscriptions(user_id, status);

-- Index for active debt counting
-- Used for FREE tier debt limit enforcement (countActiveDebtsByUserId)
CREATE INDEX IF NOT EXISTS idx_debts_user_status
ON debts(user_id, status);

-- Index for wallet counting
-- Used for FREE tier wallet limit enforcement
CREATE INDEX IF NOT EXISTS idx_wallets_user
ON wallets(user_id);

-- Query performance validation comments:
-- 1. Subscription check: O(log n) lookup time with idx_subscriptions_user_status
-- 2. Debt count: O(log n) lookup time with idx_debts_user_status
-- 3. Wallet count: O(log n) lookup time with idx_wallets_user
