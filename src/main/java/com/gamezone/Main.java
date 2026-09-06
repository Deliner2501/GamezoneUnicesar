package com.gamezone;

import com.gamezone.persistence.PersonDAO;
import com.gamezone.persistence.ProductDAO;
import com.gamezone.persistence.SaleDAO;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import com.gamezone.ui.MainMenu;
import java.io.IOException;

/**
 * Entry point of the GameZone Unicesar application.
 * Wires together the persistence, service, and UI layers, then
 * starts the console menu.
 */
public class Main {

    /**
     * Starts the GameZone Unicesar system.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            // Persistence layer
            PersonDAO personDAO = new PersonDAO();
            ProductDAO productDAO = new ProductDAO();
            SaleDAO saleDAO = new SaleDAO(personDAO, productDAO);

            // Service layer
            PersonService personService = new PersonService();
            ProductService productService = new ProductService(productDAO);
            SaleService saleService = new SaleService(saleDAO, productDAO, personService);

            // UI layer
            MainMenu mainMenu = new MainMenu(personService, productService, saleService);
            mainMenu.start();

        } catch (IOException e) {
            System.out.println("Error fatal al iniciar la aplicación: " + e.getMessage());
        }
    }
}
