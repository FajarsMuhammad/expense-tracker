-- Migration V3: Add performance indexes for reporting queries
-- Purpose: Optimize report generation, export operations, and analytics queries
-- Date: 2025-12-03

-- ============================================================================
-- TRANSACTIONS TABLE INDEXES
-- ============================================================================

-- Index for report queries filtering by user and date range
-- Used by: getSummaryByDateRange(), getTrendData(), getCategoryBreakdown()
-- Query pattern: WHERE user_id = ? AND date >= ? AND date <= ?
CREATE INDEX IF NOT EXISTS idx_transactions_user_date
ON transactions(user_id, date DESC);

-- Index for filtering by user, date, and type (INCOME/EXPENSE)
-- Used by: getCategoryBreakdown() with type filter
-- Query pattern: WHERE user_id = ? AND date >= ? AND date <= ? AND type = ?
CREATE INDEX IF NOT EXISTS idx_transactions_user_date_type
ON transactions(user_id, date DESC, type);

-- Index for category-based reporting
-- Used by: Category breakdown queries, analytics
-- Query pattern: WHERE user_id = ? AND category_id = ? AND date >= ?
CREATE INDEX IF NOT EXISTS idx_transactions_user_category_date
ON transactions(user_id, category_id, date DESC);

-- Index for wallet-based reporting
-- Used by: Wallet balance calculations, wallet-specific reports
-- Query pattern: WHERE user_id = ? AND wallet_id = ? AND date >= ?
CREATE INDEX IF NOT EXISTS idx_transactions_user_wallet_date
ON transactions(user_id, wallet_id, date DESC);

-- Composite index for complex report filters
-- Used by: findByUserIdWithFilters() with multiple filters
-- Query pattern: WHERE user_id = ? AND wallet_id = ? AND category_id = ? AND type = ?
CREATE INDEX IF NOT EXISTS idx_transactions_user_wallet_category_type
ON transactions(user_id, wallet_id, category_id, type);

-- Index for date-only queries (trend analysis)
-- Used by: Time series analysis, daily aggregations
CREATE INDEX IF NOT EXISTS idx_transactions_date
ON transactions(date DESC);

-- ============================================================================
-- CATEGORIES TABLE INDEXES
-- ============================================================================

-- Index for category lookups by user
-- Used by: Category breakdown, category filtering
-- Query pattern: WHERE user_id = ? AND type = ?
CREATE INDEX IF NOT EXISTS idx_categories_user_type
ON categories(user_id, type);

-- ============================================================================
-- WALLETS TABLE INDEXES
-- ============================================================================

-- Index for wallet lookups by user
-- Used by: Wallet balance calculations, wallet filtering
-- Query pattern: WHERE user_id = ?
CREATE INDEX IF NOT EXISTS idx_wallets_user
ON wallets(user_id);

-- ============================================================================
-- DEBTS TABLE INDEXES
-- ============================================================================

-- Index for debt queries by user and status
-- Used by: Debt reports, overdue analysis
-- Query pattern: WHERE user_id = ? AND status = ?
CREATE INDEX IF NOT EXISTS idx_debts_user_status
ON debts(user_id, status);

-- Index for debt queries by user and due date
-- Used by: Overdue debt detection, upcoming payments
-- Query pattern: WHERE user_id = ? AND due_date <= ?
CREATE INDEX IF NOT EXISTS idx_debts_user_due_date
ON debts(user_id, due_date);

-- ============================================================================
-- DEBT_PAYMENTS TABLE INDEXES
-- ============================================================================

-- Index for payment history queries
-- Used by: Payment history reports, debt analysis
-- Query pattern: WHERE debt_id = ? AND paid_at >= ?
CREATE INDEX IF NOT EXISTS idx_debt_payments_debt_paid_at
ON debt_payments(debt_id, paid_at DESC);

-- ============================================================================
-- USERS TABLE INDEXES
-- ============================================================================

-- Email index for login queries (if not already unique index)
-- Used by: Authentication, user lookup by email
-- Note: UNIQUE constraint already creates an index, so this is redundant
-- Kept here for documentation purposes
-- CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ============================================================================
-- SUBSCRIPTIONS TABLE INDEXES
-- ============================================================================

-- Index for active subscription lookups
-- Used by: SubscriptionService.isPremiumUser()
-- Query pattern: WHERE user_id = ? AND status = 'ACTIVE'
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_status
ON subscriptions(user_id, status);

-- Index for subscription expiry checks
-- Used by: Background jobs checking expired subscriptions
-- Query pattern: WHERE status = 'ACTIVE' AND ended_at <= NOW()
CREATE INDEX IF NOT EXISTS idx_subscriptions_status_ended_at
ON subscriptions(status, ended_at);

-- ============================================================================
-- PERFORMANCE ANALYSIS
-- ============================================================================

-- Expected Performance Improvements:
-- 1. Report queries (getSummaryByDateRange): 50-80% faster
-- 2. Category breakdown queries: 60-90% faster
-- 3. Trend data queries: 70-95% faster
-- 4. Export operations: 40-70% faster
-- 5. Wallet balance calculations: 50-80% faster

-- Index Size Estimates (per 10,000 transactions):
-- idx_transactions_user_date: ~500KB
-- idx_transactions_user_date_type: ~600KB
-- idx_transactions_user_category_date: ~700KB
-- idx_transactions_user_wallet_date: ~700KB
-- Total estimated index overhead: ~2.5MB per 10,000 transactions

-- ============================================================================
-- MAINTENANCE NOTES
-- ============================================================================

-- 1. Indexes are automatically maintained by PostgreSQL
-- 2. VACUUM ANALYZE should be run periodically for optimal performance
-- 3. Monitor index usage with:
--    SELECT * FROM pg_stat_user_indexes WHERE schemaname = 'public';
-- 4. Check for unused indexes:
--    SELECT * FROM pg_stat_user_indexes WHERE idx_scan = 0;
-- 5. Reindex if performance degrades:
--    REINDEX TABLE transactions;

-- ============================================================================
-- ROLLBACK SCRIPT (for reference)
-- ============================================================================

-- To rollback these indexes if needed:
-- DROP INDEX IF EXISTS idx_transactions_user_date;
-- DROP INDEX IF EXISTS idx_transactions_user_date_type;
-- DROP INDEX IF EXISTS idx_transactions_user_category_date;
-- DROP INDEX IF EXISTS idx_transactions_user_wallet_date;
-- DROP INDEX IF EXISTS idx_transactions_user_wallet_category_type;
-- DROP INDEX IF EXISTS idx_transactions_date;
-- DROP INDEX IF EXISTS idx_categories_user_type;
-- DROP INDEX IF EXISTS idx_wallets_user;
-- DROP INDEX IF EXISTS idx_debts_user_status;
-- DROP INDEX IF EXISTS idx_debts_user_due_date;
-- DROP INDEX IF EXISTS idx_debt_payments_debt_paid_at;
-- DROP INDEX IF EXISTS idx_subscriptions_user_status;
-- DROP INDEX IF EXISTS idx_subscriptions_status_ended_at;
