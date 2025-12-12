-- V4: Add Payment Integration Tables and Indexes
-- Description: Create payment_transactions table for Midtrans integration and enhance subscriptions table
-- Author: Fajar Sudarmaji
-- Date: 2025-12-06

-- ========================================
-- 1. CREATE PAYMENT_TRANSACTIONS TABLE
-- ========================================
CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscription_id UUID REFERENCES subscriptions(id) ON DELETE SET NULL,

    -- Order & Transaction IDs
    order_id VARCHAR(255) UNIQUE NOT NULL,
    transaction_id VARCHAR(255),

    -- Payment Details
    amount NUMERIC(15, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    payment_method VARCHAR(50),

    -- Status & Provider
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'EXPIRED', 'CANCELLED')),
    provider VARCHAR(20) NOT NULL DEFAULT 'MIDTRANS',

    -- Midtrans Snap Integration
    snap_token TEXT,
    snap_redirect_url TEXT,

    -- Webhook & Metadata
    webhook_payload JSONB,
    metadata JSONB,
    idempotency_key VARCHAR(255) UNIQUE,

    -- Timestamps
    paid_at TIMESTAMP,
    expired_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- 2. CREATE INDEXES FOR PAYMENT_TRANSACTIONS
-- ========================================

-- Index for user payment history queries
CREATE INDEX idx_payment_user ON payment_transactions(user_id);

-- Index for order lookup (webhook verification)
CREATE INDEX idx_payment_order ON payment_transactions(order_id);

-- Index for status filtering and monitoring
CREATE INDEX idx_payment_status ON payment_transactions(status);

-- Index for chronological queries (recent payments)
CREATE INDEX idx_payment_created ON payment_transactions(created_at DESC);

-- Index for subscription-based queries
CREATE INDEX idx_payment_subscription ON payment_transactions(subscription_id) WHERE subscription_id IS NOT NULL;

-- Index for idempotency checks
CREATE INDEX idx_payment_idempotency ON payment_transactions(idempotency_key) WHERE idempotency_key IS NOT NULL;

-- ========================================
-- 3. ENHANCE SUBSCRIPTIONS TABLE INDEXES
-- ========================================

-- Index for user subscription lookup (frequently used)
CREATE INDEX IF NOT EXISTS idx_subscription_user_status ON subscriptions(user_id, status);

-- Index for subscription expiry monitoring
CREATE INDEX IF NOT EXISTS idx_subscription_ended ON subscriptions(ended_at) WHERE ended_at IS NOT NULL;

-- ========================================
-- 4. ADD COMMENTS FOR DOCUMENTATION
-- ========================================

COMMENT ON TABLE payment_transactions IS 'Stores all payment transactions from Midtrans integration';
COMMENT ON COLUMN payment_transactions.order_id IS 'Unique order identifier sent to Midtrans (format: ORDER-{userId}-{timestamp})';
COMMENT ON COLUMN payment_transactions.transaction_id IS 'Midtrans transaction ID received from webhook';
COMMENT ON COLUMN payment_transactions.snap_token IS 'Midtrans Snap token for payment page';
COMMENT ON COLUMN payment_transactions.snap_redirect_url IS 'Midtrans Snap redirect URL for payment page';
COMMENT ON COLUMN payment_transactions.webhook_payload IS 'Full webhook payload from Midtrans for audit trail';
COMMENT ON COLUMN payment_transactions.idempotency_key IS 'Client-provided key to prevent duplicate payment creation';
COMMENT ON COLUMN payment_transactions.metadata IS 'Additional metadata (e.g., device info, IP address, campaign)';
COMMENT ON COLUMN payment_transactions.paid_at IS 'Timestamp when payment was successfully completed';
COMMENT ON COLUMN payment_transactions.expired_at IS 'Timestamp when payment link expired';

-- ========================================
-- 5. CREATE TRIGGER FOR UPDATED_AT
-- ========================================

CREATE OR REPLACE FUNCTION update_payment_transactions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_payment_transactions_updated_at
    BEFORE UPDATE ON payment_transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_payment_transactions_updated_at();

-- ========================================
-- 6. ROLLBACK INSTRUCTIONS (For Emergency)
-- ========================================
-- To rollback this migration manually:
-- DROP TRIGGER IF EXISTS trigger_update_payment_transactions_updated_at ON payment_transactions;
-- DROP FUNCTION IF EXISTS update_payment_transactions_updated_at();
-- DROP INDEX IF EXISTS idx_payment_user;
-- DROP INDEX IF EXISTS idx_payment_order;
-- DROP INDEX IF EXISTS idx_payment_status;
-- DROP INDEX IF EXISTS idx_payment_created;
-- DROP INDEX IF EXISTS idx_payment_subscription;
-- DROP INDEX IF EXISTS idx_payment_idempotency;
-- DROP INDEX IF EXISTS idx_subscription_user_status;
-- DROP INDEX IF EXISTS idx_subscription_ended;
-- DROP TABLE IF EXISTS payment_transactions;
