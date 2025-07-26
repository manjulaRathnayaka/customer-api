package com.enterprise.rewards.model;

/**
 * Address POJO for customer address information
 */
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    public Address() {
        this.country = "USA";
    }

    public Address(String street, String city, String state, String zipCode) {
        this();
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }

    // Getters and Setters
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
