package com.gamezone.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer of the store.
 * A customer can purchase products and has a purchase history.
 */
public class Customer extends Person {

    private String email;
    private List<Sale> purchaseHistory;

    /**
     * Creates a new Customer with the given information.
     *
     * @param name  the customer's full name
     * @param id    the customer's identification number
     * @param phone the customer's contact phone number
     * @param email the customer's email address
     */
    public Customer(String name, String id, String phone, String email) {
        super(name, id, phone);
        this.email = email;
        this.purchaseHistory = new ArrayList<>();
    }

    /**
     * Returns the customer's email address.
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the list of sales made by this customer.
     * @return the purchase history
     */
    public List<Sale> getPurchaseHistory() {
        return purchaseHistory;
    }

    /**
     * Adds a new sale to the customer's purchase history.
     * @param sale the sale to add
     */
    public void addPurchase(Sale sale) {
        this.purchaseHistory.add(sale);
    }

    /**
     * Returns a readable representation of this customer, including email.
     * @return a formatted string with the customer's data
     */
    @Override
    public String toString() {
        return super.toString() + ", Email: " + email;
    }
}