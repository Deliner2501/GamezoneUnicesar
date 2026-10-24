package com.gamezone.service;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Product;
import com.gamezone.model.ProductCategory;
import com.gamezone.persistence.ProductRepository;
import com.gamezone.validation.ProductValidator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the business rules for managing products.
 */
public class ProductService {

    private ProductRepository productDAO;
    private final Map<ProductCategory, ProductFactory> factories = new EnumMap<>(ProductCategory.class);

    /**
     * Creates the service with its repository and the factories of the
     * product types it can register.
     *
     * @param productDAO the repository where products are stored
     * @param factories  one factory for each supported product type
     */
    public ProductService(ProductRepository productDAO, List<ProductFactory> factories) {
        this.productDAO = productDAO;
        for (ProductFactory factory : factories) {
            this.factories.put(factory.getCategory(), factory);
        }
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
     * Builds a product of the given category with the factory registered
     * for it, validates it and persists it.
     *
     * @param category       the category of the product to register
     * @param id             the unique id of the product
     * @param title          the title of the product
     * @param price          the price of the product
     * @param stock          the initial stock
     * @param specificFields the fields that only this type has
     * @throws InvalidDataException if the category has no factory or the
     *         resulting product data is not valid
     */
    public void registerNewProduct(ProductCategory category, String id, String title,
                                   double price, int stock, String... specificFields) {
        ProductFactory factory = factories.get(category);
        if (factory == null) {
            throw new InvalidDataException("tipo de producto", "no es compatible");
        }
        registerProduct(factory.create(id, title, price, stock, specificFields));
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

    /**
     * Reduces stock for a product after a sale, persisting the change
     * immediately. Lets SaleService depend on ProductService instead of
     * reaching into ProductRepository/ProductDAO directly
     * (GRASP audit finding V-34: Low Coupling).
     *
     * @param productId the id of the product being sold
     * @param quantity  the quantity to subtract from the product's stock
     * @throws com.gamezone.exceptions.ResourceNotFoundException if the product does not exist
     */
    public void reduceStock(String productId, int quantity) {
        Product product = productDAO.findById(productId);
        ProductValidator.validateProductExists(product, productId);
        product.reduceStock(quantity);
        productDAO.update(product);
    }
}