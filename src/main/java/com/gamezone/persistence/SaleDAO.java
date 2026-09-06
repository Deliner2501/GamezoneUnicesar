
package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
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
 * Handles saving and loading Sale data to and from a text file.
 * Since a sale references a customer, a seller and a list of products,
 * this class relies on PersonDAO and ProductDAO to reconstruct those
 * objects from their stored ids when a sale is loaded.
 */
public class SaleDAO {

    private static final String FILE_PATH = "data/sales.txt";
    private static final String FIELD_SEPARATOR = ";";
    private static final String PRODUCT_SEPARATOR = ",";

    private PersonDAO personDAO;
    private ProductDAO productDAO;

    /**
     * Creates a SaleDAO that uses the given PersonDAO and ProductDAO
     * to resolve customer, seller and product references when loading sales.
     *
     * @param personDAO  the DAO used to look up customers and sellers by id
     * @param productDAO the DAO used to look up products by id
     */
    public SaleDAO(PersonDAO personDAO, ProductDAO productDAO) {
        this.personDAO = personDAO;
        this.productDAO = productDAO;
    }

    /**
     * Saves a single sale by appending it to the sales file.
     *
     * @param sale the sale to persist
     * @throws IOException if the sale cannot be written to disk
     */
    public void save(Sale sale) throws IOException {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(toLine(sale));
            writer.newLine();
        }
    }

    /**
     * Loads and returns every sale stored in the sales file.
     *
     * @return the list of all persisted sales, or an empty list
     *         if the file does not exist yet
     * @throws IOException if the file cannot be read
     */
    public List<Sale> findAll() throws IOException {
        List<Sale> sales = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return sales;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Sale sale = fromLine(line);
                if (sale != null) {
                    sales.add(sale);
                }
            }
        }
        return sales;
    }

    /**
     * Returns every sale made by the customer with the given id.
     *
     * @param customerId the id of the customer
     * @return the list of sales made by that customer
     * @throws IOException if the sales file cannot be read
     */
    public List<Sale> findByCustomer(String customerId) throws IOException {
        List<Sale> result = new ArrayList<>();
        for (Sale sale : findAll()) {
            if (sale.getCustomer().getId().equals(customerId)) {
                result.add(sale);
            }
        }
        return result;
    }

    /**
     * Returns every sale attended by the seller with the given id.
     *
     * @param sellerId the id of the seller
     * @return the list of sales attended by that seller
     * @throws IOException if the sales file cannot be read
     */
    public List<Sale> findBySeller(String sellerId) throws IOException {
        List<Sale> result = new ArrayList<>();
        for (Sale sale : findAll()) {
            if (sale.getSeller().getId().equals(sellerId)) {
                result.add(sale);
            }
        }
        return result;
    }

    /**
     * Converts a Sale into a single line of text for storage.
     * Format: id;date;customerId;sellerId;productId1,productId2,...
     */
    private String toLine(Sale sale) {
        StringBuilder productIds = new StringBuilder();
        List<Product> products = sale.getProducts();
        for (int i = 0; i < products.size(); i++) {
            productIds.append(products.get(i).getId());
            if (i < products.size() - 1) {
                productIds.append(PRODUCT_SEPARATOR);
            }
        }

        return sale.getId() + FIELD_SEPARATOR
                + sale.getDate() + FIELD_SEPARATOR
                + sale.getCustomer().getId() + FIELD_SEPARATOR
                + sale.getSeller().getId() + FIELD_SEPARATOR
                + productIds;
    }

    /**
     * Reconstructs a Sale from a stored line of text, resolving the
     * customer, seller and product ids back into real objects.
     */
    private Sale fromLine(String line) throws IOException {
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