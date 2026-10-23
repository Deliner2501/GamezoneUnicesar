package com.gamezone.persistence;

import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Promotion;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

/**
 * Converts promotions to and from their CSV line. It owns the storage
 * format in both directions, with a registry that maps each type code to
 * its parser (SOLID audit finding V-08).
 */
public class PromotionCsvMapper {

    private final Map<String, Function<String[], Promotion>> parsers = Map.of(
            "PERCENTAGE", this::parsePercentage,
            "CATEGORY", this::parseCategory,
            "BULK", this::parseBulk);

    /**
     * Builds the CSV line of a promotion.
     *
     * @param promotion the promotion to convert
     * @return the CSV line
     */
    public String toCsvLine(Promotion promotion) {
        String common = promotion.getId() + "," + promotion.getName() + ","
                + promotion.getStartDate() + "," + promotion.getEndDate() + ",";
        if (promotion instanceof PercentageDiscount pd) {
            return "PERCENTAGE," + common + pd.getPercentage();
        } else if (promotion instanceof CategoryDiscount cd) {
            return "CATEGORY," + common + cd.getPercentage() + "," + cd.getTargetCategory();
        } else if (promotion instanceof BulkPurchaseDiscount bd) {
            return "BULK," + common + bd.getMinQuantity() + "," + bd.getPercentage();
        }
        throw new IllegalArgumentException("Unsupported promotion type");
    }

    /**
     * Builds a promotion from a CSV line.
     *
     * @param line the CSV line
     * @return the promotion, or null if the type code is unknown
     */
    public Promotion parseLine(String line) {
        String[] parts = line.split(",", -1);
        Function<String[], Promotion> parser = parsers.get(parts[0]);
        return parser == null ? null : parser.apply(parts);
    }

    private Promotion parsePercentage(String[] p) {
        return new PercentageDiscount(p[1], p[2], LocalDate.parse(p[3]), LocalDate.parse(p[4]),
                Double.parseDouble(p[5]));
    }

    private Promotion parseCategory(String[] p) {
        return new CategoryDiscount(p[1], p[2], LocalDate.parse(p[3]), LocalDate.parse(p[4]),
                Double.parseDouble(p[5]), p[6]);
    }

    private Promotion parseBulk(String[] p) {
        return new BulkPurchaseDiscount(p[1], p[2], LocalDate.parse(p[3]), LocalDate.parse(p[4]),
                Integer.parseInt(p[5]), Double.parseDouble(p[6]));
    }
}