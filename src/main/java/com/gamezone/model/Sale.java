
package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a sale transaction registered in the store.
 * A sale is associated with a customer, a seller, and the list
 * of products purchased, and is responsible for calculating
 * its own total.
 */
public class Sale {

    private String id;
    private LocalDate date;
    private Customer customer;
    private Seller seller;
    private List<Product> products;
    private double total;
    private String appliedPromotionName;
    private double discountAmount;

    /**
     * Creates a new Sale with no products yet. Products must be
     * added with {@link #addProduct(Product)} before the sale
     * can be considered valid, since a sale must contain at
     * least one product.
     *
     * @param id       the unique identifier of the sale
     * @param date     the date the sale was made
     * @param customer the customer who made the purchase
     * @param seller   the seller who attended the sale
     * @throws IllegalArgumentException if id, date, customer or seller is null
     */
    public Sale(String id, LocalDate date, Customer customer, Seller seller) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("The sale id cannot be empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("The sale date cannot be null");
        }
        if (customer == null) {
            throw new IllegalArgumentException("A sale must have a customer");
        }
        if (seller == null) {
            throw new IllegalArgumentException("A sale must have a seller");
        }
        this.id = id;
        this.date = date;
        this.customer = customer;
        this.seller = seller;
        this.products = new ArrayList<>();
        this.total = 0.0;
        this.appliedPromotionName = null;
        this.discountAmount = 0.0;
    }

    /**
     * Adds a product to this sale and recalculates the total.
     *
     * @param product the product purchased
     * @throws IllegalArgumentException if the product is null
     */
    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Cannot add a null product to a sale");
        }
        products.add(product);
        calculateTotal();
    }

    /**
     * Calculates and stores the sale's total by summing the
     * price of every product purchased.
     *
     * @return the calculated total
     */
    public double calculateTotal() {
        double sum = 0.0;
        for (Product product : products) {
            sum += product.getPrice();
        }
        this.total = sum;
        return total;
    }

    /**
     * Returns the sale's unique identifier.
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the date the sale was made.
     * @return the date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the customer who made the purchase.
     * @return the customer
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Returns the seller who attended the sale.
     * @return the seller
     */
    public Seller getSeller() {
        return seller;
    }

    /**
     * Returns the list of products purchased in this sale.
     * @return an unmodifiable view of the products list
     */
    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }

    /**
     * Returns the total amount of the sale.
     * @return the total
     */
        public double getTotal() {
        return total;
    }

    /**
     * Checks whether this sale is still within the 30-calendar-day
     * window during which a return can be registered.
     *
     * @return true if the current date is within 30 days of the sale's date
     */
    public boolean canBeReturned() {
        long daysSinceSale = java.time.temporal.ChronoUnit.DAYS.between(date, LocalDate.now());
        return daysSinceSale <= 30;
    }
    
     /**
     * Applies a promotion's discount to this sale. A sale can have at
     * most one applied promotion at a time; calling this again replaces
     * the previously applied one.
     *
     * @param promotionName the name of the promotion being applied
     * @param discountAmount the discount amount in currency granted by the promotion
     * @throws IllegalArgumentException if the discount amount is negative
     */
    public void applyPromotion(String promotionName, double discountAmount) {
        if (discountAmount < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        this.appliedPromotionName = promotionName;
        this.discountAmount = discountAmount;
    }

    /**
     * Returns the name of the promotion applied to this sale, if any.
     * @return the applied promotion's name, or null if no promotion was applied
     */
    public String getAppliedPromotionName() {
        return appliedPromotionName;
    }

    /**
     * Returns the discount amount granted by the applied promotion.
     * @return the discount amount, or 0.0 if no promotion was applied
     */
    public double getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Returns the final amount to charge for this sale, after subtracting
     * the applied discount (if any) from the subtotal.
     * @return the subtotal minus the discount amount
     */
    public double getFinalTotal() {
        return total - discountAmount;
    }

    /**
     * Builds a readable receipt for this sale, showing the subtotal,
     * the discount applied (if any), and the final total.
     *
     * @return a formatted, multi-line receipt
     */
    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Recibo de venta ").append(id).append("\n");
        receipt.append("Fecha: ").append(date).append("\n");
        receipt.append("Cliente: ").append(customer.getName()).append("\n");
        receipt.append("Vendedor: ").append(seller.getName()).append("\n");
        receipt.append("Productos: ").append(products.size()).append("\n");
        receipt.append("Subtotal: ").append(total).append("\n");
        if (appliedPromotionName != null) {
            receipt.append("Descuento aplicado (").append(appliedPromotionName)
                    .append("): -").append(discountAmount).append("\n");
        } else {
            receipt.append("Descuento aplicado: ninguno\n");
        }
        receipt.append("Total final: ").append(getFinalTotal());
        return receipt.toString();
    }

    /**
     * Returns a readable representation of this sale.
     * @return a formatted string with the sale's data
     */
    @Override
    public String toString() {
        return "Sale{" + "id=" + id + ", date=" + date
                + ", customer=" + customer.getName()
                + ", seller=" + seller.getName()
                + ", products=" + products.size()
                + ", total=" + total + "}";
    }
}
