package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Product;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence operations for products, storing and retrieving
 * them from a CSV file.
 */
public class ProductDAO implements ProductRepository {

    private static final String DEFAULT_FILE_PATH = "data/products.csv";

    private final String filePath;
    private final DataLogger logger;
    private final List<ProductCsvMapper> mappers =
            List.of(new VideoGameCsvMapper(), new ConsoleCsvMapper());

    /**
     * Creates a DAO that uses the default file and reports warnings to the console.
     */
    public ProductDAO() {
        this(DEFAULT_FILE_PATH, new ConsoleDataLogger());
    }

    /**
     * Creates a DAO that uses the default file and the given logger.
     *
     * @param logger the logger that receives the warnings
     */
    public ProductDAO(DataLogger logger) {
        this(DEFAULT_FILE_PATH, logger);
    }

    /**
     * Creates a DAO that uses the given file and logger.
     *
     * @param filePath the CSV file where products are stored
     * @param logger   the logger that receives the warnings
     */
    public ProductDAO(String filePath, DataLogger logger) {
        this.filePath = filePath;
        this.logger = logger;
    }

    /**
     * Saves a single product by appending it to the file.
     *
     * @param product the product to persist
     * @throws PersistenceException if the file cannot be read or written
     */
    @Override
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
     * @throws PersistenceException if the file cannot be read
     */
    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return products;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Product p = parseLine(line);
                    if (p != null) products.add(p);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    logger.warn("Skipping corrupted line: " + line);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al leer el archivo " + filePath + " (operación de lectura)", e);
        }
        return products;
    }

    /**
     * Finds a product by its id.
     *
     * @param id the id of the product to find
     * @return the matching product, or null if none is found
     * @throws PersistenceException if the file cannot be read
     */
    @Override
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
     * @throws PersistenceException if the file cannot be read or written
     */
    @Override
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
        File dir = new File(filePath).getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Product p : products) {
                for (ProductCsvMapper mapper : mappers) {
                    if (mapper.getCategory() == p.getCategory()) {
                        writer.println(mapper.toCsvLine(p));
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al escribir el archivo " + filePath + " (operación de escritura)", e);
        }
    }

    private Product parseLine(String line) {
        String[] parts = line.split(",");
        String type = parts[0];
        String id = parts[1];
        String title = parts[2];
        double price = Double.parseDouble(parts[3]);
        int stock = Integer.parseInt(parts[4]);

        for (ProductCsvMapper mapper : mappers) {
            if (mapper.getTypeCode().equals(type)) {
                return mapper.create(id, title, price, stock, parts);
            }
        }
        return null;
    }
}