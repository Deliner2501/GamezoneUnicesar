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

    public ReturnService(ReturnRepository returnRepository, SaleService saleService,
                          ProductService productService) {
        this.returnRepository = returnRepository;
        this.saleService = saleService;
        this.productService = productService;
    }

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

        Return newReturn = new Return(returnId, LocalDate.now(), originalSale, returnedProducts, reason);
        newReturn.calculateRefundAmount();

        for (Product product : returnedProducts) {
            productService.restoreStock(product.getId(), 1);
        }

        existingReturns.add(newReturn);
        returnRepository.saveAll(existingReturns);

        return newReturn;
    }

    public List<Return> viewAllReturns() throws IOException {
        return returnRepository.loadAll();
    }

    public List<Return> viewReturnsByCustomer(String customerId) throws IOException {
        List<Return> result = new ArrayList<>();
        for (Return r : returnRepository.loadAll()) {
            if (r.getOriginalSale().getCustomer().getId().equals(customerId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Return> viewReturnsBySale(String saleId) throws IOException {
        List<Return> result = new ArrayList<>();
        for (Return r : returnRepository.loadAll()) {
            if (r.getOriginalSale().getId().equals(saleId)) {
                result.add(r);
            }
        }
        return result;
    }

    public double generateMonthlyBalance(int month, int year) throws IOException {
        double totalSales = 0.0;
        for (Sale sale : saleService.listSales()) {
            if (sale.getDate().getMonthValue() == month && sale.getDate().getYear() == year) {
                totalSales += sale.getFinalTotal();
            }
        }

        double totalReturns = 0.0;
        for (Return r : returnRepository.loadAll()) {
            if (r.getDate().getMonthValue() == month && r.getDate().getYear() == year) {
                totalReturns += r.getRefundAmount();
            }
        }

        return totalSales - totalReturns;
    }

    private Product findProductInSale(Sale sale, String productId) {
        for (Product product : sale.getProducts()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    private String generateReturnId(List<Return> existingReturns) {
        return "DEV" + String.format("%03d", existingReturns.size() + 1);
    }
}