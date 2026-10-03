package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents a promotion that applies a flat percentage discount
 * over the total amount of a sale.
 */
public class PercentageDiscount extends Promotion {

    private double percentage;

    public PercentageDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                               double percentage) {
        super(id, name, startDate, endDate);
        this.percentage = percentage;
    }

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
     * to the total amount of the sale.
     *
     * @param sale the sale to evaluate
     * @return the discount amount in currency
     */
    @Override
    public double calculateDiscount(Sale sale) {
        return sale.calculateTotal() * (percentage / 100.0);
    }
    
    
    @Override
    public String getPromotionDetail() {
        return getPercentage() + "% de descuento";
    }
}