package com.gamezone.persistence;

import com.gamezone.model.Return;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Storage contract for returns. Services depend on this interface instead of
 * on a concrete CSV class (SOLID audit finding V-26). It works with raw
 * records so that persistence does not depend on the service layer.
 */
public interface ReturnRepository {

    /**
     * Raw representation of a stored return, before its sale and products
     * are resolved into real objects.
     */
    class ReturnRecord {
        private String id;
        private LocalDate date;
        private String saleId;
        private List<String> productIds;
        private String reason;
        private double warrantyRefund;
        private boolean refundCalculated;

        public ReturnRecord(String id, LocalDate date, String saleId, List<String> productIds,
                            String reason, double warrantyRefund, boolean refundCalculated) {
            this.id = id;
            this.date = date;
            this.saleId = saleId;
            this.productIds = productIds;
            this.reason = reason;
            this.warrantyRefund = warrantyRefund;
            this.refundCalculated = refundCalculated;
        }

        public String getId() {
            return id;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getSaleId() {
            return saleId;
        }

        public List<String> getProductIds() {
            return productIds;
        }

        public String getReason() {
            return reason;
        }

        public double getWarrantyRefund() {
            return warrantyRefund;
        }

        public boolean isRefundCalculated() {
            return refundCalculated;
        }
    }

    /**
     * Saves the whole list of returns, replacing the stored one.
     *
     * @param returns the returns to persist
     * @throws IOException if the returns cannot be written
     */
    void saveAll(List<Return> returns) throws IOException;

    /**
     * Loads every stored return as a raw record.
     *
     * @return the stored records, or an empty list if none exist
     * @throws IOException if the returns cannot be read
     */
    List<ReturnRecord> loadAll() throws IOException;
}