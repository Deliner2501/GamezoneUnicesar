package com.gamezone.persistence;

import com.gamezone.model.Product;
import java.util.List;

/**
 * Defines the storage operations the service layer needs for products.
 */
public interface ProductRepository {

    /**
     * Saves a new product.
     *
     * @param product the product to persist
     */
    void save(Product product);

    /**
     * Returns all stored products.
     *
     * @return the list of products
     */
    List<Product> findAll();

    /**
     * Finds a product by its id.
     *
     * @param id the id to look for
     * @return the matching product, or null if none is found
     */
    Product findById(String id);

    /**
     * Updates an existing product, matching it by id.
     *
     * @param product the product with updated data
     */
    void update(Product product);
}