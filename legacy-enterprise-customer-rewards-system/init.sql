-- Customer Loyalty System Database Schema
-- MySQL 8.0 compatible

-- Create database (already created by docker-compose environment variables)
USE loyalty_system;

-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email_address VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    registration_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    loyalty_tier ENUM('BRONZE', 'SILVER', 'GOLD', 'PLATINUM') DEFAULT 'BRONZE',
    total_lifetime_points INT DEFAULT 0,
    current_available_points INT DEFAULT 0,
    account_status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_id (customer_id),
    INDEX idx_email (email_address),
    INDEX idx_loyalty_tier (loyalty_tier),
    INDEX idx_account_status (account_status)
);

-- Create points_transactions table
CREATE TABLE IF NOT EXISTS points_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    transaction_type ENUM('EARN', 'REDEEM', 'EXPIRE', 'ADJUST') NOT NULL,
    points_amount INT NOT NULL,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    reference_order_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_transaction_date (transaction_date)
);

-- Create rewards table
CREATE TABLE IF NOT EXISTS rewards (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reward_id VARCHAR(50) UNIQUE NOT NULL,
    reward_name VARCHAR(255) NOT NULL,
    description TEXT,
    points_required INT NOT NULL,
    category ENUM('MERCHANDISE', 'DISCOUNT', 'EXPERIENCE', 'GIFT_CARD') DEFAULT 'MERCHANDISE',
    is_active BOOLEAN DEFAULT TRUE,
    stock_quantity INT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reward_id (reward_id),
    INDEX idx_points_required (points_required),
    INDEX idx_category (category),
    INDEX idx_is_active (is_active)
);

-- Insert sample data
INSERT INTO customers (customer_id, first_name, last_name, email_address, phone_number, loyalty_tier, total_lifetime_points, current_available_points)
VALUES
    ('CUST001234', 'Sarah', 'Johnson', 'sarah.johnson@email.com', '555-0123', 'GOLD', 2900, 600),
    ('CUST001235', 'Mike', 'Davis', 'mike.davis@email.com', '555-0124', 'SILVER', 1300, 300)
ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    email_address = VALUES(email_address);

-- Insert sample transactions
INSERT INTO points_transactions (transaction_id, customer_id, transaction_type, points_amount, description, reference_order_id)
VALUES
    ('TXN789012', 'CUST001234', 'EARN', 150, 'Purchase reward points', 'ORD123456'),
    ('TXN789013', 'CUST001235', 'EARN', 75, 'Purchase reward points', 'ORD123457')
ON DUPLICATE KEY UPDATE
    description = VALUES(description);

-- Insert sample rewards
INSERT INTO rewards (reward_id, reward_name, description, points_required, category, stock_quantity)
VALUES
    ('REW301', 'Customer Loyalty Premium Jacket', 'High-quality waterproof jacket', 500, 'MERCHANDISE', 50),
    ('REW302', '$10 Store Credit', 'Discount voucher for next purchase', 100, 'DISCOUNT', NULL),
    ('REW303', 'VIP Customer Event', 'Exclusive access to seasonal preview', 1000, 'EXPERIENCE', 25)
ON DUPLICATE KEY UPDATE
    reward_name = VALUES(reward_name),
    description = VALUES(description),
    points_required = VALUES(points_required);
