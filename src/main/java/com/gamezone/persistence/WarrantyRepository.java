package com.gamezone.persistence;

import com.gamezone.model.Warranty;
import java.time.LocalDate;
import java.util.List;

/**
 * Storage contract for warranties. Services depend on this interface instead
 * of on a concrete CSV class (SOLID audit finding V-26). It works with raw
 * records so that persistence does not depend on the service layer.
 */
public interface WarrantyRepository {

    /**
     * Raw representation of a stored warranty, before its product and sale
     * are resolved into real objects.
     */
    class WarrantyRecord {
        private String type;
        private String id;
        private String productId;
        private String saleId;
        private LocalDate startDate;

        public WarrantyRecord(String type, String id, String productId, String saleId, LocalDate startDate) {
            this.type = type;
            this.id = id;
            this.productId = productId;
            this.saleId = saleId;
            this.startDate = startDate;
        }

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        public String getProductId() {
            return productId;
        }

        public String getSaleId() {
            return saleId;
        }

        public LocalDate getStartDate() {
            return startDate;
        }
    }

    /**
     * Saves the whole list of warranties, replacing the stored one.
     *
     * @param warranties the warranties to persist
     */
    void saveAll(List<Warranty> warranties);

    /**
     * Loads every stored warranty as a raw record.
     *
     * @return the stored records, or an empty list if none exist
     */
    List<WarrantyRecord> loadAll();
}