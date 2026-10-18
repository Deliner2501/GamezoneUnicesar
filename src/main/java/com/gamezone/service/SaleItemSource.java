package com.gamezone.service;

import com.gamezone.model.Product;

/**
 * A kind of item that can be sold: it knows how to find an item by id,
 * check its stock and reduce it. {@link SaleService} works with a list of
 * these sources, so adding a new sellable kind means adding a new source
 * instead of editing the sale registration (SOLID audit finding V-04).
 */
public interface SaleItemSource {

    /**
     * Looks an item up by id.
     *
     * @param itemId the id to look for
     * @return the item, or null if this source does not have it
     */
    Product findItem(String itemId);

    /**
     * Checks that enough stock is available for the requested quantity.
     *
     * @param item     an item previously returned by {@link #findItem(String)}
     * @param quantity the quantity requested
     * @throws com.gamezone.exceptions.BusinessRuleException if the stock is insufficient
     */
    void validateStock(Product item, int quantity);

    /**
     * Reduces the stock of the item and persists the change.
     *
     * @param item     an item previously returned by {@link #findItem(String)}
     * @param quantity the quantity to subtract from the stock
     */
    void reduceStock(Product item, int quantity);
}