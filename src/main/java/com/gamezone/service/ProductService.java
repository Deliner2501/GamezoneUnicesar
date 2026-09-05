package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.persistence.ProductDAO;
import java.util.List;

/**
 * Contains the business rules for managing products.
 */
public class ProductService {

    private ProductDAO productDAO;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /**
     * Registers a new product and persists it.
     *
     * @param product the product to register
     */
    public void registerProduct(Product product) {
        productDAO.save(product);
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
        return product.getStock() >= quantity;
    }
}