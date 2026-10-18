package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Sale;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Sale data to and from a text file.
 * Only the file access lives here; the text format of each line is
 * handled by {@link SaleCsvMapper} (SOLID audit finding V-02).
 */
public class SaleDAO {

    private static final String FILE_PATH = "data/sales.txt";
    private final SaleCsvMapper mapper;

    /**
     * Creates a SaleDAO that uses the given PersonDAO and ProductDAO
     * to resolve customer, seller and product references when loading sales.
     *
     * @param personDAO  the DAO used to look up customers and sellers by id
     * @param productDAO the DAO used to look up products by id
     */
    public SaleDAO(PersonDAO personDAO, ProductDAO productDAO) {
        this.mapper = new SaleCsvMapper(personDAO, productDAO);
    }

    /**
     * Saves a single sale by appending it to the sales file.
     *
     * @param sale the sale to persist
     * @throws PersistenceException if the sale cannot be written to disk
     * @throws IOException declared for API compatibility; write failures
     *         are now wrapped as PersistenceException instead
     */
    public void save(Sale sale) throws IOException {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(mapper.toLine(sale));
            writer.newLine();
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al escribir el archivo " + FILE_PATH + " (operación de escritura)", e);
        }
    }

    /**
     * Loads and returns every sale stored in the sales file.
     *
     * @return the list of all persisted sales, or an empty list
     *         if the file does not exist yet
     * @throws PersistenceException if the file cannot be read
     * @throws IOException declared for API compatibility; read failures
     *         are now wrapped as PersistenceException instead
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
                Sale sale = mapper.fromLine(line);
                if (sale != null) {
                    sales.add(sale);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al leer el archivo " + FILE_PATH + " (operación de lectura)", e);
        }
        return sales;
    }

        /**
     * Returns every sale made by the customer with the given id.
     *
     * @param customerId the id of the customer
     * @return the list of sales made by that customer
     * @throws PersistenceException if the sales file cannot be read
     * @throws IOException declared for API compatibility; read failures
     *         are now wrapped as PersistenceException instead
     */
    public List<Sale> findByCustomer(String customerId) throws IOException {
        List<Sale> result = new ArrayList<>();
        try {
            for (Sale sale : findAll()) {
                if (sale.getCustomer().getId().equals(customerId)) {
                    result.add(sale);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al leer el archivo " + FILE_PATH + " (operación de lectura)", e);
        }
        return result;
    }

        /**
     * Returns every sale attended by the seller with the given id.
     *
     * @param sellerId the id of the seller
     * @return the list of sales attended by that seller
     * @throws PersistenceException if the sales file cannot be read
     * @throws IOException declared for API compatibility; read failures
     *         are now wrapped as PersistenceException instead
     */
    public List<Sale> findBySeller(String sellerId) throws IOException {
        List<Sale> result = new ArrayList<>();
        try {
            for (Sale sale : findAll()) {
                if (sale.getSeller().getId().equals(sellerId)) {
                    result.add(sale);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al leer el archivo " + FILE_PATH + " (operación de lectura)", e);
        }
        return result;
    }
}