package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.Warranty;
import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.persistence.SaleRepository;
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

    private SaleRepository saleRepository;
    private List<SaleItemSource> itemSources;
    private PromotionService promotionService;
    private WarrantyService warrantyService;
    private PersonService personService;

    /**
     * Creates a SaleService with its required collaborators.
     *
     * @param saleRepository the repository used to persist and query sales
     * @param personService the service used to resolve customers and sellers by id
     * @param productService   the service that owns products and their stock
     * @param accessoryService the service that owns accessories and their stock
     * @param promotionService the service used to find and apply the best available promotion
     */
    public SaleService(SaleRepository saleRepository, ProductService productService, AccessoryService accessoryService,
                        PromotionService promotionService, PersonService personService) {
        this.saleRepository = saleRepository;
        this.itemSources = List.of(
            new ProductItemSource(productService),
            new AccessoryItemSource(accessoryService));
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

        Customer customer = resolveCustomer(customerId);
        Seller seller = resolveSeller(sellerId);

        Map<SaleItemSource, Map<Product, Integer>> resolvedItems = resolveSaleItems(quantities);

        List<Product> allItems = new ArrayList<>();
        for (Map<Product, Integer> items : resolvedItems.values()) {
            allItems.addAll(items.keySet());
        }
        SaleValidator.validateSaleData(customer, seller, allItems);

        Sale sale = buildSale(saleId, date, customer, seller, resolvedItems);

        applyBestPromotion(sale);

        double warrantyCost = assignWarranties(sale, allItems, productIdsWithExtendedWarranty);
        if (warrantyCost > 0) {
            sale.addAdditionalCost(warrantyCost);
        }

        updateInventory(resolvedItems);

        saleRepository.save(sale);
        customer.addPurchase(sale);

        return sale;
    }

    /**
     * Resolves the customer by id.
     *
     * @throws ResourceNotFoundException if no customer has that id
     */
    private Customer resolveCustomer(String customerId) {
        Customer customer = personService.findCustomerById(customerId);
        if (customer == null) {
            throw new ResourceNotFoundException("cliente", customerId);
        }
        return customer;
    }

    /**
     * Resolves the seller by id.
     *
     * @throws ResourceNotFoundException if no seller has that id
     */
    private Seller resolveSeller(String sellerId) {
        Seller seller = personService.findSellerById(sellerId);
        if (seller == null) {
            throw new ResourceNotFoundException("vendedor", sellerId);
        }
        return seller;
    }

        /**
     * Resolves each requested item id against the item sources in order
     * (products first, then accessories), validating stock BEFORE any
     * inventory is touched, so a failure halfway through never leaves the
     * system in an inconsistent state.
     *
     * @return the resolved items with their quantities, grouped by the
     *         source that owns them, in source order
     */
    private Map<SaleItemSource, Map<Product, Integer>> resolveSaleItems(Map<String, Integer> quantities) {
        Map<SaleItemSource, Map<Product, Integer>> resolvedItems = new LinkedHashMap<>();
        for (SaleItemSource source : itemSources) {
            resolvedItems.put(source, new LinkedHashMap<>());
        }

        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            if (quantity <= 0) {
                throw new InvalidDataException("cantidad",
                        "debe ser mayor que cero para el ítem " + itemId);
            }

            boolean found = false;
            for (SaleItemSource source : itemSources) {
                Product item = source.findItem(itemId);
                if (item != null) {
                    source.validateStock(item, quantity);
                    resolvedItems.get(source).put(item, quantity);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ResourceNotFoundException("producto", itemId);
            }
        }
        return resolvedItems;
    }

    /**
     * Creates the sale and adds every resolved item, which recalculates
     * the subtotal automatically as each one is added.
     */
    private Sale buildSale(String saleId, LocalDate date, Customer customer, Seller seller,
                            Map<SaleItemSource, Map<Product, Integer>> resolvedItems) {
        Sale sale = new Sale(saleId, date, customer, seller);
        for (Map<Product, Integer> items : resolvedItems.values()) {
            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                Product item = entry.getKey();
                int quantity = entry.getValue();
                for (int i = 0; i < quantity; i++) {
                    sale.addProduct(item);
                }
            }
        }
        return sale;
    }

    /**
     * Finds and applies the best available promotion to the sale, if any.
     * Calculated strictly over the items' subtotal, before any warranty
     * cost is added, so warranties are never discounted.
     */
    private void applyBestPromotion(Sale sale) {
        Promotion bestPromotion = promotionService.findBestPromotionFor(sale);
        if (bestPromotion != null) {
            double discount = bestPromotion.calculateDiscount(sale);
            sale.applyPromotion(bestPromotion.getName(), discount);
        }
    }

    /**
     * Assigns the automatic basic warranty to every product in the sale
     * that requires one, plus any extended warranty explicitly requested,
     * and returns the total extended-warranty cost to add to the sale.
     * Asks each product whether it requires a warranty instead of
     * checking its concrete type (GRASP audit finding V-04: Polymorphism).
     */
    private double assignWarranties(Sale sale, List<Product> items,
                                     List<String> productIdsWithExtendedWarranty) throws IOException {
        double warrantyCost = 0.0;
        for (Product product : items) {
            if (product.requiresWarranty()) {
                warrantyService.assignBasicWarranty(product, sale, sale.getDate());

                boolean wantsExtendedWarranty = productIdsWithExtendedWarranty != null
                        && productIdsWithExtendedWarranty.contains(product.getId());
                if (wantsExtendedWarranty) {
                    Warranty extendedWarranty = warrantyService.assignExtendedWarranty(product, sale, sale.getDate());
                    warrantyCost += extendedWarranty.getAdditionalCost();
                }
            }
        }
        return warrantyCost;
    }

     /**
     * Updates the inventory, delegating to the source that owns each
     * item's stock.
     */
    private void updateInventory(Map<SaleItemSource, Map<Product, Integer>> resolvedItems) {
        for (Map.Entry<SaleItemSource, Map<Product, Integer>> sourceEntry : resolvedItems.entrySet()) {
            SaleItemSource source = sourceEntry.getKey();
            for (Map.Entry<Product, Integer> entry : sourceEntry.getValue().entrySet()) {
                source.reduceStock(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Returns the complete history of sales registered in the store.
     *
     * @return the list of all sales
     * @throws IOException if the sales file cannot be read
     */
    public List<Sale> listSales() throws IOException {
        return saleRepository.findAll();
    }

    /**
     * Returns the purchase history of a specific customer.
     *
     * @param customerId the id of the customer
     * @return the list of sales made by that customer
     * @throws IOException if the sales file cannot be read
     */
    public List<Sale> listSalesByCustomer(String customerId) throws IOException {
        return saleRepository.findByCustomer(customerId);
    }

    /**
     * Returns the sales attended by a specific seller.
     *
     * @param sellerId the id of the seller
     * @return the list of sales attended by that seller
     * @throws IOException if the sales file cannot be read
     */
    public List<Sale> listSalesBySeller(String sellerId) throws IOException {
        return saleRepository.findBySeller(sellerId);
    }

    /**
     * Finds a sale by its id.
     *
     * @param saleId the id of the sale to find
     * @return the sale with the given id, or null if none is found
     * @throws IOException if the sales cannot be read from storage
     */
    public Sale findSaleById(String saleId) throws IOException {
        for (Sale sale : saleRepository.findAll()) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }
}