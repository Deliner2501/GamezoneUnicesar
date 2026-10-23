package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Promotion;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence operations for promotions, storing and retrieving
 * them from a CSV file. A discriminator column ("PERCENTAGE", "CATEGORY"
 * or "BULK") is used to tell the three concrete promotion types apart
 * when reloading the file.
 */
public class PromotionDAO implements PromotionRepository {

    private static final String FILE_PATH = "data/promotions.csv";
    private final PromotionCsvMapper mapper = new PromotionCsvMapper();

    /**
     * Persists the full list of promotions, overwriting the file's
     * previous content.
     *
     * @param promotions the complete list of promotions to save
     */
    @Override
    public void saveAll(List<Promotion> promotions) {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Promotion p : promotions) {
                writer.println(mapper.toCsvLine(p));
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al escribir el archivo " + FILE_PATH + " (operación de escritura)", e);
        }
    }

    /**
     * Returns all promotions currently stored in the file.
     *
     * @return the list of all persisted promotions, or an empty list
     *         if the file does not exist yet
     */
    @Override
    public List<Promotion> loadAll() {
        List<Promotion> promotions = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return promotions;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    Promotion p = mapper.parseLine(line);
                    if (p != null) promotions.add(p);
                } catch (Exception e) {
                    // Corrupted line: skipped so one bad row does not block the whole file.
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al leer el archivo " + FILE_PATH + " (operación de lectura)", e);
        }
        return promotions;
    }

}