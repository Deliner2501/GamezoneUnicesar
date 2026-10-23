package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.validation.ProductValidator;

/**
 * Sale item source for consoles and video games, backed by
 * {@link ProductService}.
 */
public class ProductItemSource implements SaleItemSource {

    private final ProductService productService;

    /**
     * Creates a source over the given product service.
     *
     * @param productService the service that owns products and their stock
     */
    public ProductItemSource(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public Product findItem(String itemId) {
        return productService.findProductById(itemId);
    }

    @Override
    public void validateStock(Product item, int quantity) {
        ProductValidator.validateStockAvailability(item, quantity);
    }

    @Override
    public void reduceStock(Product item, int quantity) {
        productService.reduceStock(item.getId(), quantity);
    }
}
