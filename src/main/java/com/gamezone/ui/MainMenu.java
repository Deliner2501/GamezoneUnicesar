package com.gamezone.ui;

import com.gamezone.model.Console;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.VideoGame;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
    private final SaleService saleService;
    private final Scanner scanner;

    /**
     * Creates the main menu with the services it depends on.
     *
     * @param personService  the service for managing customers and sellers
     * @param productService the service for managing products
     * @param saleService    the service for registering and querying sales
     */
    public MainMenu(PersonService personService, ProductService productService, SaleService saleService) {
        this.personService = personService;
        this.productService = productService;
        this.saleService = saleService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the main application loop, showing the menu until
     * the user chooses to exit.
     */
    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== GameZone Unicesar =====");
            System.out.println("1. Gestionar productos");
            System.out.println("2. Gestionar clientes y vendedores");
            System.out.println("3. Gestionar ventas");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            String option = scanner.nextLine();
            switch (option) {
                case "1" -> productMenu();
                case "2" -> personMenu();
                case "3" -> saleMenu();
                case "0" -> running = false;
                default -> System.out.println("Opción inválida.");
            }
        }
        System.out.println("Cerrando GameZone Unicesar. ¡Hasta pronto!");
    }

    // ===================== PRODUCT MENU =====================

    private void productMenu() {
        System.out.println("\n--- Gestión de productos ---");
        System.out.println("1. Registrar un videojuego");
        System.out.println("2. Registrar una consola");
        System.out.println("3. Listar productos disponibles");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        switch (scanner.nextLine()) {
            case "1" -> registerVideoGame();
            case "2" -> registerConsole();
            case "3" -> listProducts();
            case "0" -> { }
            default -> System.out.println("Opción inválida.");
        }
    }

    private void registerVideoGame() {
        try {
            System.out.print("Id: ");
            String id = scanner.nextLine();
            System.out.print("Título: ");
            String title = scanner.nextLine();
            System.out.print("Precio: ");
            double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Cantidad en inventario: ");
            int stock = Integer.parseInt(scanner.nextLine());
            System.out.print("Plataforma: ");
            String platform = scanner.nextLine();
            System.out.print("Género: ");
            String genre = scanner.nextLine();
            System.out.print("Clasificación de edad: ");
            String ageRating = scanner.nextLine();

            Product videoGame = new VideoGame(id, title, price, stock, platform, genre, ageRating);
            productService.registerProduct(videoGame);
            System.out.println("Videojuego registrado exitosamente.");
        } catch (NumberFormatException e) {
            System.out.println("Error: el precio y la cantidad deben ser valores numéricos válidos.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void registerConsole() {
        try {
            System.out.print("Id: ");
            String id = scanner.nextLine();
            System.out.print("Título: ");
            String title = scanner.nextLine();
            System.out.print("Precio: ");
            double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Cantidad en inventario: ");
            int stock = Integer.parseInt(scanner.nextLine());
            System.out.print("Marca: ");
            String brand = scanner.nextLine();
            System.out.print("Modelo: ");
            String model = scanner.nextLine();
            System.out.print("Generación: ");
            String generation = scanner.nextLine();

            Product console = new Console(id, title, price, stock, brand, model, generation);
            productService.registerProduct(console);
            System.out.println("Consola registrada exitosamente.");
        } catch (NumberFormatException e) {
            System.out.println("Error: el precio y la cantidad deben ser valores numéricos válidos.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listProducts() {
        List<Product> products = productService.listAvailableProducts();
        if (products.isEmpty()) {
            System.out.println("Aún no hay productos registrados.");
            return;
        }
        for (Product product : products) {
            System.out.println(product.getFullDescription());
        }
    }

    // ===================== PERSON MENU =====================

    private void personMenu() {
        System.out.println("\n--- Gestión de clientes y vendedores ---");
        System.out.println("1. Registrar un cliente");
        System.out.println("2. Listar clientes");
        System.out.println("3. Listar vendedores");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        switch (scanner.nextLine()) {
            case "1" -> registerCustomer();
            case "2" -> listCustomers();
            case "3" -> listSellers();
            case "0" -> { }
            default -> System.out.println("Opción inválida.");
        }
    }

    private void registerCustomer() {
        try {
            System.out.print("Id: ");
            String id = scanner.nextLine();
            System.out.print("Nombre: ");
            String name = scanner.nextLine();
            System.out.print("Teléfono: ");
            String phone = scanner.nextLine();
            System.out.print("Correo electrónico: ");
            String email = scanner.nextLine();

            personService.registerCustomer(name, id, phone, email);
            System.out.println("Cliente registrado exitosamente.");
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listCustomers() {
        List<Customer> customers = personService.listCustomers();
        if (customers.isEmpty()) {
            System.out.println("Aún no hay clientes registrados.");
            return;
        }
        customers.forEach(customer -> System.out.println(customer.toString()));
    }

    private void listSellers() {
        List<Seller> sellers = personService.listSellers();
        if (sellers.isEmpty()) {
            System.out.println("Aún no hay vendedores registrados.");
            return;
        }
        sellers.forEach(seller -> System.out.println(seller.toString()));
    }

    // ===================== SALE MENU =====================

    private void saleMenu() {
        System.out.println("\n--- Gestión de ventas ---");
        System.out.println("1. Registrar una venta");
        System.out.println("2. Listar todas las ventas");
        System.out.println("3. Consultar historial de compras de un cliente");
        System.out.println("4. Consultar ventas atendidas por un vendedor");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        switch (scanner.nextLine()) {
            case "1" -> registerSale();
            case "2" -> listSales();
            case "3" -> listSalesByCustomer();
            case "4" -> listSalesBySeller();
            case "0" -> { }
            default -> System.out.println("Opción inválida.");
        }
    }

    private void registerSale() {
        try {
            System.out.print("Id de la venta: ");
            String saleId = scanner.nextLine();
            System.out.print("Id del cliente: ");
            String customerId = scanner.nextLine();
            System.out.print("Id del vendedor: ");
            String sellerId = scanner.nextLine();

            Map<String, Integer> productQuantities = new LinkedHashMap<>();
            boolean addingProducts = true;
            while (addingProducts) {
                System.out.print("Id del producto (deje vacío para terminar): ");
                String productId = scanner.nextLine();
                if (productId.isBlank()) {
                    addingProducts = false;
                    continue;
                }
                System.out.print("Cantidad: ");
                int quantity = Integer.parseInt(scanner.nextLine());
                productQuantities.merge(productId, quantity, Integer::sum);
            }

            Sale sale = saleService.registerSale(saleId, LocalDate.now(), customerId, sellerId, productQuantities);
            System.out.println("Venta registrada exitosamente. Total: " + sale.getTotal());
        } catch (NumberFormatException e) {
            System.out.println("Error: la cantidad debe ser un valor numérico válido.");
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listSales() {
        try {
            List<Sale> sales = saleService.listSales();
            printSales(sales);
        } catch (IOException e) {
            System.out.println("Error al leer las ventas: " + e.getMessage());
        }
    }

    private void listSalesByCustomer() {
        try {
            System.out.print("Id del cliente: ");
            String customerId = scanner.nextLine();
            printSales(saleService.listSalesByCustomer(customerId));
        } catch (IOException e) {
            System.out.println("Error al leer las ventas: " + e.getMessage());
        }
    }

    private void listSalesBySeller() {
        try {
            System.out.print("Id del vendedor: ");
            String sellerId = scanner.nextLine();
            printSales(saleService.listSalesBySeller(sellerId));
        } catch (IOException e) {
            System.out.println("Error al leer las ventas: " + e.getMessage());
        }
    }

    private void printSales(List<Sale> sales) {
        if (sales.isEmpty()) {
            System.out.println("No se encontraron ventas.");
            return;
        }
        sales.forEach(sale -> System.out.println(sale.toString()));
    }
}
