package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents a promotion that applies a percentage discount over
 * the total amount of a sale when the number of products purchased
 * meets or exceeds a minimum quantity.
 */
public class BulkPurchaseDiscount extends Promotion {

    private int minQuantity;
    private double percentage;

    public BulkPurchaseDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                                 int minQuantity, double percentage) {
        super(id, name, startDate, endDate);
        this.minQuantity = minQuantity;
        this.percentage = percentage;
    }

    /**
     * Returns the minimum number of products required for this
     * promotion to apply.
     *
     * @return the minimum quantity
     */
    public int getMinQuantity() { return minQuantity; }

    /**
     * Sets the minimum number of products required for this
     * promotion to apply.
     *
     * @param minQuantity the minimum quantity to set
     */
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }

    /**
     * Returns the discount percentage applied by this promotion.
     *
     * @return the percentage (0 to 100)
     */
    public double getPercentage() { return percentage; }

    /**
     * Sets the discount percentage applied by this promotion.
     *
     * @param percentage the percentage to set (0 to 100)
     */
    public void setPercentage(double percentage) { this.percentage = percentage; }

    /**
     * Calculates the discount by applying this promotion's percentage
     * to the sale's total, but only if the sale contains at least the
     * minimum required quantity of products.
     *
     * @param sale the sale to evaluate
     * @return the discount amount in currency, or zero if the minimum
     *         quantity requirement is not met
     */
    @Override
    public double calculateDiscount(Sale sale) {
        if (sale.getProducts().size() < minQuantity) {
            return 0.0;
        }
        return sale.calculateTotal() * (percentage / 100.0);
    }
    
    
    @Override
    public String getPromotionDetail() {
        return "descuento por volumen a partir de " + getMinQuantity() + " productos";
    }
}