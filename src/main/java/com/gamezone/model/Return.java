package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a return transaction, referencing an original sale and
 * the specific products being returned from it. The reference to the
 * original sale and the list of returned products are immutable once
 * the return is created. The returned products are not necessarily
 * the full set of products from the original sale — a customer may
 * choose to return only some of them.
 */
public class Return {

    private String id;
    private LocalDate date;
    private Sale originalSale;
    private List<Product> returnedProducts;
    private String reason;
    private double refundAmount;
    private double warrantyRefund;

    public Return(String id, LocalDate date, Sale originalSale,
                  List<Product> returnedProducts, String reason, double warrantyRefund) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("The return id cannot be empty");
        }
        if (originalSale == null) {
            throw new IllegalArgumentException("A return must reference an original sale");
        }
        if (returnedProducts == null || returnedProducts.isEmpty()) {
            throw new IllegalArgumentException("A return must include at least one product");
        }
        this.id = id;
        this.date = date;
        this.originalSale = originalSale;
        this.returnedProducts = new ArrayList<>(returnedProducts);
        this.reason = reason;
        this.warrantyRefund = warrantyRefund;
        this.refundAmount = 0.0;
    }

    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public Sale getOriginalSale() { return originalSale; }

    /**
     * Returns an unmodifiable view of the products being returned.
     *
     * @return the unmodifiable list of returned products
     */
    public List<Product> getReturnedProducts() {
        return Collections.unmodifiableList(returnedProducts);
    }

    public String getReason() { return reason; }
    public double getRefundAmount() { return refundAmount; }
    public double getWarrantyRefund() { return warrantyRefund; }

    /**
     * Returns the discount rate of the original sale, asking the sale
     * itself. A sale with a zero subtotal has no discount rate.
     *
     * @return the discount rate, or 0.0 if the original sale has a zero subtotal
     */
    public double getOriginalDiscountRate() {
        if (originalSale.getTotal() > 0) {
            return originalSale.getDiscountRate();
        }
        return 0.0;
    }

    /**
     * Calculates the refund amount by summing, for each returned
     * product, the price proportionally reduced by the discount rate
     * applied to the original sale, plus the reimbursable cost of any
     * canceled warranties. This ensures a customer is never refunded
     * more than what was actually paid.
     *
     * @return the calculated refund amount
     */
    public double calculateRefundAmount() {
        double discountRate = getOriginalDiscountRate();
        double sum = 0.0;
        for (Product product : returnedProducts) {
            sum += product.getPrice() * (1 - discountRate);
        }
        this.refundAmount = sum + warrantyRefund;
        return refundAmount;
    }
}