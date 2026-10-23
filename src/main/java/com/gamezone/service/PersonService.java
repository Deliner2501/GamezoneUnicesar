package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;
import com.gamezone.persistence.CustomerRepository;
import com.gamezone.persistence.SellerRepository;
import com.gamezone.validation.PersonValidator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles the business rules for registering and listing
 * customers and sellers.
 */
public class PersonService {

    private CustomerRepository customerRepository;
    private SellerRepository sellerRepository;
    private List<Customer> customers;
    private List<Seller> sellers;

    /**
     * Creates the service and loads the existing customers and sellers
     * from persistence, so previously registered data is available
     * right after the application starts.
     *
     * @param customerRepository the repository used to persist and load customers
     * @param sellerRepository   the repository used to load sellers
     */
    public PersonService(CustomerRepository customerRepository, SellerRepository sellerRepository) {
        this.customerRepository = customerRepository;
        this.sellerRepository = sellerRepository;
        this.customers = customerRepository.loadCustomers();
        this.sellers = sellerRepository.loadSellers();
    }

    /**
     * Registers a new customer, validating the data and that the id is
     * not already used, and persists the updated list immediately.
     *
     * @param name  the customer's full name
     * @param id    the customer's identification number
     * @param phone the customer's contact phone number
     * @param email the customer's email address
     * @throws com.gamezone.exceptions.InvalidDataException if any field is invalid
     * @throws com.gamezone.exceptions.BusinessRuleException if a customer with the same id already exists
     */
    public void registerCustomer(String name, String id, String phone, String email) {
        PersonValidator.validatePersonData(id, name, email, phone);
        PersonValidator.validateUniquePerson(new ArrayList<Person>(customers), id);

        Customer newCustomer = new Customer(name, id, phone, email);
        customers.add(newCustomer);
        customerRepository.saveCustomers(customers);
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