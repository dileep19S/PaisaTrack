-- ============================================================================
-- PaisaTrack - Personal Finance Tracker
-- Phase 2: Default Indian Income & Expense Categories Seed Script
-- Target RDBMS: MySQL 8.x Community Edition
-- ============================================================================

USE paisatrack_db;

-- Default Expense Categories (8)
INSERT IGNORE INTO categories (category_name, category_type)
VALUES
    ('Food', 'EXPENSE'),
    ('Travel', 'EXPENSE'),
    ('Shopping', 'EXPENSE'),
    ('Education', 'EXPENSE'),
    ('Bills', 'EXPENSE'),
    ('Health', 'EXPENSE'),
    ('Entertainment', 'EXPENSE'),
    ('Others', 'EXPENSE');

-- Default Income Categories (5)
INSERT IGNORE INTO categories (category_name, category_type)
VALUES
    ('Salary', 'INCOME'),
    ('Pocket Money', 'INCOME'),
    ('Freelance', 'INCOME'),
    ('Business', 'INCOME'),
    ('Others', 'INCOME');
