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
    public boolean supports(Product product) {
        return product instanceof Console;
    }

    @Override
    public String toCsvLine(Product product) {
        if (!supports(product)) {
            throw new IllegalArgumentException("This mapper only handles consoles");
        }
        Console c = (Console) product;
        return "CONSOLE," + c.getId() + "," + c.getTitle() + ","
                + c.getPrice() + "," + c.getStock() + ","
                + c.getBrand() + "," + c.getModel() + "," + c.getGeneration();
    }

    @Override
    public Product parse(String[] parts) {
        return new Console(parts[1], parts[2], Double.parseDouble(parts[3]),
                Integer.parseInt(parts[4]), parts[5], parts[6], parts[7]);
    }
}