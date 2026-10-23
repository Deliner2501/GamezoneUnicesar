package com.gamezone.service;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.ProductCategory;

/**
 * Factory of consoles. Specific fields: brand, model, generation.
 */
public class ConsoleFactory implements ProductFactory {

    @Override
    public ProductCategory getCategory() {
        return ProductCategory.CONSOLE;
    }

    @Override
    public Product create(String id, String title, double price, int stock, String... specificFields) {
        return new Console(id, title, price, stock,
                specificFields[0], specificFields[1], specificFields[2]);
    }
}