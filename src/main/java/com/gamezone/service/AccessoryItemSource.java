package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Product;
import com.gamezone.validation.AccessoryValidator;

/**
 * Sale item source for accessories, backed by {@link AccessoryService}.
 * It only receives items that its own {@link #findItem(String)} returned,
 * which are always accessories.
 */
public class AccessoryItemSource implements SaleItemSource {

    private final AccessoryService accessoryService;

    /**
     * Creates a source over the given accessory service.
     *
     * @param accessoryService the service that owns accessories and their stock
     */
    public AccessoryItemSource(AccessoryService accessoryService) {
        this.accessoryService = accessoryService;
    }

    @Override
    public Product findItem(String itemId) {
        return accessoryService.findById(itemId);
    }

    @Override
    public void validateStock(Product item, int quantity) {
        AccessoryValidator.validateStockAvailability((Accessory) item, quantity);
    }

    @Override
    public void reduceStock(Product item, int quantity) {
        accessoryService.updateStock(item.getId(), quantity);
    }
}
