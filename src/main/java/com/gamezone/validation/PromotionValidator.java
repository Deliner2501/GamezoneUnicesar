package com.gamezone.validation;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.ProductCategory;

/**
 * Groups the validation rules related to promotions. Services call
 * these methods before registering a promotion; each method throws
 * the custom exception that matches the kind of problem found.
 */
public class PromotionValidator {

    private PromotionValidator() {
        // Utility class: it must not be instantiated.
    }

    /**
     * Validates that a discount percentage is between 0 and 100 (inclusive).
     *
     * @param percentage the discount percentage to check
     * @throws InvalidDataException if the percentage is out of range
     */
    public static void validatePercentage(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new InvalidDataException("porcentaje", "debe estar entre 0 y 100");
        }
    }

    /**
     * Validates that the target category of a category discount is one
     * of the categories defined by {@link ProductCategory}
     * (case-insensitive).
     *
     * @param targetCategory the target category to check
     * @throws InvalidDataException if the category is null or not supported
     */
    public static void validateTargetCategory(String targetCategory) {
        if (ProductCategory.fromText(targetCategory) == null) {
            throw new InvalidDataException("categoría objetivo",
                    "debe ser VIDEOGAME, CONSOLE o ACCESSORY");
        }
    }
}