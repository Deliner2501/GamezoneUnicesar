package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Converts a {@link Sale} to and from a single line of the sales file.
 * Format: id;date;customerId;sellerId;productId1,productId2,...;promotionName;discountAmount
 * <p>
 * This class only knows the text format. Reading and writing the file
 * itself is the job of {@link SaleDAO} (SOLID audit finding V-02).
 */
public class SaleCsvMapper {

    private static final String FIELD_SEPARATOR = ";";
    private static final String PRODUCT_SEPARATOR = ",";

    private final PersonDAO personDAO;
    private final ProductDAO productDAO;

    /**
     * Creates a mapper that resolves customer, seller and product
     * references through the given DAOs when a line is converted back
     * into a sale.
     *
     * @param personDAO  the DAO used to look up customers and sellers by id
     * @param productDAO the DAO used to look up products by id
     */
    public SaleCsvMapper(PersonDAO personDAO, ProductDAO productDAO) {
        this.personDAO = personDAO;
        this.productDAO = productDAO;
    }

    /**
     * Converts a Sale into a single line of text for storage.
     *
     * @param sale the sale to convert
     * @return the line to write to the sales file
     */
    public String toLine(Sale sale) {
        StringBuilder productIds = new StringBuilder();
        List<Product> products = sale.getProducts();
        for (int i = 0; i < products.size(); i++) {
            productIds.append(products.get(i).getId());
            if (i < products.size() - 1) {
                productIds.append(PRODUCT_SEPARATOR);
            }
        }

        String promotionName = sale.getAppliedPromotionName() == null ? "" : sale.getAppliedPromotionName();

        return sale.getId() + FIELD_SEPARATOR
                + sale.getDate() + FIELD_SEPARATOR
                + sale.getCustomer().getId() + FIELD_SEPARATOR
                + sale.getSeller().getId() + FIELD_SEPARATOR
                + productIds + FIELD_SEPARATOR
                + promotionName + FIELD_SEPARATOR
                + sale.getDiscountAmount();
    }

    /**
     * Reconstructs a Sale from a stored line of text, resolving the
     * customer, seller and product ids back into real objects.
     *
     * @param line a line of the sales file
     * @return the sale, or null if its customer or seller no longer exists
     * @throws IOException if the people or products files cannot be read
     */
    public Sale fromLine(String line) throws IOException {
        String[] parts = line.split(FIELD_SEPARATOR);
        String id = parts[0];
        LocalDate date = LocalDate.parse(parts[1]);
        String customerId = parts[2];
        String sellerId = parts[3];

        Customer customer = findCustomerById(customerId);
        Seller seller = findSellerById(sellerId);
        if (customer == null || seller == null) {
            System.out.println("Skipping sale " + id + ": customer or seller not found");
            return null;
        }

        Sale sale = new Sale(id, date, customer, seller);

        if (parts.length > 4 && !parts[4].isBlank()) {
            String[] productIds = parts[4].split(PRODUCT_SEPARATOR);
            for (String productId : productIds) {
                Product product = productDAO.findById(productId);
                if (product != null) {
                    sale.addProduct(product);
                }
            }
        }

        if (parts.length > 6 && !parts[5].isBlank()) {
            String promotionName = parts[5];
            double discountAmount = Double.parseDouble(parts[6]);
            sale.applyPromotion(promotionName, discountAmount);
        }

        return sale;
    }

    private Customer findCustomerById(String id) throws IOException {
        for (Customer customer : personDAO.loadCustomers()) {
            if (customer.getId().equals(id)) {
                return customer;
            }
        }
        return null;
    }

    private Seller findSellerById(String id) throws IOException {
        for (Seller seller : personDAO.loadSellers()) {
            if (seller.getId().equals(id)) {
                return seller;
            }
        }
        return null;
    }
}
