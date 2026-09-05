package com.gamezone.persistence;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence operations for products, storing and retrieving
 * them from a CSV file.
 */
public class ProductDAO {

    private static final String FILE_PATH = "data/products.csv";

    /**
     * Saves a single product by appending it to the file.
     *
     * @param product the product to persist
     */
    public void save(Product product) {
        List<Product> products = findAll();
        products.add(product);
        writeAll(products);
    }

    /**
     * Returns all products currently stored in the file.
     *
     * @return the list of all persisted products, or an empty list
     *         if the file does not exist yet
     */
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return products;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Product p = parseLine(line);
                    if (p != null) products.add(p);
                } catch (Exception e) {
                    System.out.println("Skipping corrupted line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading products: " + e.getMessage());
        }
        return products;
    }

    /**
     * Finds a product by its id.
     *
     * @param id the id of the product to find
     * @return the matching product, or null if none is found
     */
    public Product findById(String id) {
        for (Product p : findAll()) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    /**
     * Updates an existing product, replacing it by matching id.
     *
     * @param product the product with updated data
     */
    public void update(Product product) {
        List<Product> products = findAll();
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(product.getId())) {
                products.set(i, product);
                break;
            }
        }
        writeAll(products);
    }

    private void writeAll(List<Product> products) {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Product p : products) {
                if (p instanceof VideoGame) {
                    VideoGame vg = (VideoGame) p;
                    writer.println("VIDEOGAME," + vg.getId() + "," + vg.getTitle() + ","
                            + vg.getPrice() + "," + vg.getStock() + ","
                            + vg.getPlatform() + "," + vg.getGenre() + "," + vg.getAgeRating());
                } else if (p instanceof Console) {
                    Console c = (Console) p;
                    writer.println("CONSOLE," + c.getId() + "," + c.getTitle() + ","
                            + c.getPrice() + "," + c.getStock() + ","
                            + c.getBrand() + "," + c.getModel() + "," + c.getGeneration());
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving products: " + e.getMessage());
        }
    }

    private Product parseLine(String line) {
        String[] parts = line.split(",");
        String type = parts[0];
        String id = parts[1];
        String title = parts[2];
        double price = Double.parseDouble(parts[3]);
        int stock = Integer.parseInt(parts[4]);

        if (type.equals("VIDEOGAME")) {
            return new VideoGame(id, title, price, stock, parts[5], parts[6], parts[7]);
        } else if (type.equals("CONSOLE")) {
            return new Console(id, title, price, stock, parts[5], parts[6], parts[7]);
        }
        return null;
    }
}