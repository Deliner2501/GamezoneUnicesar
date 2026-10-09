package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Accessory;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.Warranty;
import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.persistence.ProductDAO;
import com.gamezone.persistence.AccessoryDAO;
import com.gamezone.persistence.SaleDAO;
import com.gamezone.validation.ProductValidator;
import com.gamezone.validation.SaleValidator;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the business rules for registering and consulting sales.
 * A sale must contain at least one product, and enough stock must be
 * available for every product before the sale is confirmed. Registering
 * a sale automatically reduces the stock of every product involved.
 */
public class SaleService {

    private SaleDAO saleDAO;
    private ProductDAO productDAO;
    private AccessoryDAO accessoryDAO;
    private PromotionService promotionService;
    private WarrantyService warrantyService;
    private PersonService personService;

    /**
     * Creates a SaleService with its required collaborators.
     *
     * @param saleDAO       the DAO used to persist and query sales
     * @param productDAO    the DAO used to look up and update product stock
     * @param personService the service used to resolve customers and sellers by id
     * @param accessoryDAO  the DAO used to look up and update accessory stock
     * @param promotionService the service used to find and apply the best available promotion
     */
    public SaleService(SaleDAO saleDAO, ProductDAO productDAO, AccessoryDAO accessoryDAO,
                        PromotionService promotionService, PersonService personService) {
        this.saleDAO = saleDAO;
        this.productDAO = productDAO;
        this.accessoryDAO = accessoryDAO;
        this.promotionService = promotionService;
        this.personService = personService;
    }

    /**
     * Sets the warranty service used to automatically assign basic and
     * extended warranties when a sale is registered. Injected separately
     * from the constructor to break the circular dependency between
     * SaleService and WarrantyRepository (which itself depends on
     * SaleService to resolve sale references when loading warranties).
     *
     * @param warrantyService the service used to assign warranties
     */
    public void setWarrantyService(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;
    }

    /**
     * Registers a new sale for the given customer and seller, buying the
     * requested quantity of each product. The registration follows a
     * fixed, unified sequence: resolve the customer and seller, resolve
     * every item and check its stock, validate the overall sale data,
     * build the sale and its subtotal, apply the best available
     * promotion, generate warranties, calculate the final total, update
     * the inventory, and finally persist everything.
     *
     * @param saleId            the unique identifier for the new sale
     * @param date              the date of the sale
     * @param customerId        the id of the customer making the purchase
     * @param sellerId          the id of the seller attending the sale
     * @param productQuantities a map of product id to quantity purchased
     * @param productIdsWithExtendedWarranty ids of the console products that should
     *        additionally receive an extended warranty (can be null or empty)
     * @return the registered sale, with its total already calculated
     * @throws ResourceNotFoundException if the customer, seller, or a product/accessory is not found
     * @throws InvalidDataException if a requested quantity is not positive
     * @throws BusinessRuleException if the sale has no products or stock is insufficient
     * @throws IOException if the sale cannot be persisted
     */
    public Sale registerSale(String saleId, LocalDate date, String customerId, String sellerId,
                              Map<String, Integer> productQuantities,
                              List<String> productIdsWithExtendedWarranty) throws IOException {

        Map<String, Integer> quantities = (productQuantities != null) ? productQuantities : Collections.emptyMap();

        // Step 1: resolve the customer and seller by id.
        Customer customer = personService.findCustomerById(customerId);
        if (customer == null) {
            throw new ResourceNotFoundException("cliente", customerId);
        }

        Seller seller = personService.findSellerById(sellerId);
        if (seller == null) {
            throw new ResourceNotFoundException("vendedor", sellerId);
        }

        // Step 2: resolve each item as a product or an accessory, validating
        // stock BEFORE touching any inventory, so a failure halfway through
        // never leaves the system in an inconsistent state.
        Map<Product, Integer> resolvedProducts = new LinkedHashMap<>();
        Map<Accessory, Integer> resolvedAccessories = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            if (quantity <= 0) {
                throw new InvalidDataException("cantidad",
                        "debe ser mayor que cero para el ítem " + itemId);
            }

            Product product = productDAO.findById(itemId);
            if (product != null) {
                ProductValidator.validateStockAvailability(product, quantity);
                resolvedProducts.put(product, quantity);
                continue;
            }

            Accessory accessory = accessoryDAO.findById(itemId);
            if (accessory != null) {
                if (accessory.getStock() < quantity) {
                    throw new BusinessRuleException("Stock insuficiente para el accesorio '"
                            + accessory.getTitle() + "': disponible " + accessory.getStock()
                            + ", solicitado " + quantity);
                }
                resolvedAccessories.put(accessory, quantity);
                continue;
            }

            throw new ResourceNotFoundException("producto", itemId);
        }

