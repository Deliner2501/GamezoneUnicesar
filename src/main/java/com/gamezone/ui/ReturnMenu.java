package com.gamezone.ui;

import com.gamezone.model.Return;
import com.gamezone.service.ReturnService;
import com.gamezone.service.MonthlyBalanceService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Handles the "Gestionar devoluciones" menu: registering returns,
 * viewing them overall, by customer or by sale, and consulting the
 * monthly balance. Extracted out of MainMenu so this area's UI logic
 * is cohesive on its own (GRASP audit finding V-06: High Cohesion).
 */
public class ReturnMenu {

    private final ReturnService returnService;
    private final MonthlyBalanceService monthlyBalanceService;

    /**
     * Creates the return menu with the service it depends on.
     *
     * @param returnService the service used to register and query returns
     * @param monthlyBalanceService the service used to compute the monthly balance
     */
    public ReturnMenu(ReturnService returnService, MonthlyBalanceService monthlyBalanceService) {
        this.returnService = returnService;
        this.monthlyBalanceService = monthlyBalanceService;
    }

    /**
     * Shows the return menu and dispatches the chosen option.
     */
    public void show() {
        String[] options = {
                "Registrar una devolución",
                "Consultar todas las devoluciones",
                "Consultar devoluciones por cliente",
                "Consultar devoluciones por venta",
                "Consultar balance mensual",
                "Volver"
        };
        Object selectedOption = JOptionPane.showInputDialog(
                null,
                "Gestión de devoluciones - Seleccione una opción:",
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
            case 0 -> registerReturn();
            case 1 -> viewAllReturns();
            case 2 -> viewReturnsByCustomer();
            case 3 -> viewReturnsBySale();
            case 4 -> consultMonthlyBalance();
            default -> { }
        }
    }

    private void registerReturn() {
        String saleId = MenuDialogs.askInput("Id de la venta original:");
        if (saleId == null) return;

        try {
            List<String> productIds = new ArrayList<>();
            boolean addingProducts = true;
            while (addingProducts) {
                String productId = MenuDialogs.askInput("Id de producto a devolver (deje vacío para terminar):");
                if (productId == null || productId.isBlank()) {
                    addingProducts = false;
                } else {
                    productIds.add(productId);
                }
            }

            String reason = MenuDialogs.askInput("Motivo de la devolución:");
            if (reason == null) return;

            Return processedReturn = returnService.registerReturn(saleId, productIds, reason);
MenuDialogs.showList("Devolución registrada exitosamente", ReturnReceiptFormatter.format(processedReturn));        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void viewAllReturns() {
        try {
            printReturns(returnService.viewAllReturns());
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void viewReturnsByCustomer() {
        String customerId = MenuDialogs.askInput("Id del cliente:");
        if (customerId == null) return;
        try {
            printReturns(returnService.viewReturnsByCustomer(customerId));
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void viewReturnsBySale() {
        String saleId = MenuDialogs.askInput("Id de la venta:");
        if (saleId == null) return;
        try {
            printReturns(returnService.viewReturnsBySale(saleId));
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void printReturns(List<Return> returns) {
        if (returns.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron devoluciones.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<String> entries = new ArrayList<>();
        for (Return r : returns) {
entries.add(ReturnReceiptFormatter.format(r));        }
        MenuDialogs.showList("Devoluciones", MenuDialogs.formatEntries(entries));
    }

    private void consultMonthlyBalance() {
        String monthText = MenuDialogs.askInput("Mes (1-12):");
        if (monthText == null) return;
        String yearText = MenuDialogs.askInput("Año (AAAA):");
        if (yearText == null) return;

        try {
            int month = Integer.parseInt(monthText);
            int year = Integer.parseInt(yearText);

            double totalSales = monthlyBalanceService.calculateMonthlySales(month, year);
            double totalReturns = monthlyBalanceService.calculateMonthlyReturns(month, year);
            double balance = monthlyBalanceService.generateMonthlyBalance(month, year);

            String summary = "Total de ventas para " + month + "/" + year + ": " + totalSales + "\n"
                    + "Total de devoluciones para " + month + "/" + year + ": " + totalReturns + "\n"
                    + "Balance neto para " + month + "/" + year + ": " + balance;
            JOptionPane.showMessageDialog(null, summary, "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: el mes y el año deben ser valores numéricos válidos.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }
}
