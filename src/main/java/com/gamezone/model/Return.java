package com.gamezone.model;

import java.time.LocalDate;
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

    public Return(String id, LocalDate date, Sale originalSale,
                  List<Product> returnedProducts, String reason) {
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
        this.returnedProducts = returnedProducts;
        this.reason = reason;
        this.refundAmount = 0.0;
    }

    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public Sale getOriginalSale() { return originalSale; }
    public List<Product> getReturnedProducts() { return returnedProducts; }
    public String getReason() { return reason; }
    public double getRefundAmount() { return refundAmount; }

    /**
     * Calculates the refund amount by summing, for each returned
     * product, the price proportionally reduced by the discount rate
     * applied to the original sale. This ensures a customer is never
     * refunded more than what was actually paid.
     *
     * @return the calculated refund amount
     */
    public double calculateRefundAmount() {
        double subtotal = originalSale.getTotal();
        double discountRate = 0.0;
        if (subtotal > 0) {
            discountRate = originalSale.getDiscountAmount() / subtotal;
        }
        double sum = 0.0;
        for (Product product : returnedProducts) {
            sum += product.getPrice() * (1 - discountRate);
        }
        this.refundAmount = sum;
        return refundAmount;
    }

    /**
     * Builds a readable receipt for this return, in Spanish, showing
     * the return id, date, reference to the original sale, each
     * returned product with its list price and proportional discount,
     * reason, and refund amount. If the refund amount has not been
     * calculated yet, this method calculates it first so the receipt
     * is always accurate.
     *
     * @return a formatted, multi-line receipt
     */
    public String generateReturnReceipt() {
        if (refundAmount == 0.0) {
            calculateRefundAmount();
        }
        double subtotal = originalSale.getTotal();
        double discountRate = 0.0;
        if (subtotal > 0) {
            discountRate = originalSale.getDiscountAmount() / subtotal;
        }
        StringBuilder receipt = new StringBuilder();
        receipt.append("Recibo de devolución ").append(id).append("\n");
        receipt.append("Fecha: ").append(date).append("\n");
        receipt.append("Venta original: ").append(originalSale.getId()).append("\n");
        receipt.append("Productos devueltos:\n");
        for (Product product : returnedProducts) {
            double listPrice = product.getPrice();
            double proportionalDiscount = listPrice * discountRate;
            double refundedForItem = listPrice - proportionalDiscount;
            receipt.append("  - ").append(product.getTitle())
                    .append(": precio de lista ").append(listPrice)
                    .append(", descuento proporcional ").append(proportionalDiscount)
                    .append(", reembolsado ").append(refundedForItem).append("\n");
        }
        receipt.append("Motivo: ").append(reason).append("\n");
        receipt.append("Monto reembolsado: ").append(refundAmount);
        return receipt.toString();
    }
}