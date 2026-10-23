package com.gamezone;

import com.gamezone.persistence.AccessoryDAO;
import com.gamezone.persistence.CustomerDAO;
import com.gamezone.persistence.SellerDAO;
import com.gamezone.persistence.ProductDAO;
import com.gamezone.persistence.PromotionDAO;
import com.gamezone.persistence.ReturnRepository;
import com.gamezone.persistence.SaleDAO;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.ConsoleFactory;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.WarrantyRecordResolver;
import com.gamezone.service.ReturnService;
import com.gamezone.service.ReturnRecordResolver;
import com.gamezone.service.SaleService;
import com.gamezone.service.VideoGameFactory;
import com.gamezone.service.WarrantyService;
import com.gamezone.ui.MainMenu;
import java.util.List;
import javax.swing.SwingUtilities;

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
        // Persistence layer
        CustomerDAO customerDAO = new CustomerDAO();
        SellerDAO sellerDAO = new SellerDAO();
        ProductDAO productDAO = new ProductDAO();
        AccessoryDAO accessoryDAO = new AccessoryDAO();
        PromotionDAO promotionDAO = new PromotionDAO();
        SaleDAO saleDAO = new SaleDAO(customerDAO, sellerDAO, productDAO);

        // Service layer
        // Service layer
        PersonService personService = new PersonService(customerDAO, sellerDAO);
        ProductService productService = new ProductService(productDAO,
                List.of(new VideoGameFactory(), new ConsoleFactory()));
        AccessoryService accessoryService = new AccessoryService(accessoryDAO);
        PromotionService promotionService = new PromotionService(promotionDAO);
        SaleService saleService = new SaleService(saleDAO, productService, accessoryService, promotionService, personService);
        WarrantyRepository warrantyRepository = new WarrantyRepository();
        WarrantyRecordResolver warrantyRecordResolver = new WarrantyRecordResolver(warrantyRepository, saleDAO, productService);
        WarrantyService warrantyService = new WarrantyService(warrantyRepository, warrantyRecordResolver);
        saleService.setWarrantyService(warrantyService);
        ReturnRepository returnRepository = new ReturnRepository();
        ReturnRecordResolver returnRecordResolver = new ReturnRecordResolver(returnRepository, saleService, productService, accessoryService);
        ReturnService returnService = new ReturnService(returnRepository, saleService, productService, accessoryService, warrantyService, returnRecordResolver);

        // UI layer
        MainMenu mainMenu = new MainMenu(personService, productService, saleService, accessoryService, promotionService, returnService, warrantyService);
        SwingUtilities.invokeLater(mainMenu::start);
    }
}