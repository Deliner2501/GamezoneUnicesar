package com.gamezone.persistence;

import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Warranty;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Warranty data to and from a text file.
 * To avoid a circular dependency with SaleService (which needs to use
 * WarrantyService when registering a sale), this repository does not
 * resolve product or sale references on its own: it only reads and
 * writes raw identifiers. Turning a stored record back into a real
 * Warranty object is the responsibility of WarrantyService.
 */
public class WarrantyRepository {

    private static final String FILE_PATH = "data/warranties.csv";
    private static final String FIELD_SEPARATOR = ";";
    private static final String EXTENDED_TYPE = "EXTENDED";
    private static final String BASIC_TYPE = "BASIC";

    /**
     * A raw, unresolved warranty record as stored on disk: just ids
     * and dates, with no references to actual Product or Sale objects.
     */
    public static class WarrantyRecord {
        private String type;
        private String id;
        private String productId;
        private String saleId;
        private LocalDate startDate;

        /**
         * Creates a raw warranty record.
         *
         * @param type      the discriminator ("BASIC" or "EXTENDED")
         * @param id        the warranty id
         * @param productId the id of the covered product
         * @param saleId    the id of the associated sale
         * @param startDate the date the warranty coverage starts
         */
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
     * Persists the complete list of warranties, replacing the previous
     * content of the file.
     *
     * @param warranties the full list of warranties to persist
     * @throws IOException if the warranties cannot be written to disk
     */
    public void saveAll(List<Warranty> warranties) throws IOException {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Warranty warranty : warranties) {
                writer.write(toLine(warranty));
                writer.newLine();
            }
        }
    }

    /**
     * Loads and returns every warranty record stored in the warranties
     * file, without resolving product or sale references.
     *
     * @return the list of all persisted warranty records, or an empty
     *         list if the file does not exist yet
     * @throws IOException if the file cannot be read
     */
    public List<WarrantyRecord> loadAll() throws IOException {
        List<WarrantyRecord> records = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return records;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                records.add(fromLine(line));
            }
        }
        return records;
    }

    /**
     * Converts a Warranty into a single line of text for storage.
     * Format: type;id;productId;saleId;startDate
     */
    private String toLine(Warranty warranty) {
        String type = (warranty instanceof ExtendedWarranty) ? EXTENDED_TYPE : BASIC_TYPE;
        return type + FIELD_SEPARATOR
                + warranty.getId() + FIELD_SEPARATOR
                + warranty.getProduct().getId() + FIELD_SEPARATOR
                + warranty.getSale().getId() + FIELD_SEPARATOR
                + warranty.getStartDate();
    }

    /**
     * Parses a stored line of text into a raw warranty record.
     */
    private WarrantyRecord fromLine(String line) {
        String[] parts = line.split(FIELD_SEPARATOR, -1);
        String type = parts[0];
        String id = parts[1];
        String productId = parts[2];
        String saleId = parts[3];
        LocalDate startDate = LocalDate.parse(parts[4]);
        return new WarrantyRecord(type, id, productId, saleId, startDate);
    }
}