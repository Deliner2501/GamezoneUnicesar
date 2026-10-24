package com.gamezone.ui;

import com.gamezone.model.Product;
import com.gamezone.model.ProductCategory;
import com.gamezone.service.ProductService;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Handles the "Gestionar productos" menu: registering video games and
 * consoles, and listing available products. Extracted out of MainMenu
 * so this area's UI logic is cohesive on its own (GRASP audit finding
 * V-06: High Cohesion).
 */
public class ProductMenu implements AreaMenu{

    private final ProductService productService;

    /**
     * Creates the product menu with the service it depends on.
     *
     * @param productService the service used to register and list products
     */
    public ProductMenu(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public String getTitle() {
        return "Gestionar productos";
    }
    
    /**
     * Shows the product menu and dispatches the chosen option.
     */
    @Override
    public void show() {
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
        String id = MenuDialogs.askInput("Id:");
        if (id == null) return;
        String title = MenuDialogs.askInput("Título:");
        if (title == null) return;
        String priceText = MenuDialogs.askInput("Precio:");
        if (priceText == null) return;
        String stockText = MenuDialogs.askInput("Cantidad en inventario:");
        if (stockText == null) return;
        String platform = MenuDialogs.askInput("Plataforma:");
        if (platform == null) return;
        String genre = MenuDialogs.askInput("Género:");
        if (genre == null) return;
        String ageRating = MenuDialogs.askInput("Clasificación de edad:");
        if (ageRating == null) return;

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);

            productService.registerNewProduct(ProductCategory.VIDEOGAME, id, title, price, stock,
                    platform, genre, ageRating);
            JOptionPane.showMessageDialog(null, "Videojuego registrado exitosamente.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Error: el precio y la cantidad deben ser valores numéricos válidos.",
                    "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerConsole() {
        String id = MenuDialogs.askInput("Id:");
        if (id == null) return;
        String title = MenuDialogs.askInput("Título:");
        if (title == null) return;
        String priceText = MenuDialogs.askInput("Precio:");
        if (priceText == null) return;
        String stockText = MenuDialogs.askInput("Cantidad en inventario:");
        if (stockText == null) return;
        String brand = MenuDialogs.askInput("Marca:");
        if (brand == null) return;
        String model = MenuDialogs.askInput("Modelo:");
        if (model == null) return;
        String generation = MenuDialogs.askInput("Generación:");
        if (generation == null) return;

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);

            productService.registerNewProduct(ProductCategory.CONSOLE, id, title, price, stock,
                    brand, model, generation);
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
        List<String> entries = new ArrayList<>();
        for (Product product : products) {
            entries.add(product.getFullDescription());
        }
        MenuDialogs.showList("Productos disponibles", MenuDialogs.formatEntries(entries));
    }
}