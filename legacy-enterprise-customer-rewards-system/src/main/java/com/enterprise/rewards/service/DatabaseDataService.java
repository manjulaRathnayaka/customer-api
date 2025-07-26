
package com.enterprise.rewards.service;

import com.enterprise.rewards.model.Customer;
import java.sql.*;
import java.util.*;

/**
 * Database implementation of data service for customer loyalty app data
 * Uses MySQL database for production-ready persistence
 */
public class DatabaseDataService implements DataServiceInterface {

    // Allow instantiation with or without ServletContext for compatibility
    public DatabaseDataService() {}

    public DatabaseDataService(javax.servlet.ServletContext context) {
        // Optionally, load config from context if needed in future
    }

    @Override
    public com.enterprise.rewards.model.Reward getReward(String rewardId) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM rewards WHERE reward_id = ?");
            stmt.setString(1, rewardId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToReward(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting reward: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void saveReward(com.enterprise.rewards.model.Reward reward) {
        try (Connection conn = getConnection()) {
            // Try update first
            PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE rewards SET reward_name=?, points_required=?, reward_type=?, reward_value=?, availability_count=?, expiration_date=?, category=?, description=?, is_active=? WHERE reward_id=?");
            updateStmt.setString(1, reward.getRewardName());
            updateStmt.setInt(2, reward.getPointsRequired());
            updateStmt.setString(3, reward.getRewardType());
            updateStmt.setString(4, reward.getRewardValue());
            updateStmt.setInt(5, reward.getAvailabilityCount());
            java.util.Date exp = reward.getExpirationDate();
            if (exp != null) {
                updateStmt.setTimestamp(6, new java.sql.Timestamp(exp.getTime()));
            } else {
                updateStmt.setNull(6, java.sql.Types.TIMESTAMP);
            }
            updateStmt.setString(7, reward.getCategory());
            updateStmt.setString(8, reward.getDescription());
            updateStmt.setBoolean(9, reward.isActive());
            updateStmt.setString(10, reward.getRewardId());
            int updated = updateStmt.executeUpdate();
            if (updated == 0) {
                // Insert if not updated
                PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO rewards (reward_id, reward_name, points_required, reward_type, reward_value, availability_count, expiration_date, category, description, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                insertStmt.setString(1, reward.getRewardId());
                insertStmt.setString(2, reward.getRewardName());
                insertStmt.setInt(3, reward.getPointsRequired());
                insertStmt.setString(4, reward.getRewardType());
                insertStmt.setString(5, reward.getRewardValue());
                insertStmt.setInt(6, reward.getAvailabilityCount());
                java.util.Date exp2 = reward.getExpirationDate();
                if (exp2 != null) {
                    insertStmt.setTimestamp(7, new java.sql.Timestamp(exp2.getTime()));
                } else {
                    insertStmt.setNull(7, java.sql.Types.TIMESTAMP);
                }
                insertStmt.setString(8, reward.getCategory());
                insertStmt.setString(9, reward.getDescription());
                insertStmt.setBoolean(10, reward.isActive());
                insertStmt.executeUpdate();
                insertStmt.close();
            }
            updateStmt.close();
        } catch (SQLException e) {
            System.err.println("Error saving reward: " + e.getMessage());
        }
    }

    @Override
    public java.util.Collection<com.enterprise.rewards.model.Reward> getAllRewards() {
        java.util.List<com.enterprise.rewards.model.Reward> rewards = new java.util.ArrayList<>();
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM rewards ORDER BY reward_id");
            while (rs.next()) {
                rewards.add(mapRowToReward(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all rewards: " + e.getMessage());
        }
        return rewards;
    }

    private com.enterprise.rewards.model.Reward mapRowToReward(ResultSet rs) throws SQLException {
        com.enterprise.rewards.model.Reward reward = new com.enterprise.rewards.model.Reward();
        reward.setRewardId(rs.getString("reward_id"));
        reward.setRewardName(rs.getString("reward_name"));
        reward.setPointsRequired(rs.getInt("points_required"));
        reward.setRewardType(rs.getString("reward_type"));
        reward.setRewardValue(rs.getString("reward_value"));
        reward.setAvailabilityCount(rs.getInt("availability_count"));
        reward.setExpirationDate(rs.getTimestamp("expiration_date"));
        reward.setCategory(rs.getString("category"));
        reward.setDescription(rs.getString("description"));
        reward.setActive(rs.getBoolean("is_active"));
        return reward;
    }
    // ...existing code...

    @Override
    public Customer createCustomer(String firstName, String lastName, String emailAddress, String phoneNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // Generate a unique customerId (e.g., CUST + timestamp + random 3 digits)
            String customerId = generateCustomerId();
            String sql = "INSERT INTO customers (customer_id, first_name, last_name, email_address, phone_number, registration_date, loyalty_tier, total_lifetime_points, current_available_points, account_status) VALUES (?, ?, ?, ?, ?, NOW(), 'Bronze', 0, 0, 'Active')";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, customerId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, emailAddress);
            stmt.setString(5, phoneNumber);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating customer failed, no rows affected.");
            }
            // Fetch and return the new customer by customerId
            return getCustomer(customerId);
        } catch (Exception e) {
            throw new RuntimeException("Error creating customer: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignore) {}
            if (stmt != null) try { stmt.close(); } catch (Exception ignore) {}
            if (conn != null) try { conn.close(); } catch (Exception ignore) {}
        }
    }

    // Helper to generate a unique customerId
    private String generateCustomerId() {
        String prefix = "CUST";
        long timestamp = System.currentTimeMillis() % 1000000000L;
        int random = (int)(Math.random() * 900) + 100; // 3 random digits
        return prefix + String.format("%09d", timestamp) + random;
    }

    // Stub implementations for required interface methods (if not already present)
    @Override
    public java.util.List<com.enterprise.rewards.model.PointsTransaction> getAllTransactions() {
        java.util.List<com.enterprise.rewards.model.PointsTransaction> transactions = new java.util.ArrayList<>();
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM points_transactions ORDER BY transaction_id");
            while (rs.next()) {
                transactions.add(mapRowToPointsTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all transactions: " + e.getMessage());
        }
        return transactions;
    }
    // Helper to map a ResultSet row to PointsTransaction
    private com.enterprise.rewards.model.PointsTransaction mapRowToPointsTransaction(ResultSet rs) throws SQLException {
        com.enterprise.rewards.model.PointsTransaction tx = new com.enterprise.rewards.model.PointsTransaction();
        tx.setTransactionId(rs.getString("transaction_id"));
        tx.setCustomerId(rs.getString("customer_id"));
        tx.setTransactionType(rs.getString("transaction_type"));
        tx.setPointsAmount(rs.getInt("points_amount"));
        tx.setDescription(rs.getString("description"));
        tx.setTransactionDate(rs.getTimestamp("transaction_date"));
        return tx;
    }

    @Override
    public com.enterprise.rewards.model.PointsTransaction getTransaction(String transactionId) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM points_transactions WHERE transaction_id = ?");
            stmt.setString(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToPointsTransaction(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting transaction: " + e.getMessage());
        }
        return null;
    }

    @Override
    public java.util.List<com.enterprise.rewards.model.PointsTransaction> getTransactionsByCustomer(String customerId) {
        java.util.List<com.enterprise.rewards.model.PointsTransaction> transactions = new java.util.ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM points_transactions WHERE customer_id = ? ORDER BY transaction_date DESC");
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapRowToPointsTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting customer transactions: " + e.getMessage());
        }
        return transactions;
    }

    @Override
    public com.enterprise.rewards.model.PointsTransaction createTransaction(String customerId, String transactionType, int pointsAmount, String description) {
        String txnId = generateTransactionId();
        java.util.Date now = new java.util.Date();
        com.enterprise.rewards.model.PointsTransaction txn = new com.enterprise.rewards.model.PointsTransaction();
        txn.setTransactionId(txnId);
        txn.setCustomerId(customerId);
        txn.setTransactionType(transactionType);
        txn.setPointsAmount(pointsAmount);
        txn.setDescription(description);
        txn.setTransactionDate(now);
        if ("EARNED".equalsIgnoreCase(transactionType) || "EARN".equalsIgnoreCase(transactionType)) {
            long twoYearsInMillis = 62208000000L;
            txn.setExpirationDate(new java.util.Date(System.currentTimeMillis() + twoYearsInMillis));
        }
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO points_transactions (transaction_id, customer_id, transaction_type, points_amount, transaction_date, description) VALUES (?, ?, ?, ?, ?, ?)"
            );
            stmt.setString(1, txnId);
            stmt.setString(2, customerId);
            stmt.setString(3, transactionType);
            stmt.setInt(4, pointsAmount);
            stmt.setTimestamp(5, new java.sql.Timestamp(now.getTime()));
            stmt.setString(6, description);
            stmt.executeUpdate();
            stmt.close();
            conn.commit();
            System.out.println("Saved transaction: " + txnId);
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignore) {}
            }
            throw new RuntimeException("Error creating transaction: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignore) {}
            }
        }
        return txn;
    }
    // Helper to generate a transaction ID
    private String generateTransactionId() {
        return "TXN-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    @Override
    public Customer getCustomer(String customerId) {
        long startTime = System.currentTimeMillis();
        try {
            Connection conn = getConnection();
            try {
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT c.*, " +
                    "COALESCE(SUM(CASE WHEN pt.transaction_type IN ('EARN', 'EARNED', 'ADJUST', 'SOCIAL_MEDIA_BONUS') THEN pt.points_amount ELSE 0 END), 0) AS earned, " +
                    "COALESCE(SUM(CASE WHEN pt.transaction_type IN ('REDEEM', 'REDEEMED') THEN pt.points_amount ELSE 0 END), 0) AS redeemed " +
                    "FROM customers c " +
                    "LEFT JOIN points_transactions pt ON c.customer_id = pt.customer_id " +
                    "WHERE c.customer_id = ? " +
                    "GROUP BY c.customer_id"
                );
                stmt.setString(1, customerId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Customer customer = mapRowToCustomer(rs);
                    int earned = rs.getInt("earned");
                    int redeemed = rs.getInt("redeemed");
                    int available = earned - redeemed;
                    customer.setCurrentAvailablePoints(available);
                    customer.setTotalLifetimePoints(earned);
                    long endTime = System.currentTimeMillis();
                    System.out.println("[PERF] SQL getCustomer query took " + (endTime - startTime) + " ms");
                    return customer;
                }
                long endTime = System.currentTimeMillis();
                System.out.println("[PERF] SQL getCustomer query took " + (endTime - startTime) + " ms");
                return null;
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error getting customer: " + e.getMessage());
            return null;
        }
    }
    // DB credentials loaded from db.properties on the classpath
    private static String DB_HOST = "localhost";
    private static String DB_PORT = "3306";
    private static String DB_NAME = "loyalty_db";
    private static String DB_USER = "loyalty_user";
    private static String DB_PASSWORD = "password123";
    private static String DB_URL;

    static {
        try {
            Properties props = new Properties();
            java.io.InputStream in = DatabaseDataService.class.getClassLoader().getResourceAsStream("db.properties");
            if (in != null) {
                props.load(in);
                in.close();
                DB_HOST = props.getProperty("db.host", DB_HOST);
                DB_PORT = props.getProperty("db.port", DB_PORT);
                DB_NAME = props.getProperty("db.name", DB_NAME);
                DB_USER = props.getProperty("db.user", DB_USER);
                DB_PASSWORD = props.getProperty("db.password", DB_PASSWORD);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load db.properties: " + e.getMessage());
        }
        DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Database driver not found", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    @Override
    public Collection<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM customers ORDER BY customer_id");
            while (rs.next()) {
                Customer customer = mapRowToCustomer(rs);
                // Calculate points from transactions
                PreparedStatement sumStmt = conn.prepareStatement(
                    "SELECT SUM(CASE WHEN transaction_type IN ('EARN', 'EARNED', 'ADJUST', 'SOCIAL_MEDIA_BONUS') THEN points_amount ELSE 0 END) AS earned, " +
                    "SUM(CASE WHEN transaction_type IN ('REDEEM', 'REDEEMED') THEN points_amount ELSE 0 END) AS redeemed " +
                    "FROM points_transactions WHERE customer_id = ?");
                sumStmt.setString(1, customer.getCustomerId());
                ResultSet sumRs = sumStmt.executeQuery();
                int earned = 0;
                int redeemed = 0;
                if (sumRs.next()) {
                    earned = sumRs.getInt("earned");
                    redeemed = sumRs.getInt("redeemed");
                }
                int available = earned - redeemed;
                customer.setCurrentAvailablePoints(available);
                customer.setTotalLifetimePoints(earned);
                sumRs.close();
                sumStmt.close();
                customers.add(customer);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all customers: " + e.getMessage());
        }
        return customers;
    }

    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer(
            rs.getString("customer_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email_address"),
            rs.getString("phone_number")
        );
        customer.setRegistrationDate(rs.getTimestamp("registration_date"));
        customer.setLoyaltyTier(rs.getString("loyalty_tier"));
        customer.setAccountStatus(rs.getString("account_status"));
        // Points fields are set in getAllCustomers
        return customer;
    }
}
