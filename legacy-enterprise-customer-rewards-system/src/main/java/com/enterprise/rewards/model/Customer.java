package com.enterprise.rewards.model;

import java.util.Date;

/**
 * Customer POJO for in-memory storage
 * Legacy 2015 implementation using basic Java types
 */
public class Customer {
    private String customerId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private Date registrationDate;
    private String loyaltyTier;
    private int totalLifetimePoints;
    private int currentAvailablePoints;
    private Address address;
    private String accountStatus;

    // Default constructor
    public Customer() {
        this.registrationDate = new Date();
        this.loyaltyTier = "BRONZE";
        this.totalLifetimePoints = 0;
        this.currentAvailablePoints = 0;
        this.accountStatus = "ACTIVE";
    }

    // Parameterized constructor
    public Customer(String customerId, String firstName, String lastName, String emailAddress) {
        this();
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
    }

    // Constructor with phone number
    public Customer(String customerId, String firstName, String lastName, String emailAddress, String phoneNumber) {
        this(customerId, firstName, lastName, emailAddress);
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getLoyaltyTier() {
        return loyaltyTier;
    }

    public void setLoyaltyTier(String loyaltyTier) {
        this.loyaltyTier = loyaltyTier;
    }

    public int getTotalLifetimePoints() {
        return totalLifetimePoints;
    }

    public void setTotalLifetimePoints(int totalLifetimePoints) {
        this.totalLifetimePoints = totalLifetimePoints;
    }

    public int getCurrentAvailablePoints() {
        return currentAvailablePoints;
    }

    public void setCurrentAvailablePoints(int currentAvailablePoints) {
        this.currentAvailablePoints = currentAvailablePoints;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}
