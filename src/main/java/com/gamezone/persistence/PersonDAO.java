package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Customer and Seller data
 * to and from text files, so information persists between executions.
 */
public class PersonDAO {

    private static final String CUSTOMERS_FILE = "data/customers.txt";
    private static final String CUSTOMERS_SEPARATOR = ";";

    private static final String SELLERS_FILE = "data/sellers.csv";
    private static final String SELLERS_SEPARATOR = ",";

    /**
     * Saves the complete list of customers to the customers file,
     * overwriting any previous content.
     *
     * @param customers the list of customers to save
     * @throws IllegalArgumentException if the list is null
     * @throws IOException              if the file cannot be written
     */
    public void saveCustomers(List<Customer> customers) throws IOException {
        // Validamos que no nos pasen una lista nula antes de intentar escribir
        if (customers == null) {
            throw new IllegalArgumentException("La lista de clientes no puede ser nula");
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
        }
    }

    public List<Customer> loadCustomers() throws IOException {
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
        }
        return customers;
    }

    /**
     * Saves the complete list of sellers to the sellers file,
     * overwriting any previous content.
     *
     * @param sellers the list of sellers to save
     * @throws IllegalArgumentException if the list is null
     * @throws IOException              if the file cannot be written
     */
    public void saveSellers(List<Seller> sellers) throws IOException {
        // Misma validación defensiva, ahora para la lista de vendedores
        if (sellers == null) {
            throw new IllegalArgumentException("La lista de vendedores no puede ser nula");
        }

        File file = new File(SELLERS_FILE);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,name,phone,employeeCode,workShift");
            writer.newLine();

            for (Seller seller : sellers) {
                String line = seller.getId() + SELLERS_SEPARATOR
                        + seller.getName() + SELLERS_SEPARATOR
                        + seller.getPhone() + SELLERS_SEPARATOR
                        + seller.getEmployeeCode() + SELLERS_SEPARATOR
                        + seller.getWorkShift();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public List<Seller> loadSellers() throws IOException {
        List<Seller> sellers = new ArrayList<>();
        File file = new File(SELLERS_FILE);

        if (!file.exists()) {
            return sellers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] parts = line.split(SELLERS_SEPARATOR);
                sellers.add(new Seller(parts[1], parts[0], parts[2], parts[3], parts[4]));
            }
        }
        return sellers;
    }
}