        // Step 3: validate the overall sale data (customer, seller already
        // resolved above; this call is what actually enforces "a sale must
        // contain at least one product").
        List<Product> allItems = new ArrayList<>(resolvedProducts.keySet());
        allItems.addAll(resolvedAccessories.keySet());
        SaleValidator.validateSaleData(customer, seller, allItems);

        // Step 4: create the sale and add every resolved item, which
        // calculates the subtotal automatically as each one is added.
        Sale sale = new Sale(saleId, date, customer, seller);
        for (Map.Entry<Product, Integer> entry : resolvedProducts.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            for (int i = 0; i < quantity; i++) {
                sale.addProduct(product);
            }
        }
        for (Map.Entry<Accessory, Integer> entry : resolvedAccessories.entrySet()) {
            Accessory accessory = entry.getKey();
            int quantity = entry.getValue();
            for (int i = 0; i < quantity; i++) {
                sale.addProduct(accessory);
            }
        }

        // Step 5: find and register the best applicable promotion. This is
        // calculated strictly over the items' subtotal, before any warranty
        // cost is added, so warranties are never discounted.
        Promotion bestPromotion = promotionService.findBestPromotionFor(sale);
        if (bestPromotion != null) {
            double discount = bestPromotion.calculateDiscount(sale);
            sale.applyPromotion(bestPromotion.getName(), discount);
        }

        // Step 6: generate the automatic basic warranty for every console in
        // the sale, plus any extended warranty explicitly requested for a
        // console, summing the extended warranties' additional cost.
        double warrantyCost = 0.0;
        for (Product product : resolvedProducts.keySet()) {
            if (product instanceof Console) {
                warrantyService.assignBasicWarranty(product, sale, sale.getDate());

                boolean wantsExtendedWarranty = productIdsWithExtendedWarranty != null
                        && productIdsWithExtendedWarranty.contains(product.getId());
                if (wantsExtendedWarranty) {
                    Warranty extendedWarranty = warrantyService.assignExtendedWarranty(product, sale, sale.getDate());
                    warrantyCost += extendedWarranty.getAdditionalCost();
                }
            }
        }

        // Step 7: register the warranty cost so the final total reflects it
        // (Sale.getFinalTotal() = subtotal - discount + additional cost).
        if (warrantyCost > 0) {
            sale.addAdditionalCost(warrantyCost);
        }

        // Step 8: update the inventory, delegating to the DAO that owns
        // each item's stock according to its type.
        for (Map.Entry<Product, Integer> entry : resolvedProducts.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            product.reduceStock(quantity);
            productDAO.update(product);
        }
        for (Map.Entry<Accessory, Integer> entry : resolvedAccessories.entrySet()) {
            Accessory accessory = entry.getKey();
            int quantity = entry.getValue();
            accessory.reduceStock(quantity);
            accessoryDAO.update(accessory);
        }

        // Step 9: persist the sale. Warranties were already persisted
        // individually as they were assigned in step 6.
        saleDAO.save(sale);
        customer.addPurchase(sale);

        return sale;
    }

    /**
     * Returns the complete history of sales registered in the store.
     *
     * @return the list of all sales
     * @throws IOException if the sales file cannot be read
     */
    public List<Sale> listSales() throws IOException {
        return saleDAO.findAll();
    }

    /**
     * Returns the purchase history of a specific customer.
     *
     * @param customerId the id of the customer
     * @return the list of sales made by that customer
     * @throws IOException if the sales file cannot be read
     */
    public List<Sale> listSalesByCustomer(String customerId) throws IOException {
        return saleDAO.findByCustomer(customerId);
    }

    /**
     * Returns the sales attended by a specific seller.
     *
     * @param sellerId the id of the seller
     * @return the list of sales attended by that seller
     * @throws IOException if the sales file cannot be read
     */
    public List<Sale> listSalesBySeller(String sellerId) throws IOException {
        return saleDAO.findBySeller(sellerId);
    }

    /**
     * Finds a sale by its id.
     *
     * @param saleId the id of the sale to find
     * @return the sale with the given id, or null if none is found
     * @throws IOException if the sales cannot be read from storage
     */
    public Sale findSaleById(String saleId) throws IOException {
        for (Sale sale : saleDAO.findAll()) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }
}