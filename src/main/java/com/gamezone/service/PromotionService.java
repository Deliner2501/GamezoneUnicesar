package com.gamezone.service;

import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Promotion;
import com.gamezone.persistence.PromotionDAO;
import com.gamezone.persistence.PromotionUsageLogger;
import java.time.LocalDate;
import java.util.List;
import com.gamezone.model.Sale;
import java.util.ArrayList;

/**
 * Contains the business rules for managing promotions and selecting
 * the best applicable promotion for a given sale.
 */
public class PromotionService {

    private PromotionDAO promotionDAO;
    private PromotionUsageLogger promotionUsageLogger;

    public PromotionService(PromotionDAO promotionDAO, PromotionUsageLogger promotionUsageLogger) {
        this.promotionDAO = promotionDAO;
        this.promotionUsageLogger = promotionUsageLogger;
    }

    /**
     * Registers a new percentage-based promotion and persists it.
     *
     * @param id         the unique id of the promotion
     * @param name       the display name of the promotion
     * @param startDate  the date the promotion becomes active
     * @param endDate    the date the promotion stops being active
     * @param percentage the discount percentage applied to the sale's total
     * @return the registered promotion
     */
    public PercentageDiscount registerPercentageDiscount(String id, String name, LocalDate startDate,
                                                      LocalDate endDate, double percentage) {
        validatePercentage(percentage);
        PercentageDiscount promotion = new PercentageDiscount(id, name, startDate, endDate, percentage);
        List<Promotion> promotions = promotionDAO.loadAll();
        promotions.add(promotion);
        promotionDAO.saveAll(promotions);
        return promotion;
    }

    /**
     * Registers a new category-based promotion and persists it.
     *
     * @param id             the unique id of the promotion
     * @param name           the display name of the promotion
     * @param startDate      the date the promotion becomes active
     * @param endDate        the date the promotion stops being active
     * @param percentage     the discount percentage applied to the target category
     * @param targetCategory the category this promotion applies to ("VIDEOGAME", "CONSOLE" or "ACCESSORY")
     * @return the registered promotion
     */
    public CategoryDiscount registerCategoryDiscount(String id, String name, LocalDate startDate,
                                                      LocalDate endDate, double percentage,
                                                      String targetCategory) {
        validatePercentage(percentage);
        validateTargetCategory(targetCategory);
        CategoryDiscount promotion = new CategoryDiscount(id, name, startDate, endDate, percentage, targetCategory);
        List<Promotion> promotions = promotionDAO.loadAll();
        promotions.add(promotion);
        promotionDAO.saveAll(promotions);
        return promotion;
    }

    /**
     * Registers a new bulk-purchase promotion and persists it.
     *
     * @param id          the unique id of the promotion
     * @param name        the display name of the promotion
     * @param startDate   the date the promotion becomes active
     * @param endDate     the date the promotion stops being active
     * @param minQuantity the minimum number of products required for the promotion to apply
     * @param percentage  the discount percentage applied to the sale's total
     * @return the registered promotion
     */
    public BulkPurchaseDiscount registerBulkPurchaseDiscount(String id, String name, LocalDate startDate,
                                                              LocalDate endDate, int minQuantity,
                                                              double percentage) {
        validatePercentage(percentage);
        BulkPurchaseDiscount promotion = new BulkPurchaseDiscount(id, name, startDate, endDate, minQuantity, percentage);
        List<Promotion> promotions = promotionDAO.loadAll();
        promotions.add(promotion);
        promotionDAO.saveAll(promotions);
        return promotion;
    }

    /**
     * Returns the list of all registered promotions.
     *
     * @return the list of all promotions
     */
    public List<Promotion> listAllPromotions() {
        return promotionDAO.loadAll();
    }

    /**
     * Returns the list of promotions that are currently active,
     * based on today's date.
     *
     * @return the list of active promotions
     */
    public List<Promotion> listActivePromotions() {
        List<Promotion> active = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Promotion p : promotionDAO.loadAll()) {
            if (p.isActive(today)) {
                active.add(p);
            }
        }
        return active;
    }

    /**
     * Finds, among the currently active promotions, the one that would
     * grant the largest monetary discount to the given sale.
     *
     * @param sale the sale to evaluate
     * @return the best applicable promotion, or null if no active
     *         promotion applies or the maximum discount is zero
     */
    public Promotion findBestPromotionFor(Sale sale) {
        Promotion best = null;
        double bestDiscount = 0.0;

        for (Promotion p : listActivePromotions()) {
            double discount = p.calculateDiscount(sale);
            if (discount > bestDiscount) {
                bestDiscount = discount;
                best = p;
            }
        }

        if (best != null) {
            promotionUsageLogger.log(sale, best, bestDiscount);
        }

        return best;
    }

    /**
     * Finds a promotion by its id.
     *
     * @param id the id of the promotion to find
     * @return the matching promotion, or null if none is found
     */
    public Promotion findById(String id) {
        for (Promotion p : promotionDAO.loadAll()) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    private void validatePercentage(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
    }

    private void validateTargetCategory(String targetCategory) {
        if (targetCategory == null
                || !(targetCategory.equalsIgnoreCase("VIDEOGAME")
                     || targetCategory.equalsIgnoreCase("CONSOLE")
                     || targetCategory.equalsIgnoreCase("ACCESSORY"))) {
            throw new IllegalArgumentException(
                    "Target category must be VIDEOGAME, CONSOLE or ACCESSORY");
        }
    }
}