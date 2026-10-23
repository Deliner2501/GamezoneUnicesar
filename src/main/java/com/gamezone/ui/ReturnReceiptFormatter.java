package com.gamezone.ui;

import com.gamezone.model.Product;
import com.gamezone.model.Return;

/**
 * Builds the human-readable receipt text for a {@link Return}.
 * Receipt formatting is a presentation concern, so it is kept out of
 * the {@code Return} class (SOLID audit finding V-09: SRP).
 */
public final class ReturnReceiptFormatter {

    private ReturnReceiptFormatter() {
        // utility class, no instances
    }

    /**
     * Builds a readable receipt, in Spanish, showing the return id, date,
     * original sale, each returned product with its list price and
     * proportional discount, the reason and the refund amount. If the refund
     * amount has not been calculated yet, it is calculated first so the
     * receipt is always accurate.
     *
     * @param processedReturn the return to format
     * @return a formatted, multi-line receipt
     */
    public static String format(Return processedReturn) {
        if (processedReturn.getRefundAmount() == 0.0) {
            processedReturn.calculateRefundAmount();
        }
        double discountRate = processedReturn.getOriginalDiscountRate();
        StringBuilder receipt = new StringBuilder();
        receipt.append("Recibo de devolución ").append(processedReturn.getId()).append("\n");
        receipt.append("Fecha: ").append(processedReturn.getDate()).append("\n");
        receipt.append("Venta original: ").append(processedReturn.getOriginalSale().getId()).append("\n");
        receipt.append("Productos devueltos:\n");
        for (Product product : processedReturn.getReturnedProducts()) {
            double listPrice = product.getPrice();
            double proportionalDiscount = listPrice * discountRate;
            double refundedForItem = listPrice - proportionalDiscount;
            receipt.append("  - ").append(product.getTitle())
                    .append(": precio de lista ").append(listPrice)
                    .append(", descuento proporcional ").append(proportionalDiscount)
                    .append(", reembolsado ").append(refundedForItem).append("\n");
        }
        receipt.append("Motivo: ").append(processedReturn.getReason()).append("\n");
        receipt.append("Reembolso por garantías canceladas: ")
                .append(processedReturn.getWarrantyRefund()).append("\n");
        receipt.append("Monto reembolsado: ").append(processedReturn.getRefundAmount());
        return receipt.toString();
    }
}