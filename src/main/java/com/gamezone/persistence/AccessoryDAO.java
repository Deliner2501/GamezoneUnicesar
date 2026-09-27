package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles persistence operations for accessories, storing and retrieving
 * them from a CSV file. A discriminator column ("CONTROLLER", "CABLE" or
 * "MEMORY") is used to tell the three concrete accessory types apart
 * when reloading the file.
 */
public class AccessoryDAO {

    private static final String FILE_PATH = "data/accessories.csv";
    private static final String CONSOLE_LIST_SEPARATOR = ";";

    /**
     * Saves a single accessory by appending it to the file.
     *
     * @param accessory the accessory to persist
     */
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
    public List<Accessory> findAll() {
        List<Accessory> accessories = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return accessories;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    Accessory a = parseLine(line);
                    if (a != null) accessories.add(a);
                } catch (Exception e) {
                    System.out.println("Skipping corrupted line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading accessories: " + e.getMessage());
        }
        return accessories;
    }

    /**
     * Finds an accessory by its id.
     *
     * @param id the id of the accessory to find
     * @return the matching accessory, or null if none is found
     */
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
                String consoles = String.join(CONSOLE_LIST_SEPARATOR, a.getCompatibleConsoleIds());
                if (a instanceof Controller) {
                    Controller c = (Controller) a;
                    writer.println("CONTROLLER," + c.getId() + "," + c.getTitle() + ","
                            + c.getPrice() + "," + c.getStock() + ","
                            + c.getConnectionType() + "," + consoles);
                } else if (a instanceof Cable) {
                    Cable c = (Cable) a;
                    writer.println("CABLE," + c.getId() + "," + c.getTitle() + ","
                            + c.getPrice() + "," + c.getStock() + ","
                            + c.getLengthInMeters() + "," + c.getConnectorType() + "," + consoles);
                } else if (a instanceof Memory) {
                    Memory m = (Memory) a;
                    writer.println("MEMORY," + m.getId() + "," + m.getTitle() + ","
                            + m.getPrice() + "," + m.getStock() + ","
                            + m.getCapacityInGigabytes() + "," + m.getMemoryType() + "," + consoles);
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving accessories: " + e.getMessage());
        }
    }

    private Accessory parseLine(String line) {
        String[] parts = line.split(",", -1);
        String type = parts[0];
        String id = parts[1];
        String title = parts[2];
        double price = Double.parseDouble(parts[3]);
        int stock = Integer.parseInt(parts[4]);

        Accessory accessory = null;

        if (type.equals("CONTROLLER")) {
            String connectionType = parts[5];
            accessory = new Controller(id, title, price, stock, connectionType);
            applyCompatibleConsoles(accessory, parts, 6);
        } else if (type.equals("CABLE")) {
            double lengthInMeters = Double.parseDouble(parts[5]);
            String connectorType = parts[6];
            accessory = new Cable(id, title, price, stock, lengthInMeters, connectorType);
            applyCompatibleConsoles(accessory, parts, 7);
        } else if (type.equals("MEMORY")) {
            int capacityInGigabytes = Integer.parseInt(parts[5]);
            String memoryType = parts[6];
            accessory = new Memory(id, title, price, stock, capacityInGigabytes, memoryType);
            applyCompatibleConsoles(accessory, parts, 7);
        }

        return accessory;
    }

    private void applyCompatibleConsoles(Accessory accessory, String[] parts, int consolesIndex) {
        if (parts.length <= consolesIndex || parts[consolesIndex].isBlank()) {
            return;
        }
        List<String> consoleIds = Arrays.asList(parts[consolesIndex].split(CONSOLE_LIST_SEPARATOR));
        accessory.setCompatibleConsoleIds(consoleIds);
    }
}