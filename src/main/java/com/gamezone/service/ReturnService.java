package com.gamezone.service;

import com.gamezone.model.Accessory;
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
    private AccessoryService accessoryService;

    /**
     * Creates a ReturnService with its required collaborators.
     *
     * @param returnRepository the repository used to persist and load returns
     * @param saleService      the service used to resolve the original sale
     * @param productService   the service used to restore stock of returned products
     * @param accessoryService the service used to restore stock of returned accessories
     */
    public ReturnService(ReturnRepository returnRepository, SaleService saleService,
                          ProductService productService, AccessoryService accessoryService) {
        this.returnRepository = returnRepository;
        this.saleService = saleService;
        this.productService = productService;
        this.accessoryService = accessoryService;
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
        if (saleId == null || saleId.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el id de la venta original");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un producto para devolver");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo de la devolución");
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

                Return newReturn = new Return(returnId, LocalDate.now(), originalSale, returnedProducts, reason);
        newReturn.calculateRefundAmount();

        for (Product product : returnedProducts) {
            if (product instanceof Accessory) {
                accessoryService.restoreStock(product.getId(), 1);
            } else {
                productService.restoreStock(product.getId(), 1);
            }
        }

        existingReturns.add(newReturn);
        returnRepository.saveAll(existingReturns);

        return newReturn;
    }

    /**
     * Finds the product with the given id within a sale's product list.
     *
     * @param sale      the sale to search
     * @param productId the id of the product to find
     * @return the matching product, or null if it does not belong to the sale
     */
    private Product findProductInSale(Sale sale, String productId) {
        for (Product product : sale.getProducts()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Generates a sequential return id based on how many returns already exist.
     *
     * @param existingReturns the currently persisted returns
     * @return a new, unique return id
     */
    private String generateReturnId(List<Return> existingReturns) {
        int nextNumber = existingReturns.size() + 1;
        return "RET" + String.format("%03d", nextNumber);
    }

    /**
     * Returns every return registered in the system.
     *
     * @return the list of all returns
     * @throws IOException if the returns cannot be read from storage
     */
    public List<Return> viewAllReturns() throws IOException {
        return returnRepository.loadAll();
    }

    /**
     * Returns every return whose original sale belongs to the given customer.
     *
     * @param customerId the id of the customer
     * @return the list of returns associated with that customer
     * @throws IOException if the returns cannot be read from storage
     */
    public List<Return> viewReturnsByCustomer(String customerId) throws IOException {
        List<Return> result = new ArrayList<>();
        for (Return r : returnRepository.loadAll()) {
            if (r.getOriginalSale().getCustomer().getId().equals(customerId)) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Returns every return associated with the given original sale.
     *
     * @param saleId the id of the original sale
     * @return the list of returns associated with that sale
     * @throws IOException if the returns cannot be read from storage
     */
    public List<Return> viewReturnsBySale(String saleId) throws IOException {
        List<Return> result = new ArrayList<>();
        for (Return r : returnRepository.loadAll()) {
            if (r.getOriginalSale().getId().equals(saleId)) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Calculates the net balance (sales minus returns) for a given month and year.
     *
     * @param month the month to calculate the balance for (1-12)
     * @param year  the year to calculate the balance for
     * @return the total sales minus the total returns for that period
     * @throws IOException if sales or returns cannot be read from storage
     */
    public double calculateMonthlySales(int month, int year) throws IOException {
        double totalSales = 0.0;
        for (Sale sale : saleService.listSales()) {
            if (sale.getDate().getMonthValue() == month && sale.getDate().getYear() == year) {
                totalSales += sale.getFinalTotal();
            }
        }
        return totalSales;
    }

    public double calculateMonthlyReturns(int month, int year) throws IOException {
        double totalReturns = 0.0;
        for (Return r : returnRepository.loadAll()) {
            if (r.getDate().getMonthValue() == month && r.getDate().getYear() == year) {
                totalReturns += r.getRefundAmount();
            }
        }
        return totalReturns;
    }

    public double generateMonthlyBalance(int month, int year) throws IOException {
        return calculateMonthlySales(month, year) - calculateMonthlyReturns(month, year);
    }
}