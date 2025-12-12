-- Add type column to debts table
ALTER TABLE debts ADD COLUMN type VARCHAR(20);

-- Make type column NOT NULL
ALTER TABLE debts ALTER COLUMN type SET NOT NULL;

-- Add note column to debts table
ALTER TABLE debts ADD COLUMN note VARCHAR(500);

-- Update due_date column to use TIMESTAMP instead of DATE (for consistency with entity)
ALTER TABLE debts ALTER COLUMN due_date TYPE TIMESTAMP;

-- Make due_date column NOT NULL
ALTER TABLE debts ALTER COLUMN due_date SET NOT NULL;

-- Add index on type for better query performance
CREATE INDEX idx_debt_type ON debts(type);

-- Add index on user_id for better query performance (if not already exists)
CREATE INDEX IF NOT EXISTS idx_debt_user_id ON debts(user_id);

-- Add index on status for better query performance (if not already exists)
CREATE INDEX IF NOT EXISTS idx_debt_status ON debts(status);

-- Add index on due_date for better query performance (if not already exists)
CREATE INDEX IF NOT EXISTS idx_debt_due_date ON debts(due_date);
