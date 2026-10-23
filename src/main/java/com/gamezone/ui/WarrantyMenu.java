package com.gamezone.ui;

import com.gamezone.model.Warranty;
import com.gamezone.service.WarrantyService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Handles the "Gestionar garantías" menu: consulting a warranty by
 * product and sale, and listing warranties overall, active, or
 * expiring soon. Extracted out of MainMenu so this area's UI logic is
 * cohesive on its own (GRASP audit finding V-06: High Cohesion).
 */
public class WarrantyMenu implements AreaMenu {

    private final WarrantyService warrantyService;

    /**
     * Creates the warranty menu with the service it depends on.
     *
     * @param warrantyService the service used to query warranties
     */
    public WarrantyMenu(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;
    }

    @Override
    public String getTitle() {
        return "Gestionar garantías";
    }
    
    /**
     * Shows the warranty menu and dispatches the chosen option.
     */
    @Override
    public void show() {
        String[] options = {
                "Consultar garantía por producto y venta",
                "Listar todas las garantías",
                "Listar garantías vigentes",
                "Listar garantías próximas a vencer",
                "Volver"
        };
        Object selectedOption = JOptionPane.showInputDialog(
                null,
                "Gestión de garantías - Seleccione una opción:",
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
            case 0 -> consultWarrantyByProduct();
            case 1 -> listAllWarranties();
            case 2 -> listActiveWarranties();
            case 3 -> listWarrantiesExpiringSoon();
            default -> { }
        }
    }

    private void consultWarrantyByProduct() {
        String productId = MenuDialogs.askInput("Id del producto:");
        if (productId == null) return;
        String saleId = MenuDialogs.askInput("Id de la venta:");
        if (saleId == null) return;

        try {
            Warranty warranty = warrantyService.findWarrantyByProduct(productId, saleId);
            if (warranty == null) {
                JOptionPane.showMessageDialog(null, "No se encontró una garantía para ese producto en esa venta.",
                        "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            } else {
MenuDialogs.showList("Garantía encontrada", WarrantyCertificateFormatter.format(warranty));            }
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void listAllWarranties() {
        try {
            printWarranties(warrantyService.listAllWarranties());
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void listActiveWarranties() {
        try {
            printWarranties(warrantyService.listActiveWarranties());
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void listWarrantiesExpiringSoon() {
        String daysAheadText = MenuDialogs.askInput("¿Con cuántos días de anticipación desea consultar?");
        if (daysAheadText == null) return;

        try {
            int daysAhead = Integer.parseInt(daysAheadText);
            printWarranties(warrantyService.listWarrantiesExpiringSoon(daysAhead));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: los días deben ser un valor numérico válido.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void printWarranties(List<Warranty> warranties) {
        if (warranties.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron garantías.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<String> entries = new ArrayList<>();
        for (Warranty warranty : warranties) {
entries.add(WarrantyCertificateFormatter.format(warranty));        }
        MenuDialogs.showList("Garantías", MenuDialogs.formatEntries(entries));
    }
}
