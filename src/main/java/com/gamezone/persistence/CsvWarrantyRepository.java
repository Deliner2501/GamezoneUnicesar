package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Warranty;
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
 * Handles saving and loading Warranty data to and from a text file.
 * To avoid a circular dependency with SaleService (which needs to use
 * WarrantyService when registering a sale), this repository does not
 * resolve product or sale references on its own: it only reads and
 * writes raw identifiers. Turning a stored record back into a real
 * Warranty object is the responsibility of WarrantyService.
 */
public class CsvWarrantyRepository implements WarrantyRepository {

    private static final String FILE_PATH = "data/warranties.csv";
    private static final String FIELD_SEPARATOR = ";";
    private final WarrantyTypeMapper typeMapper = new WarrantyTypeMapper();


    /**
     * Persists the complete list of warranties, replacing the previous
     * content of the file.
     *
     * @param warranties the full list of warranties to persist
     * @throws PersistenceException if the warranties cannot be written to disk
     */
    @Override
    public void saveAll(List<Warranty> warranties) {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Warranty warranty : warranties) {
                writer.write(toLine(warranty));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al escribir el archivo " + FILE_PATH + " (operación de escritura)", e);
        }
    }

    /**
     * Loads and returns every warranty record stored in the warranties
     * file, without resolving product or sale references.
     *
     * @return the list of all persisted warranty records, or an empty
     *         list if the file does not exist yet
     * @throws PersistenceException if the file cannot be read
     */
    @Override
    public List<WarrantyRecord> loadAll() {
        List<WarrantyRecord> records = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return records;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                records.add(fromLine(line));
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al leer el archivo " + FILE_PATH + " (operación de lectura)", e);
        }
        return records;
    }

    /**
     * Converts a Warranty into a single line of text for storage.
     * Format: type;id;productId;saleId;startDate
     */
    private String toLine(Warranty warranty) {
        String type = typeMapper.toTypeCode(warranty);
        return type + FIELD_SEPARATOR
                + warranty.getId() + FIELD_SEPARATOR
                + warranty.getProduct().getId() + FIELD_SEPARATOR
                + warranty.getSale().getId() + FIELD_SEPARATOR
                + warranty.getStartDate();
    }

    /**
     * Parses a stored line of text into a raw warranty record.
     */
    private WarrantyRecord fromLine(String line) {
        String[] parts = line.split(FIELD_SEPARATOR, -1);
        String type = parts[0];
        String id = parts[1];
        String productId = parts[2];
        String saleId = parts[3];
        LocalDate startDate = LocalDate.parse(parts[4]);
        return new WarrantyRecord(type, id, productId, saleId, startDate);
    }
}