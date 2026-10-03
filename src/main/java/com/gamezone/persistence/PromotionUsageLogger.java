package com.gamezone.persistence;

import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import java.io.FileWriter;
import java.io.IOException;

/**
 * AFTER: applies Pure Fabrication.
 * This class does not represent any concept from the GameZone domain —
 * there is no real-world "usage logger" that a customer or a seller
 * would recognize. It exists purely to fulfill a technical
 * responsibility: recording which promotion was applied to which sale.
 * This keeps domain classes like Promotion focused on business logic
 * only, instead of taking on file-access duties that don't belong to
 * them.
 */
public class PromotionUsageLogger {

    /**
     * Appends a record of an applied promotion to the usage log file.
     *
     * @param sale       the sale the promotion was applied to
     * @param promotion  the promotion that was applied
     * @param discount   the discount amount granted
     */
    public void log(Sale sale, Promotion promotion, double discount) {
        try (FileWriter writer = new FileWriter("data/promotion-usage.log", true)) {
            writer.write(sale.getDate() + "," + promotion.getId() + "," + promotion.getName() + "," + discount + "\n");
        } catch (IOException e) {
            System.out.println("No se pudo registrar el uso de la promoción: " + e.getMessage());
        }
    }
}
