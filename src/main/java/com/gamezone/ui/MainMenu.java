package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.GameZoneException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Accessory;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.Warranty;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Console-based user interface for the GameZone Unicesar system.
 * Displays the main menu and delegates every operation to the
 * corresponding service class; never accesses persistence directly.
 * All text shown to the end user is written in Spanish, while the
 * code itself follows English naming conventions.
 */
public class MainMenu {

    private final PersonService personService;
    private final ProductService productService;
    private final AccessoryService accessoryService;
    private final PromotionService promotionService;
    private final WarrantyService warrantyService;
    private final ReturnService returnService;
    private final SaleService saleService;
    private final ProductMenu productMenuView;
    private final PersonMenu personMenuView;
    private final SaleMenu saleMenuView;
    private final AccessoryMenu accessoryMenuView;
    /**
     * Creates the main menu with the services it depends on.
     *
     * @param personService  the service for managing customers and sellers
     * @param productService the service for managing products
     * @param saleService    the service for registering and querying sales
     * @param accessoryService the service for managing accessories
     * @param promotionService the service for managing promotions
     * @param returnService the service for managing returns
     * @param warrantyService the service for managing warranties
     */
    public MainMenu(PersonService personService, ProductService productService,
                 SaleService saleService, AccessoryService accessoryService,
                 PromotionService promotionService, ReturnService returnService,
                 WarrantyService warrantyService) {
    this.personService = personService;
    this.productService = productService;
    this.saleService = saleService;
    this.accessoryService = accessoryService;
    this.promotionService = promotionService;
    this.returnService = returnService;
    this.warrantyService = warrantyService;
    this.productMenuView = new ProductMenu(productService);
    this.personMenuView = new PersonMenu(personService);
    this.saleMenuView = new SaleMenu(saleService, productService);
    this.accessoryMenuView = new AccessoryMenu(accessoryService);
}

    /**
     * Starts the main application loop, showing the menu until
     * the user chooses to exit.
     */
        public void start() {
        boolean running = true;
        while (running) {
                    String[] options = {
                    "Gestionar productos",
                    "Gestionar clientes y vendedores",
                    "Gestionar ventas",
                    "Gestionar accesorios",
                    "Gestionar promociones",
                    "Gestionar devoluciones",
                    "Gestionar garantías",
                    "Salir"
            };
            Object selectedOption = JOptionPane.showInputDialog(
                    null,
                    "Seleccione una opción:",
                    "GameZone Unicesar",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            int choice = (selectedOption == null)
                    ? -1
                    : java.util.Arrays.asList(options).indexOf(selectedOption);

            try {
                switch (choice) {
                    case 0 -> productMenuView.show();
                    case 1 -> personMenuView.show();
                    case 2 -> saleMenuView.show();
                    case 3 -> accessoryMenuView.show();
                    case 4 -> promotionMenu();
                    case 5 -> returnMenu();
                    case 6 -> warrantyMenu();
                    case 7, -1 -> running = false;
                }
            } catch (ResourceNotFoundException e) {
                MenuDialogs.showResourceNotFound(e);
            } catch (BusinessRuleException e) {
                MenuDialogs.showBusinessRuleViolation(e);
            } catch (InvalidDataException e) {
                MenuDialogs.showInvalidData(e);
            } catch (PersistenceException e) {
                MenuDialogs.showPersistenceError();
            } catch (GameZoneException e) {
                MenuDialogs.showGenericError(e);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                        "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        JOptionPane.showMessageDialog(null, "Cerrando GameZone Unicesar. ¡Hasta pronto!",
                "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
    }

// ===================== PROMOTION MENU =====================

    private void promotionMenu() {
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
        List<String> entries = new java.util.ArrayList<>();
        for (Promotion promotion : promotions) {
            entries.add(promotion.getId() + " - " + promotion.getName()
                    + " (" + promotion.getStartDate() + " a " + promotion.getEndDate() + ")");
        }
        MenuDialogs.showList("Promociones", MenuDialogs.formatEntries(entries));
    }
    
// ===================== RETURN MENU =====================

    private void returnMenu() {
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
            List<String> productIds = new java.util.ArrayList<>();
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
            MenuDialogs.showList("Devolución registrada exitosamente", processedReturn.generateReturnReceipt());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.io.IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void viewAllReturns() {
        try {
            printReturns(returnService.viewAllReturns());
        } catch (java.io.IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void viewReturnsByCustomer() {
        String customerId = MenuDialogs.askInput("Id del cliente:");
        if (customerId == null) return;
        try {
            printReturns(returnService.viewReturnsByCustomer(customerId));
        } catch (java.io.IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void viewReturnsBySale() {
        String saleId = MenuDialogs.askInput("Id de la venta:");
        if (saleId == null) return;
        try {
            printReturns(returnService.viewReturnsBySale(saleId));
        } catch (java.io.IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void printReturns(List<Return> returns) {
        if (returns.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron devoluciones.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<String> entries = new java.util.ArrayList<>();
        for (Return r : returns) {
            entries.add(r.generateReturnReceipt());
        }
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

            double totalSales = returnService.calculateMonthlySales(month, year);
            double totalReturns = returnService.calculateMonthlyReturns(month, year);
            double balance = returnService.generateMonthlyBalance(month, year);

            String summary = "Total de ventas para " + month + "/" + year + ": " + totalSales + "\n"
                    + "Total de devoluciones para " + month + "/" + year + ": " + totalReturns + "\n"
                    + "Balance neto para " + month + "/" + year + ": " + balance;
            JOptionPane.showMessageDialog(null, summary, "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: el mes y el año deben ser valores numéricos válidos.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.io.IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

// ===================== WARRANTY MENU =====================

    private void warrantyMenu() {
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
                MenuDialogs.showList("Garantía encontrada", warranty.generateWarrantyCertificate());
            }
        } catch (java.io.IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void listAllWarranties() {
        try {
            printWarranties(warrantyService.listAllWarranties());
        } catch (java.io.IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void listActiveWarranties() {
        try {
            printWarranties(warrantyService.listActiveWarranties());
        } catch (java.io.IOException e) {
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
        } catch (java.io.IOException e) {
            MenuDialogs.showPersistenceError();
        }
    }

    private void printWarranties(List<Warranty> warranties) {
        if (warranties.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron garantías.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<String> entries = new java.util.ArrayList<>();
        for (Warranty warranty : warranties) {
            entries.add(warranty.generateWarrantyCertificate());
        }
        MenuDialogs.showList("Garantías", MenuDialogs.formatEntries(entries));
    }
}
