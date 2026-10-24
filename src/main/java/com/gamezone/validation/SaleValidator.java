package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import java.util.List;

/**
 * Groups the validation rules related to sales. Services call these
 * methods before registering or querying a sale; each method throws
 * the custom exception that matches the kind of problem found.
 */
public class SaleValidator {

    private SaleValidator() {
        // Utility class: it must not be instantiated.
    }

    /**
     * Validates the data needed to register a sale: the customer and the
     * seller must be present, and the sale must contain at least one product.
     *
     * @param customer the customer who makes the purchase
     * @param seller   the seller who attends the sale
     * @param products the products included in the sale
     * @throws InvalidDataException  if the customer or the seller is missing
     * @throws BusinessRuleException if the sale has no products
     */
    public static void validateSaleData(Customer customer, Seller seller, List<Product> products) {
        if (customer == null) {
            throw new InvalidDataException("cliente", "es obligatorio para registrar una venta");
        }
        if (seller == null) {
            throw new InvalidDataException("vendedor", "es obligatorio para registrar una venta");
        }
        if (products == null || products.isEmpty()) {
            throw new BusinessRuleException("Una venta debe contener al menos un producto");
        }
    }

    /**
     * Validates that a sale was found.
     *
     * @param sale   the sale returned by a search (null if not found)
     * @param saleId the identifier that was searched
     * @throws ResourceNotFoundException if the sale is null
     */
    public static void validateSaleExists(Sale sale, String saleId) {
        if (sale == null) {
            throw new ResourceNotFoundException("venta", saleId);
        }
    }
}