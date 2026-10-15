package com.gamezone.model;

/**
 * Categories a product can belong to. Used by promotions that target
 * a specific kind of product.
 */
public enum ProductCategory {
    VIDEOGAME,
    CONSOLE,
    ACCESSORY;

    /**
     * Finds the category whose name matches the given text, ignoring case.
     *
     * @param text the category name to look up
     * @return the matching category, or null if there is none
     */
    public static ProductCategory fromText(String text) {
        for (ProductCategory category : values()) {
            if (category.name().equalsIgnoreCase(text)) {
                return category;
            }
        }
        return null;
    }
}