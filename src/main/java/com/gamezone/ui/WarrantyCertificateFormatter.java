package com.gamezone.ui;

import com.gamezone.model.Warranty;

/**
 * Builds the human-readable certificate text for a {@link Warranty}.
 * Certificate formatting is a presentation concern, so it is kept out of
 * the {@code Warranty} class (SOLID audit finding V-09: SRP).
 */
public final class WarrantyCertificateFormatter {

    private WarrantyCertificateFormatter() {
        // utility class, no instances
    }

    /**
     * Builds a readable certificate, in Spanish, showing the warranty id,
     * type, associated product, coverage dates and additional cost.
     *
     * @param warranty the warranty to format
     * @return a formatted, multi-line certificate
     */
    public static String format(Warranty warranty) {
        StringBuilder certificate = new StringBuilder();
        certificate.append("Certificado de garantía ").append(warranty.getId()).append("\n");
        certificate.append("Tipo: ").append(warranty.getWarrantyType()).append("\n");
        certificate.append("Producto: ").append(warranty.getProduct().getTitle()).append("\n");
        certificate.append("Fecha de inicio: ").append(warranty.getStartDate()).append("\n");
        certificate.append("Fecha de fin: ").append(warranty.getEndDate()).append("\n");
        certificate.append("Costo adicional: ").append(warranty.getAdditionalCost());
        return certificate.toString();
    }
}