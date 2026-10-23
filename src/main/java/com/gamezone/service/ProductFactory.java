package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.model.ProductCategory;

/**
 * Builds one concrete type of product from the data entered by the user.
 * Supporting a new product type only requires a new implementation.
 */
public interface ProductFactory {

    /**
     * Returns the category of the products this factory builds.
     *
     * @return the product category
     */
    ProductCategory getCategory();

    /**
     * Builds a product of this factory's type.
     *
     * @param id             the unique id of the product
     * @param title          the title of the product
     * @param price          the price of the product
     * @param stock          the initial stock
     * @param specificFields the fields that only this type has, in the order
     *                       requested by the menu
     * @return the product created
     */
    Product create(String id, String title, double price, int stock, String... specificFields);
}