package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.GameZoneException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Accessory;
import com.gamezone.model.Console;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.VideoGame;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
                    case 0 -> productMenu();
                    case 1 -> personMenu();
                    case 2 -> saleMenu();
                    case 3 -> accessoryMenu();
                    case 4 -> promotionMenu();
                    case 5 -> returnMenu();
                    case 6 -> warrantyMenu();
                    case 7, -1 -> running = false;
                }
            } catch (ResourceNotFoundException e) {
                showResourceNotFound(e);
            } catch (BusinessRuleException e) {
                showBusinessRuleViolation(e);
            } catch (InvalidDataException e) {
                showInvalidData(e);
            } catch (PersistenceException e) {
                showPersistenceError();
            } catch (GameZoneException e) {
                showGenericError(e);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                        "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        JOptionPane.showMessageDialog(null, "Cerrando GameZone Unicesar. ¡Hasta pronto!",
                "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
    }

        // ===================== ERROR HANDLING =====================

    /**
     * Shows the message for a resource that does not exist in the system
     * (product, customer, seller or sale).
     *
     * @param e the exception describing the missing resource
     */
    private void showResourceNotFound(ResourceNotFoundException e) {
        JOptionPane.showMessageDialog(null, "No se encontró el recurso solicitado: " + e.getMessage(),
                "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows the message for an operation that violates a business rule
     * (insufficient stock, duplicated customer, sale without products).
     *
     * @param e the exception describing the violated rule
     */
    private void showBusinessRuleViolation(BusinessRuleException e) {
        JOptionPane.showMessageDialog(null, "No se puede completar la operación: " + e.getMessage(),
                "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows the message for input data that fails format or range checks.
     *
     * @param e the exception describing the invalid field
     */
    private void showInvalidData(InvalidDataException e) {
        JOptionPane.showMessageDialog(null, "Los datos ingresados no son válidos: " + e.getMessage(),
                "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a generic message for storage problems. It never exposes file
     * names or technical details to the user.
     */
    private void showPersistenceError() {
        JOptionPane.showMessageDialog(null,
                "Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.",
                "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a fallback message for any GameZoneException subtype that has
     * no dedicated handler, so new exception types never reach the user raw.
     *
     * @param e the unclassified GameZone exception
     */
    private void showGenericError(GameZoneException e) {
        JOptionPane.showMessageDialog(null, "No se puede completar la operación: " + e.getMessage(),
                "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
    }
    
        // ===================== UI HELPERS =====================

    /**
     * Shows a single-line input dialog and returns what the user typed.
     *
     * @param label the label describing the requested value
     * @return the text entered, or null if the dialog was cancelled/closed
     */
    private String askInput(String label) {
        return JOptionPane.showInputDialog(null, label, "GameZone Unicesar", JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Shows a block of text inside a scrollable, read-only text area,
     * used to display listings that may be too long for a plain dialog.
     *
     * @param title   the dialog title
     * @param content the text content to display
     */
    private void showList(String title, String content) {
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(520, 400));

        JOptionPane.showMessageDialog(null, scrollPane, title, JOptionPane.PLAIN_MESSAGE);
    }
    
    // ===================== PRODUCT MENU =====================

        private void productMenu() {
        String[] options = {
                "Registrar un videojuego",
                "Registrar una consola",
                "Listar productos disponibles",
                "Volver"
        };
        Object selectedOption = JOptionPane.showInputDialog(
                null,
                "Gestión de productos - Seleccione una opción:",
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
            case 0 -> registerVideoGame();
            case 1 -> registerConsole();
            case 2 -> listProducts();
            default -> { }
        }
    }

    private void registerVideoGame() {
        String id = askInput("Id:");
        if (id == null) return;
        String title = askInput("Título:");
        if (title == null) return;
        String priceText = askInput("Precio:");
        if (priceText == null) return;
        String stockText = askInput("Cantidad en inventario:");
        if (stockText == null) return;
        String platform = askInput("Plataforma:");
        if (platform == null) return;
        String genre = askInput("Género:");
        if (genre == null) return;
        String ageRating = askInput("Clasificación de edad:");
        if (ageRating == null) return;

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);

            Product videoGame = new VideoGame(id, title, price, stock, platform, genre, ageRating);
            productService.registerProduct(videoGame);
            JOptionPane.showMessageDialog(null, "Videojuego registrado exitosamente.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Error: el precio y la cantidad deben ser valores numéricos válidos.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerConsole() {
        String id = askInput("Id:");
        if (id == null) return;
        String title = askInput("Título:");
        if (title == null) return;
        String priceText = askInput("Precio:");
        if (priceText == null) return;
        String stockText = askInput("Cantidad en inventario:");
        if (stockText == null) return;
        String brand = askInput("Marca:");
        if (brand == null) return;
        String model = askInput("Modelo:");
        if (model == null) return;
        String generation = askInput("Generación:");
        if (generation == null) return;

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);

            Product console = new Console(id, title, price, stock, brand, model, generation);
            productService.registerProduct(console);
            JOptionPane.showMessageDialog(null, "Consola registrada exitosamente.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Error: el precio y la cantidad deben ser valores numéricos válidos.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void listProducts() {
        List<Product> products = productService.listAvailableProducts();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Aún no hay productos registrados.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder content = new StringBuilder();
        for (Product product : products) {
            content.append(product.getFullDescription()).append("\n");
        }
        showList("Productos disponibles", content.toString());
    }

    // ===================== PERSON MENU =====================

        private void personMenu() {
        String[] options = {
                "Registrar un cliente",
                "Listar clientes",
                "Listar vendedores",
                "Volver"
        };
        Object selectedOption = JOptionPane.showInputDialog(
                null,
                "Gestión de clientes y vendedores - Seleccione una opción:",
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
            case 0 -> registerCustomer();
            case 1 -> listCustomers();
            case 2 -> listSellers();
            default -> { }
        }
    }

    private void registerCustomer() {
        String id = askInput("Id:");
        if (id == null) return;
        String name = askInput("Nombre:");
        if (name == null) return;
        String phone = askInput("Teléfono:");
        if (phone == null) return;
        String email = askInput("Correo electrónico:");
        if (email == null) return;

        try {
            personService.registerCustomer(name, id, phone, email);
            JOptionPane.showMessageDialog(null, "Cliente registrado exitosamente.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            showPersistenceError();
        }
    }

    private void listCustomers() {
        List<Customer> customers = personService.listCustomers();
        if (customers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Aún no hay clientes registrados.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder content = new StringBuilder();
        for (Customer customer : customers) {
            content.append(customer.toString()).append("\n");
        }
        showList("Clientes registrados", content.toString());
    }

    private void listSellers() {
        List<Seller> sellers = personService.listSellers();
        if (sellers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Aún no hay vendedores registrados.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder content = new StringBuilder();
        for (Seller seller : sellers) {
            content.append(seller.toString()).append("\n");
        }
        showList("Vendedores registrados", content.toString());
    }

    // ===================== SALE MENU =====================

        private void saleMenu() {
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
        String saleId = askInput("Id de la venta:");
        if (saleId == null) return;
        String customerId = askInput("Id del cliente:");
        if (customerId == null) return;
        String sellerId = askInput("Id del vendedor:");
        if (sellerId == null) return;

        try {
            Map<String, Integer> productQuantities = new LinkedHashMap<>();
            List<String> productIdsWithExtendedWarranty = new java.util.ArrayList<>();
            boolean addingProducts = true;
            JOptionPane.showMessageDialog(null,
                    "Puede agregar productos (videojuegos, consolas) y accesorios (controles, cables, memorias) en la misma venta.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            while (addingProducts) {
                String productId = askInput("Id del producto o accesorio (deje vacío para terminar):");
                if (productId == null || productId.isBlank()) {
                    addingProducts = false;
                    continue;
                }
                String quantityText = askInput("Cantidad:");
                if (quantityText == null) {
                    addingProducts = false;
                    continue;
                }
                int quantity = Integer.parseInt(quantityText);
                productQuantities.merge(productId, quantity, Integer::sum);

                Product product = productService.findProductById(productId);
                if (product instanceof Console) {
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
            showList("Venta registrada exitosamente", sale.generateReceipt());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: la cantidad debe ser un valor numérico válido.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            showPersistenceError();
        }
    }

    private void listSales() {
        try {
            List<Sale> sales = saleService.listSales();
            printSales(sales);
        } catch (IOException e) {
            showPersistenceError();
        }
    }

    private void listSalesByCustomer() {
        String customerId = askInput("Id del cliente:");
        if (customerId == null) return;
        try {
            printSales(saleService.listSalesByCustomer(customerId));
        } catch (IOException e) {
            showPersistenceError();
        }
    }

    private void listSalesBySeller() {
        String sellerId = askInput("Id del vendedor:");
        if (sellerId == null) return;
        try {
            printSales(saleService.listSalesBySeller(sellerId));
        } catch (IOException e) {
            showPersistenceError();
        }
    }

    private void printSales(List<Sale> sales) {
        if (sales.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron ventas.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder content = new StringBuilder();
        for (Sale sale : sales) {
            content.append(sale.generateReceipt()).append("\n---\n");
        }
        showList("Ventas", content.toString());
    }
    
        private void accessoryMenu() {
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
        String id = askInput("Id:");
        if (id == null) return;
        String title = askInput("Título:");
        if (title == null) return;
        String priceText = askInput("Precio:");
        if (priceText == null) return;
        String stockText = askInput("Cantidad en inventario:");
        if (stockText == null) return;
        String connectionType = askInput("Tipo de conexión (inalámbrico/alámbrico):");
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
        String id = askInput("Id:");
        if (id == null) return;
        String title = askInput("Título:");
        if (title == null) return;
        String priceText = askInput("Precio:");
        if (priceText == null) return;
        String stockText = askInput("Cantidad en inventario:");
        if (stockText == null) return;
        String lengthText = askInput("Longitud en metros:");
        if (lengthText == null) return;
        String connectorType = askInput("Tipo de conector (HDMI/USB/óptico/otro):");
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
        String id = askInput("Id:");
        if (id == null) return;
        String title = askInput("Título:");
        if (title == null) return;
        String priceText = askInput("Precio:");
        if (priceText == null) return;
        String stockText = askInput("Cantidad en inventario:");
        if (stockText == null) return;
        String capacityText = askInput("Capacidad en gigabytes:");
        if (capacityText == null) return;
        String memoryType = askInput("Tipo de memoria (SD/microSD/tarjeta interna):");
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
            String consoleId = askInput("Id de consola compatible (deje vacío para terminar):");
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
        String type = askInput("Tipo de accesorio (CONTROLLER/CABLE/MEMORY):");
        if (type == null) return;
        List<Accessory> accessories = accessoryService.listAccessoriesByType(type);
        printAccessories(accessories);
    }

    private void listAccessoriesCompatibleWithConsole() {
        String consoleId = askInput("Id de la consola:");
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
        StringBuilder content = new StringBuilder();
        for (Accessory accessory : accessories) {
            content.append(accessory.getFullDescription()).append("\n");
        }
        showList("Accesorios", content.toString());
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
        String id = askInput("Id:");
        if (id == null) return;
        String name = askInput("Nombre:");
        if (name == null) return;
        String startDateText = askInput("Fecha de inicio (AAAA-MM-DD):");
        if (startDateText == null) return;
        String endDateText = askInput("Fecha de fin (AAAA-MM-DD):");
        if (endDateText == null) return;
        String percentageText = askInput("Porcentaje de descuento (0-100):");
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
        String id = askInput("Id:");
        if (id == null) return;
        String name = askInput("Nombre:");
        if (name == null) return;
        String startDateText = askInput("Fecha de inicio (AAAA-MM-DD):");
        if (startDateText == null) return;
        String endDateText = askInput("Fecha de fin (AAAA-MM-DD):");
        if (endDateText == null) return;
        String percentageText = askInput("Porcentaje de descuento (0-100):");
        if (percentageText == null) return;
        String targetCategory = askInput("Categoría objetivo (VIDEOGAME/CONSOLE/ACCESSORY):");
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
        String id = askInput("Id:");
        if (id == null) return;
        String name = askInput("Nombre:");
        if (name == null) return;
        String startDateText = askInput("Fecha de inicio (AAAA-MM-DD):");
        if (startDateText == null) return;
        String endDateText = askInput("Fecha de fin (AAAA-MM-DD):");
        if (endDateText == null) return;
        String minQuantityText = askInput("Cantidad mínima de productos:");
        if (minQuantityText == null) return;
        String percentageText = askInput("Porcentaje de descuento (0-100):");
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
        StringBuilder content = new StringBuilder();
        for (Promotion promotion : promotions) {
            content.append(promotion.getId()).append(" - ").append(promotion.getName())
                    .append(" (").append(promotion.getStartDate()).append(" a ")
                    .append(promotion.getEndDate()).append(")\n");
        }
        showList("Promociones", content.toString());
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
        String saleId = askInput("Id de la venta original:");
        if (saleId == null) return;

        try {
            List<String> productIds = new java.util.ArrayList<>();
            boolean addingProducts = true;
            while (addingProducts) {
                String productId = askInput("Id de producto a devolver (deje vacío para terminar):");
                if (productId == null || productId.isBlank()) {
                    addingProducts = false;
                } else {
                    productIds.add(productId);
                }
            }

            String reason = askInput("Motivo de la devolución:");
            if (reason == null) return;

            Return processedReturn = returnService.registerReturn(saleId, productIds, reason);
            showList("Devolución registrada exitosamente", processedReturn.generateReturnReceipt());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.io.IOException e) {
            showPersistenceError();
        }
    }

    private void viewAllReturns() {
        try {
            printReturns(returnService.viewAllReturns());
        } catch (java.io.IOException e) {
            showPersistenceError();
        }
    }

    private void viewReturnsByCustomer() {
        String customerId = askInput("Id del cliente:");
        if (customerId == null) return;
        try {
            printReturns(returnService.viewReturnsByCustomer(customerId));
        } catch (java.io.IOException e) {
            showPersistenceError();
        }
    }

    private void viewReturnsBySale() {
        String saleId = askInput("Id de la venta:");
        if (saleId == null) return;
        try {
            printReturns(returnService.viewReturnsBySale(saleId));
        } catch (java.io.IOException e) {
            showPersistenceError();
        }
    }

    private void printReturns(List<Return> returns) {
        if (returns.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron devoluciones.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder content = new StringBuilder();
        for (Return r : returns) {
            content.append(r.generateReturnReceipt()).append("\n---\n");
        }
        showList("Devoluciones", content.toString());
    }

    private void consultMonthlyBalance() {
        String monthText = askInput("Mes (1-12):");
        if (monthText == null) return;
        String yearText = askInput("Año (AAAA):");
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
            showPersistenceError();
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
        String productId = askInput("Id del producto:");
        if (productId == null) return;
        String saleId = askInput("Id de la venta:");
        if (saleId == null) return;

        try {
            Warranty warranty = warrantyService.findWarrantyByProduct(productId, saleId);
            if (warranty == null) {
                JOptionPane.showMessageDialog(null, "No se encontró una garantía para ese producto en esa venta.",
                        "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showList("Garantía encontrada", warranty.generateWarrantyCertificate());
            }
        } catch (java.io.IOException e) {
            showPersistenceError();
        }
    }

    private void listAllWarranties() {
        try {
            printWarranties(warrantyService.listAllWarranties());
        } catch (java.io.IOException e) {
            showPersistenceError();
        }
    }

    private void listActiveWarranties() {
        try {
            printWarranties(warrantyService.listActiveWarranties());
        } catch (java.io.IOException e) {
            showPersistenceError();
        }
    }

    private void listWarrantiesExpiringSoon() {
        String daysAheadText = askInput("¿Con cuántos días de anticipación desea consultar?");
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
            showPersistenceError();
        }
    }

    private void printWarranties(List<Warranty> warranties) {
        if (warranties.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron garantías.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder content = new StringBuilder();
        for (Warranty warranty : warranties) {
            content.append(warranty.generateWarrantyCertificate()).append("\n---\n");
        }
        showList("Garantías", content.toString());
    }
}
