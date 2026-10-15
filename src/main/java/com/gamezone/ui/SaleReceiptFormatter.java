package com.gamezone.ui;

import com.gamezone.model.Sale;

/**
 * Builds the human-readable receipt text for a {@link Sale}.
 * This is a Pure Fabrication: receipt formatting is a presentation
 * concern, not a domain responsibility, so it is kept out of the
 * {@code Sale} class itself (GRASP audit finding V-01).
 */
public final class SaleReceiptFormatter {

    private SaleReceiptFormatter() {
        // utility class, no instances
    }

    /**
     * Builds a readable receipt for the given sale, showing the
     * subtotal, additional charges, the discount applied (if any),
     * and the final total.
     *
     * @param sale the sale to format
     * @return a formatted, multi-line receipt
     */
    public static String format(Sale sale) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Recibo de venta ").append(sale.getId()).append("\n");
        receipt.append("Fecha: ").append(sale.getDate()).append("\n");
        receipt.append("Cliente: ").append(sale.getCustomer().getName()).append("\n");
        receipt.append("Vendedor: ").append(sale.getSeller().getName()).append("\n");
        receipt.append("Productos: ").append(sale.getProducts().size()).append("\n");
        receipt.append("Subtotal: ").append(sale.getTotal()).append("\n");
        if (sale.getAdditionalCost() > 0) {
            receipt.append("Cargos adicionales (garantías extendidas): ").append(sale.getAdditionalCost()).append("\n");
        }
        if (sale.getAppliedPromotionName() != null) {
            receipt.append("Descuento aplicado (").append(sale.getAppliedPromotionName())
                    .append("): -").append(sale.getDiscountAmount()).append("\n");
        } else {
            receipt.append("Descuento aplicado: ninguno\n");
        }
        receipt.append("Total final: ").append(sale.getFinalTotal());
        return receipt.toString();
    }
}