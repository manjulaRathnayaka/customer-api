package com.enterprise.rewards.service;

import java.util.Collection;
import java.util.List;

import com.enterprise.rewards.model.Customer;
import com.enterprise.rewards.model.PointsTransaction;
import com.enterprise.rewards.model.Reward;

/**
 * Interface for data service operations
 * Supports database implementation
 * Legacy 2015 design pattern
 */
public interface DataServiceInterface {

    // Reward operations
    Reward getReward(String rewardId);
    void saveReward(Reward reward);
    Collection<Reward> getAllRewards();
    /**
     * Creates a new customer with the given details.
     */
    Customer createCustomer(String firstName, String lastName, String emailAddress, String phoneNumber);
    /**
     * Returns a transaction by transactionId, or null if not found.
     */
    PointsTransaction getTransaction(String transactionId);

    /**
     * Returns all transactions in the system.
     */
    List<PointsTransaction> getAllTransactions();

    /**
     * Returns all transactions for a given customer.
     */
    List<PointsTransaction> getTransactionsByCustomer(String customerId);

    /**
     * Creates a new transaction.
     */
    PointsTransaction createTransaction(String customerId, String transactionType, int pointsAmount, String description);
    /**
     * Returns all customers in the system.
     */
    Collection<Customer> getAllCustomers();

    /**
     * Returns a customer by customerId, or null if not found.
     */
    Customer getCustomer(String customerId);
}
