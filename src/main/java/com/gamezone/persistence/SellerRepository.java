package com.gamezone.persistence;

import com.gamezone.model.Seller;
import java.util.List;

/**
 * Storage contract for sellers. Classes that only need sellers depend on
 * this interface instead of on a concrete DAO (SOLID audit finding V-15).
 */
public interface SellerRepository {

    /**
     * Saves the whole list of sellers, replacing the stored one.
     *
     * @param sellers the sellers to persist
     */
    void saveSellers(List<Seller> sellers);

    /**
     * Loads every stored seller.
     *
     * @return the stored sellers, or an empty list if none exist
     */
    List<Seller> loadSellers();
}