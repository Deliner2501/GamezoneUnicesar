package com.gamezone.persistence;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
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
 * Handles saving and loading sellers to and from a CSV file.
 * Format: id,name,phone,employeeCode,workShift, with a header line.
 */
public class SellerDAO implements SellerRepository {

    private static final String SELLERS_FILE = "data/sellers.csv";
    private static final String SELLERS_SEPARATOR = ",";

    @Override
    public void saveSellers(List<Seller> sellers) {
        if (sellers == null) {
            throw new InvalidDataException("lista de vendedores", "no puede ser nula");
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
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al escribir el archivo " + SELLERS_FILE + " (operación de escritura)", e);
        }
    }

    @Override
    public List<Seller> loadSellers() {
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
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al leer el archivo " + SELLERS_FILE + " (operación de lectura)", e);
        }
        return sellers;
    }
}