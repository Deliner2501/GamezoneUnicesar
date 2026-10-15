package com.gamezone.persistence;

import com.gamezone.model.Product;
import com.gamezone.model.ProductCategory;

/**
 * Converts one concrete type of product to and from its CSV representation.
 */
public interface ProductCsvMapper {

    /**
     * Returns the category of the products this mapper handles.
     *
     * @return the product category
     */
    ProductCategory getCategory();

    /**
     * Returns the discriminator written in the first CSV column.
     *
     * @return the type code
     */
    String getTypeCode();

    /**
     * Builds the CSV line of the given product.
     *
     * @param product the product to convert
     * @return the CSV line
     */
    String toCsvLine(Product product);

    /**
     * Builds a product from its parsed CSV values.
     *
     * @param id    the product id
     * @param title the product title
     * @param price the product price
     * @param stock the product stock
     * @param parts the full array of CSV columns
     * @return the product created
     */
    Product create(String id, String title, double price, int stock, String[] parts);
}