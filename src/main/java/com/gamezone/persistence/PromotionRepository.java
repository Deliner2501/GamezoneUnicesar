package com.gamezone.persistence;

import com.gamezone.model.Promotion;
import java.util.List;

/**
 * Defines the storage operations the service layer needs for promotions.
 * The service depends on this abstraction instead of on a concrete
 * storage class (SOLID audit finding V-25).
 */
public interface PromotionRepository {

    /**
     * Persists the full list of promotions, replacing the stored content.
     *
     * @param promotions the complete list of promotions to save
     */
    void saveAll(List<Promotion> promotions);

    /**
     * Returns all stored promotions.
     *
     * @return the list of promotions, or an empty list if none exist
     */
    List<Promotion> loadAll();
}