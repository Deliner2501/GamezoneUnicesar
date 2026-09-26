package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a return transaction, referencing an original sale and
 * the specific products being returned from it.
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
     * Calculates the refund amount by summing the prices of the
     * returned products, and stores it in this return's state.
     *
     * @return the calculated refund amount
     */
    public double calculateRefundAmount() {
        double sum = 0.0;
        for (Product product : returnedProducts) {
            sum += product.getPrice();
        }
        this.refundAmount = sum;
        return refundAmount;
    }

    /**
     * Builds a readable receipt for this return, in Spanish, showing
     * the return id, date, reference to the original sale, returned
     * products with their prices, reason, and refund amount.
     *
     * @return a formatted, multi-line receipt
     */
    public String generateReturnReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Recibo de devolución ").append(id).append("\n");
        receipt.append("Fecha: ").append(date).append("\n");
        receipt.append("Venta original: ").append(originalSale.getId()).append("\n");
        receipt.append("Productos devueltos:\n");
        for (Product product : returnedProducts) {
            receipt.append("  - ").append(product.getTitle())
                    .append(": ").append(product.getPrice()).append("\n");
        }
        receipt.append("Motivo: ").append(reason).append("\n");
        receipt.append("Monto reembolsado: ").append(refundAmount);
        return receipt.toString();
    }
}