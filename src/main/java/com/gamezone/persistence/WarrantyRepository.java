package com.gamezone.persistence;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
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
 * Since a warranty references an associated product and sale, this
 * class relies on SaleService and ProductService to resolve those
 * references back into real objects when a warranty is loaded from
 * disk. A discriminator field is stored per line to tell BasicWarranty
 * and ExtendedWarranty records apart when reconstructing them.
 */
public class WarrantyRepository {

    private static final String FILE_PATH = "data/warranties.csv";
    private static final String FIELD_SEPARATOR = ";";
    private static final String BASIC_TYPE = "BASIC";
    private static final String EXTENDED_TYPE = "EXTENDED";

    private SaleService saleService;
    private ProductService productService;

    /**
     * Creates a WarrantyRepository that uses the given SaleService and
     * ProductService to resolve sale and product references when
     * loading warranties.
     *
     * @param saleService    the service used to look up the associated sale by id
     * @param productService the service used to look up the associated product by id
     */
    public WarrantyRepository(SaleService saleService, ProductService productService) {
        this.saleService = saleService;
        this.productService = productService;
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
     * Loads and returns every warranty stored in the warranties file.
     *
     * @return the list of all persisted warranties, or an empty list
     *         if the file does not exist yet
     * @throws IOException if the file cannot be read
     */
    public List<Warranty> loadAll() throws IOException {
        List<Warranty> warranties = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return warranties;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Warranty warranty = fromLine(line);
                if (warranty != null) {
                    warranties.add(warranty);
                }
            }
        }
        return warranties;
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
     * Reconstructs a Warranty from a stored line of text, resolving the
     * associated product and sale back into real objects and creating
     * the correct concrete subclass based on the stored discriminator.
     */
    private Warranty fromLine(String line) throws IOException {
        String[] parts = line.split(FIELD_SEPARATOR, -1);
        String type = parts[0];
        String id = parts[1];
        String productId = parts[2];
        String saleId = parts[3];
        LocalDate startDate = LocalDate.parse(parts[4]);

        Product product = productService.findProductById(productId);
        if (product == null) {
            System.out.println("Skipping warranty " + id + ": product not found");
            return null;
        }

        Sale sale = saleService.findSaleById(saleId);
        if (sale == null) {
            System.out.println("Skipping warranty " + id + ": sale not found");
            return null;
        }

        if (EXTENDED_TYPE.equals(type)) {
            return new ExtendedWarranty(id, product, sale, startDate);
        }
        return new BasicWarranty(id, product, sale, startDate);
    }
}