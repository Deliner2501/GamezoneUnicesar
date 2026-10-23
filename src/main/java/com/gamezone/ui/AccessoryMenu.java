package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.service.AccessoryService;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Handles the "Gestionar accesorios" menu: registering controllers,
 * cables and memories, and listing accessories. Extracted out of
 * MainMenu so this area's UI logic is cohesive on its own (GRASP audit
 * finding V-06: High Cohesion).
 */
public class AccessoryMenu implements AreaMenu {

    private final AccessoryService accessoryService;

    /**
     * Creates the accessory menu with the service it depends on.
     *
     * @param accessoryService the service used to register and list accessories
     */
    public AccessoryMenu(AccessoryService accessoryService) {
        this.accessoryService = accessoryService;
    }

    @Override
    public String getTitle() {
        return "Gestionar accesorios";
    }
    
    /**
     * Shows the accessory menu and dispatches the chosen option.
     */
    @Override
    public void show() {
        String[] options = {
                "Registrar un control",
                "Registrar un cable",
                "Registrar una memoria",
                "Listar todos los accesorios",
                "Listar accesorios por tipo",
                "Consultar accesorios compatibles con una consola",
                "Volver"
        };
        Object selectedOption = JOptionPane.showInputDialog(
                null,
                "Gestión de accesorios - Seleccione una opción:",
                "GameZone Unicesar",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        int choice = (selectedOption == null)
                ? -1
                : java.util.Arrays.asList(options).indexOf(selectedOption);

        switch (choice) {
            case 0 -> registerController();
            case 1 -> registerCable();
            case 2 -> registerMemory();
            case 3 -> listAllAccessories();
            case 4 -> listAccessoriesByType();
            case 5 -> listAccessoriesCompatibleWithConsole();
            default -> { }
        }
    }

    private void registerController() {
        String id = MenuDialogs.askInput("Id:");
        if (id == null) return;
        String title = MenuDialogs.askInput("Título:");
        if (title == null) return;
        String priceText = MenuDialogs.askInput("Precio:");
        if (priceText == null) return;
        String stockText = MenuDialogs.askInput("Cantidad en inventario:");
        if (stockText == null) return;
        String connectionType = MenuDialogs.askInput("Tipo de conexión (inalámbrico/alámbrico):");
        if (connectionType == null) return;

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);

            accessoryService.registerController(id, title, price, stock, connectionType);
            registerCompatibleConsoles(id);
            JOptionPane.showMessageDialog(null, "Control registrado exitosamente.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: precio y cantidad deben ser valores numéricos válidos.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerCable() {
        String id = MenuDialogs.askInput("Id:");
        if (id == null) return;
        String title = MenuDialogs.askInput("Título:");
        if (title == null) return;
        String priceText = MenuDialogs.askInput("Precio:");
        if (priceText == null) return;
        String stockText = MenuDialogs.askInput("Cantidad en inventario:");
        if (stockText == null) return;
        String lengthText = MenuDialogs.askInput("Longitud en metros:");
        if (lengthText == null) return;
        String connectorType = MenuDialogs.askInput("Tipo de conector (HDMI/USB/óptico/otro):");
        if (connectorType == null) return;

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);
            double lengthInMeters = Double.parseDouble(lengthText);

            accessoryService.registerCable(id, title, price, stock, lengthInMeters, connectorType);
            JOptionPane.showMessageDialog(null, "Cable registrado exitosamente.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Error: precio, cantidad y longitud deben ser valores numéricos válidos.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerMemory() {
        String id = MenuDialogs.askInput("Id:");
        if (id == null) return;
        String title = MenuDialogs.askInput("Título:");
        if (title == null) return;
        String priceText = MenuDialogs.askInput("Precio:");
        if (priceText == null) return;
        String stockText = MenuDialogs.askInput("Cantidad en inventario:");
        if (stockText == null) return;
        String capacityText = MenuDialogs.askInput("Capacidad en gigabytes:");
        if (capacityText == null) return;
        String memoryType = MenuDialogs.askInput("Tipo de memoria (SD/microSD/tarjeta interna):");
        if (memoryType == null) return;

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);
            int capacityInGigabytes = Integer.parseInt(capacityText);

            accessoryService.registerMemory(id, title, price, stock, capacityInGigabytes, memoryType);
            registerCompatibleConsoles(id);
            JOptionPane.showMessageDialog(null, "Memoria registrada exitosamente.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Error: precio, cantidad y capacidad deben ser valores numéricos válidos.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerCompatibleConsoles(String accessoryId) {
        boolean addingConsoles = true;
        while (addingConsoles) {
            String consoleId = MenuDialogs.askInput("Id de consola compatible (deje vacío para terminar):");
            if (consoleId == null || consoleId.isBlank()) {
                addingConsoles = false;
            } else {
                accessoryService.registerCompatibility(accessoryId, consoleId);
            }
        }
    }

    private void listAllAccessories() {
        List<Accessory> accessories = accessoryService.listAllAccessories();
        printAccessories(accessories);
    }

    private void listAccessoriesByType() {
        String type = MenuDialogs.askInput("Tipo de accesorio (CONTROLLER/CABLE/MEMORY):");
        if (type == null) return;
        List<Accessory> accessories = accessoryService.listAccessoriesByType(type);
        printAccessories(accessories);
    }

    private void listAccessoriesCompatibleWithConsole() {
        String consoleId = MenuDialogs.askInput("Id de la consola:");
        if (consoleId == null) return;
        List<Accessory> accessories = accessoryService.findAccessoriesCompatibleWith(consoleId);
        printAccessories(accessories);
    }

    private void printAccessories(List<Accessory> accessories) {
        if (accessories.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron accesorios.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<String> entries = new ArrayList<>();
        for (Accessory accessory : accessories) {
            entries.add(accessory.getFullDescription());
        }
        MenuDialogs.showList("Accesorios", MenuDialogs.formatEntries(entries));
    }
}