package com.gamezone.persistence;

import com.gamezone.model.Product;
import com.gamezone.model.ProductCategory;

/**
 * Converts one concrete type of product to and from its CSV representation.
 * It owns the storage format in both directions (SOLID audit finding V-07).
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
     * Checks whether this mapper can convert the given product.
     *
     * @param product the product to check
     * @return true if the product is of the type this mapper handles
     */
    boolean supports(Product product);

    /**
     * Builds the CSV line of the given product. The product must be
     * supported by this mapper (see {@link #supports(Product)}).
     *
     * @param product the product to convert
     * @return the CSV line
     * @throws IllegalArgumentException if the product is not supported
     */
    String toCsvLine(Product product);

    /**
     * Builds a product from the columns of one CSV line, including the
     * common ones (id, title, price, stock).
     *
     * @param parts the full array of CSV columns
     * @return the product created
     * @throws NumberFormatException if a numeric column is not valid
     * @throws ArrayIndexOutOfBoundsException if columns are missing
     */
    Product parse(String[] parts);
}