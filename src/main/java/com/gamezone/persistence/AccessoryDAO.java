package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import com.gamezone.exceptions.PersistenceException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence operations for accessories, storing and retrieving
 * them from a CSV file. A discriminator column ("CONTROLLER", "CABLE" or
 * "MEMORY") is used to tell the three concrete accessory types apart
 * when reloading the file.
 */
  public class AccessoryDAO implements AccessoryRepository {

    private static final String FILE_PATH = "data/accessories.csv";
    private final AccessoryCsvMapper mapper = new AccessoryCsvMapper();
    
    /**
     * Saves a single accessory by appending it to the file.
     *
     * @param accessory the accessory to persist
     */
    @Override
    public void save(Accessory accessory) {
        List<Accessory> accessories = findAll();
        accessories.add(accessory);
        writeAll(accessories);
    }

    /**
     * Returns all accessories currently stored in the file.
     *
     * @return the list of all persisted accessories, or an empty list
     *         if the file does not exist yet
     */
    @Override
    public List<Accessory> findAll() {
        List<Accessory> accessories = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return accessories;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    Accessory a = mapper.parseLine(line);
                    if (a != null) accessories.add(a);
                } catch (Exception e) {
                    // Corrupted line: skipped so one bad row does not block the whole file.
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al leer el archivo " + FILE_PATH + " (operación de lectura)", e);
        }
        return accessories;
    }

    /**
     * Finds an accessory by its id.
     *
     * @param id the id of the accessory to find
     * @return the matching accessory, or null if none is found
     */
    @Override
    public Accessory findById(String id) {
        for (Accessory a : findAll()) {
            if (a.getId().equals(id)) return a;
        }
        return null;
    }

    /**
     * Updates an existing accessory, replacing it by matching id.
     *
     * @param accessory the accessory with updated data
     */
    @Override
    public void update(Accessory accessory) {
        List<Accessory> accessories = findAll();
        for (int i = 0; i < accessories.size(); i++) {
            if (accessories.get(i).getId().equals(accessory.getId())) {
                accessories.set(i, accessory);
                break;
            }
        }
        writeAll(accessories);
    }

    private void writeAll(List<Accessory> accessories) {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Accessory a : accessories) {
                writer.println(mapper.toCsvLine(a));
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error al escribir el archivo " + FILE_PATH + " (operación de escritura)", e);
        }
    }

    
}