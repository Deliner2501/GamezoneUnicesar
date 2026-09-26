package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents a promotion that applies a percentage discount only
 * to the products of a specific category within a sale.
 */
public class CategoryDiscount extends Promotion {

    private double percentage;
    private String targetCategory;

    public CategoryDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                             double percentage, String targetCategory) {
        super(id, name, startDate, endDate);
        this.percentage = percentage;
        this.targetCategory = targetCategory;
    }

    /**
     * Returns the discount percentage applied to the target category.
     *
     * @return the percentage (0 to 100)
     */
    public double getPercentage() { return percentage; }

    /**
     * Sets the discount percentage applied to the target category.
     *
     * @param percentage the percentage to set (0 to 100)
     */
    public void setPercentage(double percentage) { this.percentage = percentage; }

    /**
     * Returns the category this promotion applies to.
     *
     * @return the target category ("VIDEOGAME" or "CONSOLE")
     */
    public String getTargetCategory() { return targetCategory; }

    /**
     * Sets the category this promotion applies to.
     *
     * @param targetCategory the target category to set
     */
    public void setTargetCategory(String targetCategory) { this.targetCategory = targetCategory; }

    /**
     * Calculates the discount by summing only the prices of the
     * products in the sale that belong to the target category,
     * and applying this promotion's percentage to that subtotal.
     *
     * @param sale the sale to evaluate
     * @return the discount amount in currency
     */
    @Override
    public double calculateDiscount(Sale sale) {
        double categorySubtotal = 0.0;
        for (Product product : sale.getProducts()) {
            if (matchesCategory(product)) {
                categorySubtotal += product.getPrice();
            }
        }
        return categorySubtotal * (percentage / 100.0);
    }

    private boolean matchesCategory(Product product) {
        if (targetCategory.equalsIgnoreCase("VIDEOGAME")) {
            return product instanceof VideoGame;
        } else if (targetCategory.equalsIgnoreCase("CONSOLE")) {
            return product instanceof Console;
        }
        return false;
    }
}