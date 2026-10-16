package com.gamezone.ui;

import com.gamezone.model.Promotion;
import com.gamezone.service.PromotionService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Handles the "Gestionar promociones" menu: registering percentage,
 * category and bulk-purchase discounts, and listing promotions.
 * Extracted out of MainMenu so this area's UI logic is cohesive on its
 * own (GRASP audit finding V-06: High Cohesion).
 */
public class PromotionMenu {

    private final PromotionService promotionService;

    /**
     * Creates the promotion menu with the service it depends on.
     *
     * @param promotionService the service used to register and list promotions
     */
    public PromotionMenu(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    /**
     * Shows the promotion menu and dispatches the chosen option.
     */
    public void show() {
        String[] options = {
                "Registrar promoción por porcentaje",
                "Registrar promoción por categoría",
                "Registrar promoción por volumen de compra",
                "Listar todas las promociones",
                "Listar promociones vigentes",
                "Volver"
        };
        Object selectedOption = JOptionPane.showInputDialog(
                null,
                "Gestión de promociones - Seleccione una opción:",
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
            case 0 -> registerPercentageDiscount();
            case 1 -> registerCategoryDiscount();
            case 2 -> registerBulkPurchaseDiscount();
            case 3 -> listAllPromotions();
            case 4 -> listActivePromotions();
            default -> { }
        }
    }

    private void registerPercentageDiscount() {
        String id = MenuDialogs.askInput("Id:");
        if (id == null) return;
        String name = MenuDialogs.askInput("Nombre:");
        if (name == null) return;
        String startDateText = MenuDialogs.askInput("Fecha de inicio (AAAA-MM-DD):");
        if (startDateText == null) return;
        String endDateText = MenuDialogs.askInput("Fecha de fin (AAAA-MM-DD):");
        if (endDateText == null) return;
        String percentageText = MenuDialogs.askInput("Porcentaje de descuento (0-100):");
        if (percentageText == null) return;

        try {
            LocalDate startDate = LocalDate.parse(startDateText);
            LocalDate endDate = LocalDate.parse(endDateText);
            double percentage = Double.parseDouble(percentageText);

            promotionService.registerPercentageDiscount(id, name, startDate, endDate, percentage);
            JOptionPane.showMessageDialog(null, "Promoción por porcentaje registrada exitosamente.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: el porcentaje debe ser un valor numérico válido.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(null, "Error: la fecha debe tener el formato AAAA-MM-DD.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerCategoryDiscount() {
        String id = MenuDialogs.askInput("Id:");
        if (id == null) return;
        String name = MenuDialogs.askInput("Nombre:");
        if (name == null) return;
        String startDateText = MenuDialogs.askInput("Fecha de inicio (AAAA-MM-DD):");
        if (startDateText == null) return;
        String endDateText = MenuDialogs.askInput("Fecha de fin (AAAA-MM-DD):");
        if (endDateText == null) return;
        String percentageText = MenuDialogs.askInput("Porcentaje de descuento (0-100):");
        if (percentageText == null) return;
        String targetCategory = MenuDialogs.askInput("Categoría objetivo (VIDEOGAME/CONSOLE/ACCESSORY):");
        if (targetCategory == null) return;

        try {
            LocalDate startDate = LocalDate.parse(startDateText);
            LocalDate endDate = LocalDate.parse(endDateText);
            double percentage = Double.parseDouble(percentageText);

            promotionService.registerCategoryDiscount(id, name, startDate, endDate, percentage, targetCategory);
            JOptionPane.showMessageDialog(null, "Promoción por categoría registrada exitosamente.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: el porcentaje debe ser un valor numérico válido.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(null, "Error: la fecha debe tener el formato AAAA-MM-DD.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerBulkPurchaseDiscount() {
        String id = MenuDialogs.askInput("Id:");
        if (id == null) return;
        String name = MenuDialogs.askInput("Nombre:");
        if (name == null) return;
        String startDateText = MenuDialogs.askInput("Fecha de inicio (AAAA-MM-DD):");
        if (startDateText == null) return;
        String endDateText = MenuDialogs.askInput("Fecha de fin (AAAA-MM-DD):");
        if (endDateText == null) return;
        String minQuantityText = MenuDialogs.askInput("Cantidad mínima de productos:");
        if (minQuantityText == null) return;
        String percentageText = MenuDialogs.askInput("Porcentaje de descuento (0-100):");
        if (percentageText == null) return;

        try {
            LocalDate startDate = LocalDate.parse(startDateText);
            LocalDate endDate = LocalDate.parse(endDateText);
            int minQuantity = Integer.parseInt(minQuantityText);
            double percentage = Double.parseDouble(percentageText);

            promotionService.registerBulkPurchaseDiscount(id, name, startDate, endDate, minQuantity, percentage);
            JOptionPane.showMessageDialog(null, "Promoción por volumen de compra registrada exitosamente.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Error: la cantidad mínima y el porcentaje deben ser valores numéricos válidos.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(null, "Error: la fecha debe tener el formato AAAA-MM-DD.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void listAllPromotions() {
        List<Promotion> promotions = promotionService.listAllPromotions();
        printPromotions(promotions);
    }

    private void listActivePromotions() {
        List<Promotion> promotions = promotionService.listActivePromotions();
        printPromotions(promotions);
    }

    private void printPromotions(List<Promotion> promotions) {
        if (promotions.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron promociones.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<String> entries = new ArrayList<>();
        for (Promotion promotion : promotions) {
            entries.add(promotion.getId() + " - " + promotion.getName()
                    + " (" + promotion.getStartDate() + " a " + promotion.getEndDate() + ")");
        }
        MenuDialogs.showList("Promociones", MenuDialogs.formatEntries(entries));
    }
}
