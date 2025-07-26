package com.enterprise.rewards.model;

import java.util.Date;

/**
 * Reward POJO for in-memory catalog storage
 */
public class Reward {
    private String rewardId;
    private String rewardName;
    private int pointsRequired;
    private String rewardType;
    private String rewardValue;
    private int availabilityCount;
    private Date expirationDate;
    private String category;
    private String description;
    private boolean isActive;

    public Reward() {
        this.isActive = true;
        this.availabilityCount = 100;
    }

    public Reward(String rewardId, String rewardName, int pointsRequired, String rewardType, String rewardValue) {
        this();
        this.rewardId = rewardId;
        this.rewardName = rewardName;
        this.pointsRequired = pointsRequired;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
    }

    // Getters and Setters
    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public String getRewardValue() {
        return rewardValue;
    }

    public void setRewardValue(String rewardValue) {
        this.rewardValue = rewardValue;
    }

    public int getAvailabilityCount() {
        return availabilityCount;
    }

    public void setAvailabilityCount(int availabilityCount) {
        this.availabilityCount = availabilityCount;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
