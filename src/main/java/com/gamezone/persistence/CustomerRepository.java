package com.gamezone.persistence;

import com.gamezone.model.Customer;
import java.util.List;

/**
 * Storage contract for customers. Classes that only need customers depend on
 * this interface instead of on a concrete DAO (SOLID audit finding V-15).
 */
public interface CustomerRepository {

    /**
     * Saves the whole list of customers, replacing the stored one.
     *
     * @param customers the customers to persist
     */
    void saveCustomers(List<Customer> customers);

    /**
     * Loads every stored customer.
     *
     * @return the stored customers, or an empty list if none exist
     */
    List<Customer> loadCustomers();
}