package com.gamezone.persistence;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Customer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading customers to and from a text file.
 * Format: id;name;phone;email, without a header line.
 */
public class CustomerDAO implements CustomerRepository {

    private static final String CUSTOMERS_FILE = "data/customers.txt";
    private static final String CUSTOMERS_SEPARATOR = ";";

    @Override
    public void saveCustomers(List<Customer> customers) {
        if (customers == null) {
            throw new InvalidDataException("lista de clientes", "no puede ser nula");
        }

        File file = new File(CUSTOMERS_FILE);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Customer customer : customers) {
                String line = customer.getId() + CUSTOMERS_SEPARATOR
                        + customer.getName() + CUSTOMERS_SEPARATOR
                        + customer.getPhone() + CUSTOMERS_SEPARATOR
                        + customer.getEmail();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al escribir el archivo " + CUSTOMERS_FILE + " (operación de escritura)", e);
        }
    }

    @Override
    public List<Customer> loadCustomers() {
        List<Customer> customers = new ArrayList<>();
        File file = new File(CUSTOMERS_FILE);

        if (!file.exists()) {
            return customers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(CUSTOMERS_SEPARATOR);
                customers.add(new Customer(parts[1], parts[0], parts[2], parts[3]));
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al leer el archivo " + CUSTOMERS_FILE + " (operación de lectura)", e);
        }
        return customers;
    }
}