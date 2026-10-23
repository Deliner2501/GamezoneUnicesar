package com.gamezone.persistence;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles saving and loading Return data to and from a text file.
 * To keep the persistence layer independent from the service layer,
 * this repository does not resolve sale or product references on its
 * own: it only reads and writes raw identifiers. Turning a stored
 * record back into a real Return object is the responsibility of
 * ReturnService.
 */
public class CsvReturnRepository implements ReturnRepository {

    private static final String FILE_PATH = "data/returns.csv";
    private static final String FIELD_SEPARATOR = ";";
    private static final String PRODUCT_SEPARATOR = ",";

    /**
     * A raw, unresolved return record as stored on disk: just ids,
     * dates and amounts, with no references to actual Sale or Product
     * objects.
     */
    public static class ReturnRecord {
    }

    /**
     * Persists the complete list of returns, replacing the previous
     * content of the file.
     *
     * @param returns the full list of returns to persist
     * @throws IOException if the returns cannot be written to disk
     */
    @Override
    public void saveAll(List<Return> returns) throws IOException {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Return r : returns) {
                writer.write(toLine(r));
                writer.newLine();
            }
        }
    }

    /**
     * Loads and returns every return record stored in the returns file,
     * without resolving sale or product references.
     *
     * @return the list of all persisted return records, or an empty
     *         list if the file does not exist yet
     * @throws IOException if the file cannot be read
     */
    @Override
    public List<ReturnRecord> loadAll() throws IOException {
        List<ReturnRecord> records = new ArrayList<>();
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
     * Converts a Return into a single line of text for storage.
     * Format: id;date;saleId;productId1,productId2,...;reason;refundAmount;warrantyRefund
     */
    private String toLine(Return r) {
        StringBuilder productIds = new StringBuilder();
        List<Product> products = r.getReturnedProducts();
        for (int i = 0; i < products.size(); i++) {
            productIds.append(products.get(i).getId());
            if (i < products.size() - 1) {
                productIds.append(PRODUCT_SEPARATOR);
            }
        }

        return r.getId() + FIELD_SEPARATOR
                + r.getDate() + FIELD_SEPARATOR
                + r.getOriginalSale().getId() + FIELD_SEPARATOR
                + productIds + FIELD_SEPARATOR
                + r.getReason() + FIELD_SEPARATOR
                + r.getRefundAmount() + FIELD_SEPARATOR
                + r.getWarrantyRefund();
    }

    /**
     * Parses a stored line of text into a raw return record.
     */
    private ReturnRecord fromLine(String line) {
        String[] parts = line.split(FIELD_SEPARATOR, -1);
        String id = parts[0];
        LocalDate date = LocalDate.parse(parts[1]);
        String saleId = parts[2];

        List<String> productIds = new ArrayList<>();
        if (parts.length > 3 && !parts[3].isBlank()) {
            productIds.addAll(Arrays.asList(parts[3].split(PRODUCT_SEPARATOR)));
        }

        String reason = parts.length > 4 ? parts[4] : "";
        double warrantyRefund = (parts.length > 6 && !parts[6].isBlank())
                ? Double.parseDouble(parts[6]) : 0.0;
        boolean refundCalculated = parts.length > 5 && !parts[5].isBlank();

        return new ReturnRecord(id, date, saleId, productIds, reason, warrantyRefund, refundCalculated);
    }
}