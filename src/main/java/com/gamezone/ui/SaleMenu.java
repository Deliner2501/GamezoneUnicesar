package com.gamezone.ui;

import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Handles the "Gestionar ventas" menu: registering sales and listing
 * them overall, by customer, or by seller. Extracted out of MainMenu
 * so this area's UI logic is cohesive on its own (GRASP audit finding
 * V-06: High Cohesion).
 */
public class SaleMenu {

    private final SaleService saleService;
    private final ProductService productService;

    /**
     * Creates the sale menu with the services it depends on.
     *
     * @param saleService    the service used to register and query sales
     * @param productService the service used to look up a product's warranty eligibility
     */
    public SaleMenu(SaleService saleService, ProductService productService) {
        this.saleService = saleService;
        this.productService = productService;
    }

    /**
     * Shows the sale menu and dispatches the chosen option.
     */
    public void show() {
        String[] options = {
                "Registrar una venta (productos y/o accesorios)",
                "Listar todas las ventas",
                "Consultar historial de compras de un cliente",
                "Consultar ventas atendidas por un vendedor",
                "Volver"
        };
        Object selectedOption = JOptionPane.showInputDialog(
                null,
                "Gestión de ventas - Seleccione una opción:",
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
            case 0 -> registerSale();
            case 1 -> listSales();
            case 2 -> listSalesByCustomer();
            case 3 -> listSalesBySeller();
            default -> { }
        }
    }

    private void registerSale() {
        String saleId = MenuDialogs.askInput("Id de la venta:");
        if (saleId == null) return;
        String customerId = MenuDialogs.askInput("Id del cliente:");
        if (customerId == null) return;
        String sellerId = MenuDialogs.askInput("Id del vendedor:");
        if (sellerId == null) return;

        try {
            Map<String, Integer> productQuantities = new LinkedHashMap<>();
            List<String> productIdsWithExtendedWarranty = new ArrayList<>();
            boolean addingProducts = true;
            JOptionPane.showMessageDialog(null,
                    "Puede agregar productos (videojuegos, consolas) y accesorios (controles, cables, memorias) en la misma venta.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            while (addingProducts) {
                String productId = MenuDialogs.askInput("Id del producto o accesorio (deje vacío para terminar):");
                if (productId == null || productId.isBlank()) {
                    addingProducts = false;
                    continue;
                }
                String quantityText = MenuDialogs.askInput("Cantidad:");
                if (quantityText == null) {
                    addingProducts = false;
                    continue;
                }
                int quantity = Integer.parseInt(quantityText);
                productQuantities.merge(productId, quantity, Integer::sum);

                Product product = productService.findProductById(productId);
                if (product != null && product.requiresWarranty()) {
                    int answer = JOptionPane.showConfirmDialog(
                            null,
                            "¿Agregar garantía extendida a este producto?",
                            "GameZone Unicesar",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (answer == JOptionPane.YES_OPTION) {
                        productIdsWithExtendedWarranty.add(productId);
                    }
                }
            }

            Sale sale = saleService.registerSale(saleId, LocalDate.now(), customerId, sellerId,
                    productQuantities, productIdsWithExtendedWarranty);
            MenuDialogs.showList("Venta registrada exitosamente", sale.generateReceipt());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: la cantidad debe ser un valor numérico válido.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void listSales() {
        try {
            List<Sale> sales = saleService.listSales();
            printSales(sales);
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void listSalesByCustomer() {
        String customerId = MenuDialogs.askInput("Id del cliente:");
        if (customerId == null) return;
        try {
            printSales(saleService.listSalesByCustomer(customerId));
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void listSalesBySeller() {
        String sellerId = MenuDialogs.askInput("Id del vendedor:");
        if (sellerId == null) return;
        try {
            printSales(saleService.listSalesBySeller(sellerId));
        } catch (IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void printSales(List<Sale> sales) {
        if (sales.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron ventas.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<String> entries = new ArrayList<>();
        for (Sale sale : sales) {
            entries.add(sale.generateReceipt());
        }
        MenuDialogs.showList("Ventas", MenuDialogs.formatEntries(entries));
    }
}
