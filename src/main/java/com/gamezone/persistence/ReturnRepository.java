package com.gamezone.persistence;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.service.AccessoryService;
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
 * Handles saving and loading Return data to and from a text file.
 * Since a return references an original sale and a list of returned
 * products, this class relies on SaleService and ProductService to
 * resolve those references back into real objects when a return is
 * loaded from disk.
 */
public class ReturnRepository {

    private static final String FILE_PATH = "data/returns.csv";
    private static final String FIELD_SEPARATOR = ";";
    private static final String PRODUCT_SEPARATOR = ",";

    private SaleService saleService;
    private ProductService productService;
    private AccessoryService accessoryService;

    /**
     * Creates a ReturnRepository that uses the given SaleService,
     * ProductService and AccessoryService to resolve sale, product
     * and accessory references when loading returns.
     *
     * @param saleService      the service used to look up the original sale by id
     * @param productService   the service used to look up returned products by id
     * @param accessoryService the service used to look up returned accessories by id
     */
    public ReturnRepository(SaleService saleService, ProductService productService,
                             AccessoryService accessoryService) {
        this.saleService = saleService;
        this.productService = productService;
        this.accessoryService = accessoryService;
    }
    

    /**
     * Persists the complete list of returns, replacing the previous
     * content of the file.
     *
     * @param returns the full list of returns to persist
     * @throws IOException if the returns cannot be written to disk
     */
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
     * Loads and returns every return stored in the returns file.
     *
     * @return the list of all persisted returns, or an empty list
     *         if the file does not exist yet
     * @throws IOException if the file cannot be read
     */
    public List<Return> loadAll() throws IOException {
        List<Return> returns = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return returns;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Return r = fromLine(line);
                if (r != null) {
                    returns.add(r);
                }
            }
        }
        return returns;
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
     * Reconstructs a Return from a stored line of text, resolving the
     * original sale and the returned products back into real objects.
     */
    private Return fromLine(String line) throws IOException {
        String[] parts = line.split(FIELD_SEPARATOR, -1);
        String id = parts[0];
        LocalDate date = LocalDate.parse(parts[1]);
        String saleId = parts[2];

        Sale originalSale = saleService.findSaleById(saleId);
        if (originalSale == null) {
            System.out.println("Skipping return " + id + ": original sale not found");
            return null;
        }

        List<Product> returnedProducts = new ArrayList<>();
        if (parts.length > 3 && !parts[3].isBlank()) {
            String[] productIds = parts[3].split(PRODUCT_SEPARATOR);
            for (String productId : productIds) {
                Product product = productService.findProductById(productId);
                if (product == null) {
                    product = accessoryService.findById(productId);
                }
                if (product != null) {
                    returnedProducts.add(product);
                }
            }
        }
        if (returnedProducts.isEmpty()) {
            System.out.println("Skipping return " + id + ": no returned products could be resolved");
            return null;
        }

        String reason = parts.length > 4 ? parts[4] : "";
        double warrantyRefund = (parts.length > 6 && !parts[6].isBlank())
                ? Double.parseDouble(parts[6]) : 0.0;

        Return r = new Return(id, date, originalSale, returnedProducts, reason, warrantyRefund);
        if (parts.length > 5 && !parts[5].isBlank()) {
            r.calculateRefundAmount();
        }
        return r;
    }
}