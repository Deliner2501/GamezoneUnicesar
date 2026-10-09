package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Product;

/**
 * Groups the validation rules related to products. Services call these
 * methods before executing a business operation; each method throws the
 * custom exception that matches the kind of problem found.
 */
public class ProductValidator {

    private ProductValidator() {
        // Utility class: it must not be instantiated.
    }

    /**
     * Validates the basic data of a product: the id and the title must
     * not be null or empty, the price must be positive and the stock
     * must not be negative.
     *
     * @param id    the product identifier
     * @param title the product title
     * @param price the product price
     * @param stock the quantity available in inventory
     * @throws InvalidDataException if any of the values is not valid
     */
    public static void validateProductData(String id, String title, double price, int stock) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("id", "no puede estar vacío");
        }
        if (title == null || title.isBlank()) {
            throw new InvalidDataException("título", "no puede estar vacío");
        }
        if (price <= 0) {
            throw new InvalidDataException("precio", "debe ser mayor que cero");
        }
        if (stock < 0) {
            throw new InvalidDataException("cantidad en inventario", "no puede ser negativa");
        }
    }

    /**
     * Validates that a product has enough stock for the requested quantity.
     *
     * @param product           the product to check
     * @param requestedQuantity the quantity the customer wants
     * @throws BusinessRuleException if the available stock is insufficient
     */
    public static void validateStockAvailability(Product product, int requestedQuantity) {
        if (product.getStock() < requestedQuantity) {
            throw new BusinessRuleException("Stock insuficiente para el producto '"
                    + product.getTitle() + "': disponible " + product.getStock()
                    + ", solicitado " + requestedQuantity);
        }
    }

    /**
     * Validates that a product was found.
     *
     * @param product   the product returned by a search (null if not found)
     * @param productId the identifier that was searched
     * @throws ResourceNotFoundException if the product is null
     */
    public static void validateProductExists(Product product, String productId) {
        if (product == null) {
            throw new ResourceNotFoundException("producto", productId);
        }
    }
}