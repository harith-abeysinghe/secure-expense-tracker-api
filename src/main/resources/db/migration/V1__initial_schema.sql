CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(72) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE UNIQUE INDEX uk_users_email_lower ON users (LOWER(email));

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    family_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_hash VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens(family_id);

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    owner_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(80) NOT NULL,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE UNIQUE INDEX uk_categories_global_name ON categories (LOWER(name)) WHERE owner_id IS NULL;
CREATE UNIQUE INDEX uk_categories_owner_name ON categories (owner_id, LOWER(name)) WHERE owner_id IS NOT NULL;

CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id),
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    expense_date DATE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date DESC, created_at DESC);
CREATE INDEX idx_expenses_user_category ON expenses(user_id, category_id);

CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id),
    budget_year INTEGER NOT NULL CHECK (budget_year BETWEEN 2000 AND 2200),
    budget_month INTEGER NOT NULL CHECK (budget_month BETWEEN 1 AND 12),
    amount NUMERIC(19,2) NOT NULL CHECK (amount >= 0),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_budget_user_category_month UNIQUE(user_id, category_id, budget_year, budget_month)
);
CREATE INDEX idx_budgets_user_month ON budgets(user_id, budget_year, budget_month);

INSERT INTO categories (id, owner_id, name, archived, created_at, updated_at) VALUES
('00000000-0000-0000-0000-000000000001', NULL, 'Groceries', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000002', NULL, 'Housing', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000003', NULL, 'Transport', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000004', NULL, 'Utilities', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000005', NULL, 'Healthcare', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000006', NULL, 'Entertainment', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000007', NULL, 'Other', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
