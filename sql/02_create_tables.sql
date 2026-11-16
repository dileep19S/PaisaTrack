-- ============================================================================
-- PaisaTrack - Personal Finance Tracker
-- Phase 2: Table Creation, Constraints, Relationships & Indexes
-- Target RDBMS: MySQL 8.x Community Edition
-- ============================================================================

USE paisatrack_db;

-- ----------------------------------------------------------------------------
-- 1. USERS TABLE
-- Stores registered user accounts. Passwords are stored only as BCrypt hashes.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id       BIGINT       NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_full_name CHECK (CHAR_LENGTH(TRIM(full_name)) > 0),
    CONSTRAINT chk_users_email CHECK (email LIKE '%_@_%.__%'),
    CONSTRAINT chk_users_password_hash CHECK (CHAR_LENGTH(password_hash) >= 20)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------------------------------------------------------
-- 2. CATEGORIES TABLE
-- Stores shared global INCOME and EXPENSE categories (not duplicated per user).
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    category_id   BIGINT      NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(60) NOT NULL,
    category_type VARCHAR(10) NOT NULL,

    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT uq_categories_name_type UNIQUE (category_name, category_type),
    CONSTRAINT chk_categories_name CHECK (CHAR_LENGTH(TRIM(category_name)) > 0),
    CONSTRAINT chk_categories_type CHECK (category_type IN ('INCOME', 'EXPENSE')),
    INDEX idx_categories_type (category_type)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------------------------------------------------------
-- 3. TRANSACTIONS TABLE
-- Stores income and expense records belonging to a specific user and category.
-- Monetary amounts strictly use DECIMAL(12,2) for exact INR precision.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   BIGINT        NOT NULL AUTO_INCREMENT,
    user_id          BIGINT        NOT NULL,
    category_id      BIGINT        NOT NULL,
    transaction_type VARCHAR(10)   NOT NULL,
    amount           DECIMAL(12,2) NOT NULL,
    description      VARCHAR(255)  NULL,
    transaction_date DATE          NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_transactions PRIMARY KEY (transaction_id),
    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_transactions_type CHECK (transaction_type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT chk_transactions_amount CHECK (amount > 0.00),

    INDEX idx_transactions_user_date (user_id, transaction_date DESC),
    INDEX idx_transactions_user_type_date (user_id, transaction_type, transaction_date DESC),
    INDEX idx_transactions_category (category_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
