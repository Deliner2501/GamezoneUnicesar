package com.gamezone.service;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductRepository;
import com.gamezone.validation.ProductValidator;
import java.util.List;

/**
 * Contains the business rules for managing products.
 */
public class ProductService {

    private ProductRepository productDAO;

    public ProductService(ProductRepository productDAO) {
        this.productDAO = productDAO;
    }

    /**
     * Registers a new product and persists it.
     *
     * @param product the product to register
     * @throws InvalidDataException if the product is null, has an empty id
     *         or title, a non-positive price, or a negative stock
     */
    public void registerProduct(Product product) {
        if (product == null) {
            throw new InvalidDataException("producto", "no puede ser nulo");
        }
        ProductValidator.validateProductData(product.getId(), product.getTitle(),
                product.getPrice(), product.getStock());
        productDAO.save(product);
    }

    /**
     * Creates a video game from the given data, validates it and persists it.
     *
     * @param id        the unique id of the video game
     * @param title     the title of the video game
     * @param price     the price of the video game
     * @param stock     the initial stock
     * @param platform  the platform the game runs on
     * @param genre     the genre of the game
     * @param ageRating the age rating of the game
     * @throws InvalidDataException if the resulting product data is not valid
     */
    public void registerVideoGame(String id, String title, double price, int stock,
                                  String platform, String genre, String ageRating) {
        registerProduct(new VideoGame(id, title, price, stock, platform, genre, ageRating));
    }

    /**
     * Creates a console from the given data, validates it and persists it.
     *
     * @param id         the unique id of the console
     * @param title      the title of the console
     * @param price      the price of the console
     * @param stock      the initial stock
     * @param brand      the brand of the console
     * @param model      the model of the console
     * @param generation the generation of the console
     * @throws InvalidDataException if the resulting product data is not valid
     */
    public void registerConsole(String id, String title, double price, int stock,
                                String brand, String model, String generation) {
        registerProduct(new Console(id, title, price, stock, brand, model, generation));
    }

    /**
     * Returns the list of all currently available products.
     *
     * @return the list of available products
     */
    public List<Product> listAvailableProducts() {
        return productDAO.findAll();
    }

    /**
     * Checks whether there is enough stock of a given product.
     *
     * @param productId the id of the product to check
     * @param quantity the quantity requested
     * @return true if there is enough stock, false otherwise
     */
    public boolean checkStock(String productId, int quantity) {
        Product product = productDAO.findById(productId);
        if (product == null) {
            return false;
        }
        return product.hasEnoughStock(quantity);
    }

    /**
     * Finds a product by its id.
     *
     * @param productId the id of the product to find
     * @return the product with the given id, or null if none is found
     */
    public Product findProductById(String productId) {
        return productDAO.findById(productId);
    }

    /**
     * Restores stock for a product, typically after a return is processed.
     * The change is persisted immediately.
     *
     * @param productId the id of the product whose stock is being restored
     * @param quantity  the quantity to add back to the product's stock
     * @throws com.gamezone.exceptions.ResourceNotFoundException if the product does not exist
     */
    public void restoreStock(String productId, int quantity) {
        Product product = productDAO.findById(productId);
        ProductValidator.validateProductExists(product, productId);
        product.increaseStock(quantity);
        productDAO.update(product);
    }
}