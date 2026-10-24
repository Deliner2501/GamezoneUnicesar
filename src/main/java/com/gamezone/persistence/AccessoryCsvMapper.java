package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Converts accessories to and from their CSV line. It owns the storage
 * format in both directions, with a registry that maps each type code to
 * its parser (SOLID audit finding V-08).
 */
public class AccessoryCsvMapper {

    private static final String CONSOLE_LIST_SEPARATOR = ";";

    private final Map<String, Function<String[], Accessory>> parsers = Map.of(
            "CONTROLLER", this::parseController,
            "CABLE", this::parseCable,
            "MEMORY", this::parseMemory);

    /**
     * Builds the CSV line of an accessory.
     *
     * @param accessory the accessory to convert
     * @return the CSV line
     */
    public String toCsvLine(Accessory accessory) {
        String consoles = String.join(CONSOLE_LIST_SEPARATOR, accessory.getCompatibleConsoleIds());
        String common = accessory.getId() + "," + accessory.getTitle() + ","
                + accessory.getPrice() + "," + accessory.getStock() + ",";
        if (accessory instanceof Controller c) {
            return "CONTROLLER," + common + c.getConnectionType() + "," + consoles;
        } else if (accessory instanceof Cable c) {
            return "CABLE," + common + c.getLengthInMeters() + "," + c.getConnectorType() + "," + consoles;
        } else if (accessory instanceof Memory m) {
            return "MEMORY," + common + m.getCapacityInGigabytes() + "," + m.getMemoryType() + "," + consoles;
        }
        throw new IllegalArgumentException("Unsupported accessory type");
    }

    /**
     * Builds an accessory from a CSV line.
     *
     * @param line the CSV line
     * @return the accessory, or null if the type code is unknown
     */
    public Accessory parseLine(String line) {
        String[] parts = line.split(",", -1);
        Function<String[], Accessory> parser = parsers.get(parts[0]);
        return parser == null ? null : parser.apply(parts);
    }

    private Accessory parseController(String[] p) {
        Accessory a = new Controller(p[1], p[2], Double.parseDouble(p[3]), Integer.parseInt(p[4]), p[5]);
        applyConsoles(a, p, 6);
        return a;
    }

    private Accessory parseCable(String[] p) {
        Accessory a = new Cable(p[1], p[2], Double.parseDouble(p[3]), Integer.parseInt(p[4]),
                Double.parseDouble(p[5]), p[6]);
        applyConsoles(a, p, 7);
        return a;
    }

    private Accessory parseMemory(String[] p) {
        Accessory a = new Memory(p[1], p[2], Double.parseDouble(p[3]), Integer.parseInt(p[4]),
                Integer.parseInt(p[5]), p[6]);
        applyConsoles(a, p, 7);
        return a;
    }

    private void applyConsoles(Accessory accessory, String[] parts, int index) {
        if (parts.length <= index || parts[index].isBlank()) {
            return;
        }
        List<String> ids = Arrays.asList(parts[index].split(CONSOLE_LIST_SEPARATOR));
        accessory.setCompatibleConsoleIds(ids);
    }
}