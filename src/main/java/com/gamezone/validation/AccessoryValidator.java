package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Accessory;

/**
 * Groups the validation rules related to accessories. Services call
 * these methods before executing a business operation; each method
 * throws the custom exception that matches the kind of problem found.
 */
public class AccessoryValidator {

    private AccessoryValidator() {
        // Utility class: it must not be instantiated.
    }

    /**
     * Validates the basic data of an accessory: the id must not be
     * null or empty and the price must not be negative.
     *
     * @param id    the accessory identifier
     * @param price the accessory price
     * @throws InvalidDataException if any of the values is not valid
     */
    public static void validateAccessoryData(String id, double price) {
        if (id == null || id.isEmpty()) {
            throw new InvalidDataException("id", "no puede estar vacío");
        }
        if (price < 0) {
            throw new InvalidDataException("precio", "no puede ser negativo");
        }
    }

    /**
     * Validates that an accessory was found.
     *
     * @param accessory   the accessory returned by a search (null if not found)
     * @param accessoryId the identifier that was searched
     * @throws ResourceNotFoundException if the accessory is null
     */
    public static void validateAccessoryExists(Accessory accessory, String accessoryId) {
        if (accessory == null) {
            throw new ResourceNotFoundException("accesorio", accessoryId);
        }
    }

    /**
     * Validates that a quantity is greater than zero.
     *
     * @param quantity the quantity to check
     * @throws InvalidDataException if the quantity is zero or negative
     */
    public static void validateQuantityPositive(int quantity) {
        if (quantity <= 0) {
            throw new InvalidDataException("cantidad", "debe ser mayor que cero");
        }
    }

    /**
     * Validates that an accessory has enough stock for the requested quantity.
     *
     * @param accessory         the accessory to check
     * @param requestedQuantity the quantity requested
     * @throws BusinessRuleException if the available stock is insufficient
     */
    public static void validateStockAvailability(Accessory accessory, int requestedQuantity) {
        if (accessory.getStock() < requestedQuantity) {
            throw new BusinessRuleException("Stock insuficiente para el accesorio '"
                    + accessory.getTitle() + "': disponible " + accessory.getStock()
                    + ", solicitado " + requestedQuantity);
        }
    }
}
