package com.gamezone.persistence;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.ProductCategory;

/**
 * CSV mapper for consoles.
 */
public class ConsoleCsvMapper implements ProductCsvMapper {

    @Override
    public ProductCategory getCategory() {
        return ProductCategory.CONSOLE;
    }

    @Override
    public String getTypeCode() {
        return "CONSOLE";
    }

    @Override
    public String toCsvLine(Product product) {
        Console c = (Console) product;
        return "CONSOLE," + c.getId() + "," + c.getTitle() + ","
                + c.getPrice() + "," + c.getStock() + ","
                + c.getBrand() + "," + c.getModel() + "," + c.getGeneration();
    }

    @Override
    public Product create(String id, String title, double price, int stock, String[] parts) {
        return new Console(id, title, price, stock, parts[5], parts[6], parts[7]);
    }
}