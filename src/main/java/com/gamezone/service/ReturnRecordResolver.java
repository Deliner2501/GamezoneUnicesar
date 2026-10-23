package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.persistence.ReturnRepository;
import com.gamezone.persistence.ReturnRepository.ReturnRecord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Rebuilds {@link Return} objects from the raw records stored by the
 * {@link ReturnRepository}. It looks up the original sale and the returned
 * products of each record, so that {@link ReturnService} only has to deal
 * with the business rules of a return.
 */
public class ReturnRecordResolver {

    private final ReturnRepository returnRepository;
    private final SaleService saleService;
    private final ProductService productService;
    private final AccessoryService accessoryService;

    /**
     * Creates a resolver with the collaborators needed to look up sales and products.
     *
     * @param returnRepository the repository that holds the raw return records
     * @param saleService      the service used to find the original sale
     * @param productService   the service used to find returned products
     * @param accessoryService the service used to find returned accessories
     */
    public ReturnRecordResolver(ReturnRepository returnRepository, SaleService saleService,
                                ProductService productService, AccessoryService accessoryService) {
        this.returnRepository = returnRepository;
        this.saleService = saleService;
        this.productService = productService;
        this.accessoryService = accessoryService;
    }

    /**
     * Loads every raw return record from the repository and resolves each one
     * into a real Return object, skipping any record whose sale or products
     * can no longer be found.
     *
     * @return the list of resolved returns
     * @throws IOException if the returns cannot be read from storage
     */
    public List<Return> loadAll() throws IOException {
        List<Return> returns = new ArrayList<>();
        for (ReturnRecord record : returnRepository.loadAll()) {
            Return resolved = resolve(record);
            if (resolved != null) {
                returns.add(resolved);
            }
        }
        return returns;
    }

    /**
     * Resolves a raw return record back into a real Return object, looking up
     * its original sale and returned products. Returns null if the sale or
     * every product can no longer be resolved.
     */
    private Return resolve(ReturnRecord record) throws IOException {
        Sale originalSale = saleService.findSaleById(record.getSaleId());
        if (originalSale == null) {
            return null;
        }

        List<Product> returnedProducts = new ArrayList<>();
        for (String productId : record.getProductIds()) {
            Product product = productService.findProductById(productId);
            if (product == null) {
                product = accessoryService.findById(productId);
            }
            if (product != null) {
                returnedProducts.add(product);
            }
        }
        if (returnedProducts.isEmpty()) {
            return null;
        }

        Return resolved = new Return(record.getId(), record.getDate(), originalSale,
                returnedProducts, record.getReason(), record.getWarrantyRefund());
        if (record.isRefundCalculated()) {
            resolved.calculateRefundAmount();
        }
        return resolved;
    }
}