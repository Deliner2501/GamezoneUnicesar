
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
            System.out.println("1. Manage products");
            System.out.println("2. Manage customers and sellers");
            System.out.println("3. Manage sales");
            System.out.println("0. Exit");
            System.out.print("Select an option: ");

            String option = scanner.nextLine();
            switch (option) {
                case "1" -> productMenu();
                case "2" -> personMenu();
                case "3" -> saleMenu();
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
        System.out.println("Closing GameZone Unicesar. See you soon!");
    }

    // ===================== PRODUCT MENU =====================

    private void productMenu() {
        System.out.println("\n--- Product management ---");
        System.out.println("1. Register a video game");
        System.out.println("2. Register a console");
        System.out.println("3. List available products");
        System.out.println("0. Back");
        System.out.print("Select an option: ");

        switch (scanner.nextLine()) {
            case "1" -> registerVideoGame();
            case "2" -> registerConsole();
            case "3" -> listProducts();
            case "0" -> { }
            default -> System.out.println("Invalid option.");
        }
    }

    private void registerVideoGame() {
        try {
            System.out.print("Id: ");
            String id = scanner.nextLine();
            System.out.print("Title: ");
            String title = scanner.nextLine();
            System.out.print("Price: ");
            double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Stock: ");
            int stock = Integer.parseInt(scanner.nextLine());
            System.out.print("Platform: ");
            String platform = scanner.nextLine();
            System.out.print("Genre: ");
            String genre = scanner.nextLine();
            System.out.print("Age rating: ");
            String ageRating = scanner.nextLine();

            Product videoGame = new VideoGame(id, title, price, stock, platform, genre, ageRating);
            productService.registerProduct(videoGame);
            System.out.println("Video game registered successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void registerConsole() {
        try {
            System.out.print("Id: ");
            String id = scanner.nextLine();
            System.out.print("Title: ");
            String title = scanner.nextLine();
            System.out.print("Price: ");
            double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Stock: ");
            int stock = Integer.parseInt(scanner.nextLine());
            System.out.print("Brand: ");
            String brand = scanner.nextLine();
            System.out.print("Model: ");
            String model = scanner.nextLine();
            System.out.print("Generation: ");
            String generation = scanner.nextLine();

            Product console = new Console(id, title, price, stock, brand, model, generation);
            productService.registerProduct(console);
            System.out.println("Console registered successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listProducts() {
        List<Product> products = productService.listAvailableProducts();
        if (products.isEmpty()) {
            System.out.println("No products registered yet.");
            return;
        }
        for (Product product : products) {
            System.out.println(product.getFullDescription());
        }
    }

    // ===================== PERSON MENU =====================

    private void personMenu() {
        System.out.println("\n--- Customer and seller management ---");
        System.out.println("1. Register a customer");
        System.out.println("2. List customers");
        System.out.println("3. List sellers");
        System.out.println("0. Back");
        System.out.print("Select an option: ");

        switch (scanner.nextLine()) {
            case "1" -> registerCustomer();
            case "2" -> listCustomers();
            case "3" -> listSellers();
            case "0" -> { }
            default -> System.out.println("Invalid option.");
        }
    }

    private void registerCustomer() {
        try {
            System.out.print("Id: ");
            String id = scanner.nextLine();
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Phone: ");
            String phone = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();

            personService.registerCustomer(name, id, phone, email);
            System.out.println("Customer registered successfully.");
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listCustomers() {
        List<Customer> customers = personService.listCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers registered yet.");
            return;
        }
        customers.forEach(customer -> System.out.println(customer.toString()));
    }

    private void listSellers() {
        List<Seller> sellers = personService.listSellers();
        if (sellers.isEmpty()) {
            System.out.println("No sellers registered yet.");
            return;
        }
        sellers.forEach(seller -> System.out.println(seller.toString()));
    }

    // ===================== SALE MENU =====================

    private void saleMenu() {
        System.out.println("\n--- Sale management ---");
        System.out.println("1. Register a sale");
        System.out.println("2. List all sales");
        System.out.println("3. List purchase history for a customer");
        System.out.println("4. List sales attended by a seller");
        System.out.println("0. Back");
        System.out.print("Select an option: ");

        switch (scanner.nextLine()) {
            case "1" -> registerSale();
            case "2" -> listSales();
            case "3" -> listSalesByCustomer();
            case "4" -> listSalesBySeller();
            case "0" -> { }
            default -> System.out.println("Invalid option.");
        }
    }

    private void registerSale() {
        try {
            System.out.print("Sale id: ");
            String saleId = scanner.nextLine();
            System.out.print("Customer id: ");
            String customerId = scanner.nextLine();
            System.out.print("Seller id: ");
            String sellerId = scanner.nextLine();

            Map<String, Integer> productQuantities = new LinkedHashMap<>();
            boolean addingProducts = true;
            while (addingProducts) {
                System.out.print("Product id (leave empty to finish): ");
                String productId = scanner.nextLine();
                if (productId.isBlank()) {
                    addingProducts = false;
                    continue;
                }
                System.out.print("Quantity: ");
                int quantity = Integer.parseInt(scanner.nextLine());
                productQuantities.merge(productId, quantity, Integer::sum);
            }

            Sale sale = saleService.registerSale(saleId, LocalDate.now(), customerId, sellerId, productQuantities);
            System.out.println("Sale registered successfully. Total: " + sale.getTotal());
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listSales() {
        try {
            List<Sale> sales = saleService.listSales();
            printSales(sales);
        } catch (IOException e) {
            System.out.println("Error reading sales: " + e.getMessage());
        }
    }

    private void listSalesByCustomer() {
        try {
            System.out.print("Customer id: ");
            String customerId = scanner.nextLine();
            printSales(saleService.listSalesByCustomer(customerId));
        } catch (IOException e) {
            System.out.println("Error reading sales: " + e.getMessage());
        }
    }

    private void listSalesBySeller() {
        try {
            System.out.print("Seller id: ");
            String sellerId = scanner.nextLine();
            printSales(saleService.listSalesBySeller(sellerId));
        } catch (IOException e) {
            System.out.println("Error reading sales: " + e.getMessage());
        }
    }

    private void printSales(List<Sale> sales) {
        if (sales.isEmpty()) {
            System.out.println("No sales found.");
            return;
        }
        sales.forEach(sale -> System.out.println(sale.toString()));
    }
}
