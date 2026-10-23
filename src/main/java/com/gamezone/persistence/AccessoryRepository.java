package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import java.util.List;

/**
 * Defines the storage operations the service layer needs for accessories.
 * The service depends on this abstraction instead of on a concrete
 * storage class (SOLID audit finding V-24).
 */
public interface AccessoryRepository {

    /**
     * Saves a new accessory.
     *
     * @param accessory the accessory to persist
     */
    void save(Accessory accessory);

    /**
     * Returns all stored accessories.
     *
     * @return the list of accessories, or an empty list if none exist
     */
    List<Accessory> findAll();

    /**
     * Finds an accessory by its id.
     *
     * @param id the id to look for
     * @return the matching accessory, or null if none is found
     */
    Accessory findById(String id);

    /**
     * Updates an existing accessory, matching it by id.
     *
     * @param accessory the accessory with updated data
     */
    void update(Accessory accessory);
}