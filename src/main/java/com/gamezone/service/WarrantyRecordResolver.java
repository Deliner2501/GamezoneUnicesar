package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.SaleReader;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.persistence.WarrantyRepository.WarrantyRecord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.gamezone.persistence.WarrantyTypeMapper;

/**
 * Rebuilds {@link Warranty} objects from the raw records stored by the
 * {@link WarrantyRepository}. It looks up the product and the sale of each
 * record, so that {@link WarrantyService} only has to deal with the business
 * rules of warranties. It depends on {@link SaleReader} because it only needs
 * to read the stored sales.
 */
public class WarrantyRecordResolver {


    private final WarrantyRepository warrantyRepository;
    private final SaleReader saleReader;
    private final ProductService productService;
    private final WarrantyTypeMapper typeMapper = new WarrantyTypeMapper();

    /**
     * Creates a resolver with the collaborators needed to look up sales and products.
     *
     * @param warrantyRepository the repository that holds the raw warranty records
     * @param saleReader         the read-only view of the stored sales
     * @param productService     the service used to find the associated product by id
     */
    public WarrantyRecordResolver(WarrantyRepository warrantyRepository, SaleReader saleReader,
                                  ProductService productService) {
        this.warrantyRepository = warrantyRepository;
        this.saleReader = saleReader;
        this.productService = productService;
    }

    /**
     * Loads every raw warranty record from the repository and resolves each
     * one into a real Warranty object, skipping any record whose product or
     * sale can no longer be found.
     *
     * @return the list of resolved warranties
     * @throws IOException if the warranties or sales cannot be read from storage
     */
    public List<Warranty> loadAll() throws IOException {
        List<Warranty> warranties = new ArrayList<>();
        for (WarrantyRecord record : warrantyRepository.loadAll()) {
            Warranty warranty = resolve(record);
            if (warranty != null) {
                warranties.add(warranty);
            }
        }
        return warranties;
    }

    /**
     * Resolves a raw warranty record back into a real Warranty object,
     * looking up its product and sale. Returns null if either reference
     * can no longer be resolved.
     */
    private Warranty resolve(WarrantyRecord record) throws IOException {
        Product product = productService.findProductById(record.getProductId());
        if (product == null) {
            // Orphan record: the product no longer exists, so the warranty is skipped.
            return null;
        }

        Sale sale = findSaleById(record.getSaleId());
        if (sale == null) {
            // Orphan record: the sale no longer exists, so the warranty is skipped.
            return null;
        }

        return typeMapper.create(record.getType(), record.getId(), product, sale, record.getStartDate());
    }

    private Sale findSaleById(String saleId) throws IOException {
        for (Sale sale : saleReader.findAll()) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }
}