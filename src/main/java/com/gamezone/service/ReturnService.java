package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.persistence.ReturnRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the business rules for registering and consulting returns.
 * A return references an existing sale and can only be registered
 * within 30 calendar days of that sale, and only for products that
 * actually belong to it. Registering a return automatically restores
 * the stock of every returned product.
 */
public class ReturnService {

    private ReturnRepository returnRepository;
    private SaleService saleService;
    private ProductService productService;

    /**
     * Creates a ReturnService with its required collaborators.
     *
     * @param returnRepository the repository used to persist and load returns
     * @param saleService      the service used to resolve the original sale
     * @param productService   the service used to restore stock of returned products
     */
    public ReturnService(ReturnRepository returnRepository, SaleService saleService,
                          ProductService productService) {
        this.returnRepository = returnRepository;
        this.saleService = saleService;
        this.productService = productService;
    }

    /**
     * Registers a new return for the given original sale. The return is
     * rejected if the sale does not exist, if it is outside the 30-day
     * return window, or if any indicated product does not actually
     * belong to the referenced sale. If accepted, the refund amount is
     * calculated, the stock of every returned product is restored, and
     * the return is persisted.
     *
     * @param saleId     the id of the original sale
     * @param productIds the ids of the products being returned
     * @param reason     the reason for the return
     * @return the registered return, with its refund amount already calculated
     * @throws IllegalArgumentException if the sale is not found, the 30-day
     *         window has passed, or a product does not belong to the sale
     * @throws IOException if the return cannot be persisted
     */
    public Return registerReturn(String saleId, List<String> productIds, String reason) throws IOException {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un producto para devolver");
        }

        Sale originalSale = saleService.findSaleById(saleId);
        if (originalSale == null) {
            throw new IllegalArgumentException("La venta indicada no existe: " + saleId);
        }

        if (!originalSale.canBeReturned()) {
            throw new IllegalArgumentException(
                    "No se puede registrar la devolución: han pasado más de 30 días desde la venta");
        }

        List<Product> returnedProducts = new ArrayList<>();
        for (String productId : productIds) {
            Product product = findProductInSale(originalSale, productId);
            if (product == null) {
                throw new IllegalArgumentException(
                        "El producto " + productId + " no pertenece a la venta " + saleId);
            }
            returnedProducts.add(product);
        }

        List<Return> existingReturns = returnRepository.loadAll();
        String returnId = generateReturnId(existingReturns);

        Return newReturn = new Return(returnId, LocalDate.now(), originalSale, returnedProducts,