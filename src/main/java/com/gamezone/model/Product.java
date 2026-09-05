package com.gamezone.model;

/**
 * Abstract base class representing a generic product sold at the store.
 */
public abstract class Product {

    private String id;
    private String title;
    private double price;
    private int stock;

    public Product(String id, String title, double price, int stock) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.stock = stock;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    /**
     * Reduces the stock by the given quantity.
     *
     * @param quantity the amount to subtract from the current stock
     * @throws IllegalArgumentException if the quantity exceeds the current stock
     */
    public void reduceStock(int quantity) {
        if (quantity > this.stock) {
            throw new IllegalArgumentException("Insufficient stock for product: " + id);
        }
        this.stock -= quantity;
    }

    /**
     * Returns a full description integrating this product's specific
     * characteristics. Each subclass must implement this on its own.
     *
     * @return the full description of the product
     */
    public abstract String getFullDescription();
}