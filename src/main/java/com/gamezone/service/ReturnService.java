package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Accessory;
import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.persistence.ReturnRepository;
import com.gamezone.persistence.ReturnRepository.ReturnRecord;
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
    private WarrantyService warrantyService;

    /**
     * Creates a ReturnService with its required collaborators.
     *
     * @param returnRepository the repository used to persist and load returns
     * @param saleService      the service used to resolve the original sale
     * @param productService   the service used to restore stock of returned products
     * @param accessoryService the service used to restore stock of returned accessories
     * @param warrantyService  the service used to cancel warranties of returned consoles
     */
    public ReturnService(ReturnRepository returnRepository, SaleService saleService,
                          ProductService productService, AccessoryService accessoryService,
                          WarrantyService warrantyService) {
        this.returnRepository = returnRepository;
        this.saleService = saleService;
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.warrantyService = warrantyService;
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
     * @throws InvalidDataException if the sale id, the product list or the reason is empty
     * @throws ResourceNotFoundException if the sale does not exist
     * @throws BusinessRuleException if the 30-day window has passed or a
     *         product does not belong to the sale
     * @throws IOException if the return cannot be persisted
     */
    public Return registerReturn(String saleId, List<String> productIds, String reason) throws IOException {
        validateReturnRequest(saleId, productIds, reason);

        Sale originalSale = findReturnableSale(saleId);
        List<Product> returnedProducts = resolveReturnedProducts(originalSale, productIds);

        List<Return> existingReturns = loadAllReturns();
        String returnId = generateReturnId(existingReturns);

        double warrantyRefund = cancelConsoleWarranties(returnedProducts, saleId);

        Return newReturn = new Return(returnId, LocalDate.now(), originalSale, returnedProducts, reason, warrantyRefund);
        newReturn.calculateRefundAmount();

        restoreStockOf(returnedProducts);

        existingReturns.add(newReturn);
        returnRepository.saveAll(existingReturns);

        return newReturn;
    }

    /**
     * Checks that the sale id, the product list and the reason were provided.
     */
    private void validateReturnRequest(String saleId, List<String> productIds, String reason) {
        if (saleId == null || saleId.isBlank()) {
            throw new InvalidDataException("id de la venta", "no puede estar vacío");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new InvalidDataException("productos", "debe indicar al menos un producto para devolver");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidDataException("motivo", "no puede estar vacío");
        }
    }

    /**
     * Finds the original sale and checks that it exists and is still
     * inside the 30-day return window.
     */
    private Sale findReturnableSale(String saleId) throws IOException {
        Sale originalSale = saleService.findSaleById(saleId);
        if (originalSale == null) {
            throw new ResourceNotFoundException("venta", saleId);
        }
        if (!originalSale.canBeReturned()) {
            throw new BusinessRuleException(
                    "No se puede registrar la devolución: han pasado más de 30 días desde la venta");
        }
        return originalSale;
    }

    /**
     * Resolves the indicated product ids into the products of the sale,
     * rejecting any id that does not belong to it.
     */
    private List<Product> resolveReturnedProducts(Sale originalSale, List<String> productIds) {
        List<Product> returnedProducts = new ArrayList<>();
        for (String productId : productIds) {
            Product product = findProductInSale(originalSale, productId);
            if (product == null) {
                throw new BusinessRuleException(
                        "El producto " + productId + " no pertenece a la venta " + originalSale.getId());
            }
            returnedProducts.add(product);
        }
        return returnedProducts;
    }

    /**
     * Cancels the warranties of every returned console and returns the
     * total reimbursable warranty cost.
     */
    private double cancelConsoleWarranties(List<Product> returnedProducts, String saleId) throws IOException {
        double warrantyRefund = 0.0;
        for (Product product : returnedProducts) {
            if (product.requiresWarranty()) {
                warrantyRefund += warrantyService.cancelWarranties(product.getId(), saleId);
            }
        }
        return warrantyRefund;
    }

    /**
     * Restores the stock of every returned product, routing accessories
     * to the accessory service and everything else to the product service.
     */
    private void restoreStockOf(List<Product> returnedProducts) {
        for (Product product : returnedProducts) {
            if (product instanceof Accessory) {
                accessoryService.restoreStock(product.getId(), 1);
            } else {
                productService.restoreStock(product.getId(), 1);
            }
        }
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
     * Loads every raw return record from the repository and resolves
     * each one into a real Return object, skipping any record whose
     * sale or products can no longer be found.
     */
    private List<Return> loadAllReturns() throws IOException {
        List<Return> returns = new ArrayList<>();
        for (ReturnRecord record : returnRepository.loadAll()) {
            Return resolved = resolveReturn(record);
            if (resolved != null) {
                returns.add(resolved);
            }
        }
        return returns;
    }

    /**
     * Resolves a raw return record back into a real Return object,
     * looking up its original sale and returned products. Returns null
     * if the sale or every product can no longer be resolved.
     */
    private Return resolveReturn(ReturnRecord record) throws IOException {
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
    

    /**
     * Returns every return registered in the system.
     *
     * @return the list of all returns
     * @throws IOException if the returns cannot be read from storage
     */
    public List<Return> viewAllReturns() throws IOException {
        return loadAllReturns();
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
        for (Return r : loadAllReturns()) {
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
        for (Return r : loadAllReturns()) {
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
        for (Return r : loadAllReturns()) {
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

