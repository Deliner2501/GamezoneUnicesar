package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.persistence.AccessoryDAO;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the business rules for managing accessories: controllers,
 * cables and memories.
 */
public class AccessoryService {

    private AccessoryDAO accessoryDAO;

    public AccessoryService(AccessoryDAO accessoryDAO) {
        this.accessoryDAO = accessoryDAO;
    }

    /**
     * Registers a new controller and persists it.
     *
     * @param id             the unique id of the controller
     * @param title          the display title of the controller
     * @param price          the sale price
     * @param stock          the initial stock available
     * @param connectionType the connection type (e.g. "Wireless", "Wired")
     * @return the registered controller
     * @throws IllegalArgumentException if the id is empty or the price is negative
     */
    public Controller registerController(String id, String title, double price, int stock, String connectionType) {
        Controller controller = new Controller(id, title, price, stock, connectionType);
        validateAccessory(controller);
        accessoryDAO.save(controller);
        return controller;
    }

    /**
     * Registers a new cable and persists it.
     *
     * @param id             the unique id of the cable
     * @param title          the display title of the cable
     * @param price          the sale price
     * @param stock          the initial stock available
     * @param lengthInMeters the length of the cable in meters
     * @param connectorType  the connector type (e.g. "HDMI", "USB", "Optical")
     * @return the registered cable
     * @throws IllegalArgumentException if the id is empty or the price is negative
     */
    public Cable registerCable(String id, String title, double price, int stock,
                                double lengthInMeters, String connectorType) {
        Cable cable = new Cable(id, title, price, stock, lengthInMeters, connectorType);
        validateAccessory(cable);
        accessoryDAO.save(cable);
        return cable;
    }

    /**
     * Registers a new memory and persists it.
     *
     * @param id                  the unique id of the memory
     * @param title               the display title of the memory
     * @param price               the sale price
     * @param stock               the initial stock available
     * @param capacityInGigabytes the storage capacity in gigabytes
     * @param memoryType          the memory type (e.g. "SD", "microSD", "Internal card")
     * @return the registered memory
     * @throws IllegalArgumentException if the id is empty or the price is negative
     */
    public Memory registerMemory(String id, String title, double price, int stock,
                                  int capacityInGigabytes, String memoryType) {
        Memory memory = new Memory(id, title, price, stock, capacityInGigabytes, memoryType);
        validateAccessory(memory);
        accessoryDAO.save(memory);
        return memory;
    }

    /**
     * Registers a console as compatible with the given accessory and
     * persists the change.
     *
     * @param accessoryId the id of the accessory
     * @param consoleId   the id of the console to register as compatible
     * @throws IllegalArgumentException if the accessory does not exist
     */
    public void registerCompatibility(String accessoryId, String consoleId) {
        Accessory accessory = findById(accessoryId);
        if (accessory == null) {
            throw new IllegalArgumentException("Accessory not found: " + accessoryId);
        }
        accessory.addCompatibleConsole(consoleId);
        accessoryDAO.update(accessory);
    }

    /**
     * Returns the list of all currently available accessories.
     *
     * @return the list of available accessories
     */
    public List<Accessory> listAllAccessories() {
        return accessoryDAO.findAll();
    }

    /**
     * Returns the accessories of a specific type.
     *
     * @param type the accessory type: "CONTROLLER", "CABLE" or "MEMORY"
     * @return the list of accessories matching that type
     */
    public List<Accessory> listAccessoriesByType(String type) {
        List<Accessory> result = new ArrayList<>();
        for (Accessory a : accessoryDAO.findAll()) {
            if (matchesType(a, type)) {
                result.add(a);
            }
        }
        return result;
    }

    /**
     * Returns the accessories compatible with a specific console.
     *
     * @param consoleId the id of the console to check compatibility against
     * @return the list of accessories compatible with that console
     */
    public List<Accessory> findAccessoriesCompatibleWith(String consoleId) {
        List<Accessory> result = new ArrayList<>();
        for (Accessory a : accessoryDAO.findAll()) {
            if (a.isCompatibleWith(consoleId)) {
                result.add(a);
            }
        }
        return result;
    }

    /**
     * Finds an accessory by its id.
     *
     * @param id the id of the accessory to find
     * @return the matching accessory, or null if none is found
     */
    public Accessory findById(String id) {
        return accessoryDAO.findById(id);
    }

    private void validateAccessory(Accessory accessory) {
        if (accessory.getId() == null || accessory.getId().isEmpty()) {
            throw new IllegalArgumentException("Accessory must have a valid id");
        }
        if (accessory.getPrice() < 0) {
            throw new IllegalArgumentException("Accessory price cannot be negative");
        }
    }

    private boolean matchesType(Accessory accessory, String type) {
        if (type == null) return false;
        return switch (type.toUpperCase()) {
            case "CONTROLLER" -> accessory instanceof Controller;
            case "CABLE" -> accessory instanceof Cable;
            case "MEMORY" -> accessory instanceof Memory;
            default -> false;
        };
    }
}