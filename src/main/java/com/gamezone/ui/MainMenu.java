package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.GameZoneException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;
import javax.swing.JOptionPane;

/**
 * Console-based user interface for the GameZone Unicesar system.
 * Shows the main menu and delegates each area (products, people, sales,
 * accessories, promotions, returns, warranties) to its own dedicated
 * menu class, so this class is only responsible for the main loop and
 * translating system exceptions into user-facing messages (GRASP audit
 * finding V-06: High Cohesion). All text shown to the end user is
 * written in Spanish, while the code itself follows English naming
 * conventions.
 */
public class MainMenu {

    private final ProductMenu productMenuView;
    private final PersonMenu personMenuView;
    private final SaleMenu saleMenuView;
    private final AccessoryMenu accessoryMenuView;
    private final PromotionMenu promotionMenuView;
    private final ReturnMenu returnMenuView;
    private final WarrantyMenu warrantyMenuView;

    /**
     * Creates the main menu with the services it depends on, building
     * the dedicated menu for each area.
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
        this.productMenuView = new ProductMenu(productService);
        this.personMenuView = new PersonMenu(personService);
        this.saleMenuView = new SaleMenu(saleService, productService);
        this.accessoryMenuView = new AccessoryMenu(accessoryService);
        this.promotionMenuView = new PromotionMenu(promotionService);
        this.returnMenuView = new ReturnMenu(returnService);
        this.warrantyMenuView = new WarrantyMenu(warrantyService);
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
                    case 4 -> promotionMenuView.show();
                    case 5 -> returnMenuView.show();
                    case 6 -> warrantyMenuView.show();
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
}