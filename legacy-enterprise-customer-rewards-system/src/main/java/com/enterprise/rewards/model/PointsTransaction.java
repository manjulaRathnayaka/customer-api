package com.enterprise.rewards.model;

import java.util.Date;

/**
 * Points Transaction POJO for in-memory storage
 */
public class PointsTransaction {
    private String transactionId;
    private String customerId;
    private String transactionType;
    private int pointsAmount;
    private Date transactionDate;
    private Date expirationDate;
    private String relatedOrderId;
    private String description;
    private String createdBy;
    private String status;

    public PointsTransaction() {
        this.transactionDate = new Date();
        this.createdBy = "SYSTEM";
        this.status = "COMPLETED";
    }

    public PointsTransaction(String transactionId, String customerId, String transactionType, int pointsAmount) {
        this();
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.transactionType = transactionType;
        this.pointsAmount = pointsAmount;

        // Set expiration date to 24 months from now
        if ("EARNED".equals(transactionType)) {
            long twoYearsInMillis = 24L * 30L * 24L * 60L * 60L * 1000L; // Approximate 24 months
            this.expirationDate = new Date(System.currentTimeMillis() + twoYearsInMillis);
        }
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public int getPointsAmount() {
        return pointsAmount;
    }

    public void setPointsAmount(int pointsAmount) {
        this.pointsAmount = pointsAmount;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(String relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
