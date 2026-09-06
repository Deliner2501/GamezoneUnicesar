package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.ProductDAO;
import com.gamezone.persistence.SaleDAO;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the business rules for registering and consulting sales.
 * A sale must contain at least one product, and enough stock must be
 * available for every product before the sale is confirmed. Registering
 * a sale automatically reduces the stock of every product involved.
 */
public class SaleService {

    private SaleDAO saleDAO;
    private ProductDAO productDAO;
    private PersonService personService;

    /**
     * Creates a SaleService with its required collaborators.
     *
     * @param saleDAO       the DAO used to persist and query sales
     * @param productDAO    the DAO used to look up and update product stock
     * @param personService the service used to resolve customers and sellers by id
     */
    public SaleService(SaleDAO saleDAO, ProductDAO productDAO, PersonService personService) {
        this.saleDAO = saleDAO;
        this.productDAO = productDAO;
        this.personService = personService;
    }

    /**
     * Registers a new sale for the given customer and seller, buying the
     * requested quantity of each product. The sale is rejected if the
     * product map is empty, if any product does not exist, or if any
     * product does not have enough stock available. If the sale is
     * accepted, the stock of every involved product is reduced and
     * the sale is persisted.
     *
     * @param saleId            the unique identifier for the new sale
     * @param date              the date of the sale
     * @param customerId        the id of the customer making the purchase
     * @param sellerId          the id of the seller attending the sale
     * @param productQuantities a map of product id to quantity purchased
     * @return the registered sale, with its total already calculated
     * @throws IllegalArgumentException if the product list is empty, a
     *         customer/seller/product is not found, or stock is insufficient
     * @throws IOException if the sale cannot be persisted
     */
    public Sale registerSale(String saleId, LocalDate date, String customerId,
                              String sellerId, Map<String, Integer> productQuantities) throws IOException {

        if (productQuantities == null || productQuantities.isEmpty()) {
            throw new IllegalArgumentException("A sale must contain at least one product");
        }

        Customer customer = personService.findCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }

        Seller seller = personService.findSellerById(sellerId);
        if (seller == null) {
            throw new IllegalArgumentException("Seller not found: " + sellerId);
        }

        // First pass: validate everything BEFORE touching any stock,
        // so a failure halfway through never leaves inventory in a bad state.
        Map<Product, Integer> resolvedProducts = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            Product product = productDAO.findById(entry.getKey());
            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + entry.getKey());
            }
            int quantity = entry.getValue();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for product: " + entry.getKey());
            }
            if (product.getStock() < quantity) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product: " + product.getTitle()
                                + " (available: " + product.getStock() + ", requested: " + quantity + ")");
            }
            resolvedProducts.put(product, quantity);
        }

        // Second pass: everything is valid, so build the sale and commit the changes.
        Sale sale = new Sale(saleId, date, customer, seller);
        for (Map.Entry<Product, Integer> entry : resolvedProducts.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();

            for (int i = 0; i < quantity; i++) {
                sale.addProduct(product);
            }

            product.reduceStock(quantity);
            productDAO.update(product);
        }

        saleDAO.save(sale);
        customer.addPurchase(sale);

        return sale;
    }

    /**
     * Returns the complete history of sales registered in the store.
     *
     * @return the list of all sales
     * @throws IOException if the sales file cannot be read
     */
    public List<Sale> listSales() throws IOException {
        return saleDAO.findAll();
    }

    /**
     * Returns the purchase history of a specific customer.
     *
     * @param customerId the id of the customer
     * @return the list of sales made by that customer
     * @throws IOException if the sales file cannot be read
     */
    public List<Sale> listSalesByCustomer(String customerId) throws IOException {
        return saleDAO.findByCustomer(customerId);
    }

    /**
     * Returns the sales attended by a specific seller.
     *
     * @param sellerId the id of the seller
     * @return the list of sales attended by that seller
     * @throws IOException if the sales file cannot be read
     */
    public List<Sale> listSalesBySeller(String sellerId) throws IOException {
        return saleDAO.findBySeller(sellerId);
    }
}
