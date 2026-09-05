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

    public Customer(String name, String id, String phone, String email) {
        super(name, id, phone);
        this.email = email;
        this.purchaseHistory = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public List<Sale> getPurchaseHistory() {
        return purchaseHistory;
    }

    public void addPurchase(Sale sale) {
        this.purchaseHistory.add(sale);
    }
}
