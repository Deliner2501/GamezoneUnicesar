package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonDAO;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Handles the business rules for registering and listing
 * customers and sellers.
 */
public class PersonService {

    private PersonDAO personDAO;
    private List<Customer> customers;
    private List<Seller> sellers;

    /**
     * Creates the service and loads the existing customers and sellers
     * from persistence, so previously registered data is available
     * right after the application starts.
     *
     * @throws IOException if the stored data cannot be read
     */
    public PersonService() throws IOException {
        this.personDAO = new PersonDAO();
        this.customers = personDAO.loadCustomers();
        this.sellers = personDAO.loadSellers();
    }

    /**
     * Registers a new customer, validating that the id is not already used,
     * and persists the updated list immediately.
     *
     * @param name  the customer's full name
     * @param id    the customer's identification number
     * @param phone the customer's contact phone number
     * @param email the customer's email address
     * @throws IllegalArgumentException if a customer with the same id already exists
     * @throws IOException              if the data cannot be saved
     */
    public void registerCustomer(String name, String id, String phone, String email) throws IOException {
        for (Customer customer : customers) {
            if (customer.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe un cliente con el id: " + id);
            }
        }
        Customer newCustomer = new Customer(name, id, phone, email);
        customers.add(newCustomer);
        personDAO.saveCustomers(customers);
    }

    /**
     * Returns the list of all registered customers.
     * @return an unmodifiable view of the customers list
     */
    public List<Customer> listCustomers() {
        return Collections.unmodifiableList(customers);
    }

    /**
     * Returns the list of all registered sellers.
     * @return an unmodifiable view of the sellers list
     */
    public List<Seller> listSellers() {
        return Collections.unmodifiableList(sellers);
    }

    /**
     * Finds a customer by their id.
     * @param id the customer's identification number
     * @return the matching Customer, or null if not found
     */
    public Customer findCustomerById(String id) {
        for (Customer customer : customers) {
            if (customer.getId().equals(id)) {
                return customer;
            }
        }
        return null;
    }

    /**
     * Finds a seller by their id.
     * @param id the seller's identification number
     * @return the matching Seller, or null if not found
     */
    public Seller findSellerById(String id) {
        for (Seller seller : sellers) {
            if (seller.getId().equals(id)) {
                return seller;
            }
        }
        return null;
    }
}