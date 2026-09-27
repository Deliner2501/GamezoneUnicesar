package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.SaleDAO;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.persistence.WarrantyRepository.WarrantyRecord;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the business logic for assigning and consulting warranties.
 * A basic warranty is generated automatically for consoles at the
 * time of sale, while an extended warranty can be optionally assigned
 * and adds a cost to the sale. Since WarrantyRepository only reads and
 * writes raw records, this service is responsible for resolving those
 * records back into real Warranty objects using SaleDAO and
 * ProductService.
 */
public class WarrantyService {

    private static final String EXTENDED_TYPE = "EXTENDED";

    private WarrantyRepository warrantyRepository;
    private SaleDAO saleDAO;
    private ProductService productService;

    /**
     * Creates a WarrantyService with its required collaborators.
     *
     * @param warrantyRepository the repository used to persist and load raw warranty records
     * @param saleDAO            the DAO used to resolve the associated sale by id
     * @param productService     the service used to resolve the associated product by id
     */
    public WarrantyService(WarrantyRepository warrantyRepository, SaleDAO saleDAO, ProductService productService) {
        this.warrantyRepository = warrantyRepository;
        this.saleDAO = saleDAO;
        this.productService = productService;
    }

    /**
     * Creates and persists a basic warranty for the given product and sale.
     *
     * @param product   the product covered by the warranty
     * @param sale      the sale the warranty is associated with
     * @param startDate the date the warranty coverage starts
     * @return the newly created basic warranty
     * @throws IllegalArgumentException if product, sale, or startDate is null
     * @throws IOException if the warranty cannot be persisted
     */
    public BasicWarranty assignBasicWarranty(Product product, Sale sale, LocalDate startDate) throws IOException {
        if (product == null || sale == null) {
            throw new IllegalArgumentException("Debe indicar el producto y la venta para asignar la garantía");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Debe indicar la fecha de inicio de la garantía");
        }

        List<Warranty> existingWarranties = loadAllWarranties();
        String warrantyId = generateWarrantyId(existingWarranties);

        BasicWarranty warranty = new BasicWarranty(warrantyId, product, sale, startDate);

        existingWarranties.add(warranty);
        warrantyRepository.saveAll(existingWarranties);

        return warranty;
    }

    /**
     * Creates and persists an extended warranty for the given product and sale.
     *
     * @param product   the product covered by the warranty
     * @param sale      the sale the warranty is associated with
     * @param startDate the date the warranty coverage starts
     * @return the newly created extended warranty
     * @throws IllegalArgumentException if product, sale, or startDate is null
     * @throws IOException if the warranty cannot be persisted
     */
    public ExtendedWarranty assignExtendedWarranty(Product product, Sale sale, LocalDate startDate) throws IOException {
        if (product == null || sale == null) {
            throw new IllegalArgumentException("Debe indicar el producto y la venta para asignar la garantía");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Debe indicar la fecha de inicio de la garantía");
        }

        List<Warranty> existingWarranties = loadAllWarranties();
        String warrantyId = generateWarrantyId(existingWarranties);

        ExtendedWarranty warranty = new ExtendedWarranty(warrantyId, product, sale, startDate);

        existingWarranties.add(warranty);
        warrantyRepository.saveAll(existingWarranties);

        return warranty;
    }

    /**
     * Finds the warranty associated with a specific product within a
     * specific sale.
     *
     * @param productId the id of the product
     * @param saleId    the id of the sale
     * @return the matching warranty, or null if none is found
     * @throws IOException if the warranties file cannot be read
     */
    public Warranty findWarrantyByProduct(String productId, String saleId) throws IOException {
        for (Warranty warranty : loadAllWarranties()) {
            if (warranty.getProduct().getId().equals(productId)
                    && warranty.getSale().getId().equals(saleId)) {
                return warranty;
            }
        }
        return null;
    }

    /**
     * Returns every warranty registered in the store.
     *
     * @return the list of all warranties
     * @throws IOException if the warranties file cannot be read
     */
    public List<Warranty> listAllWarranties() throws IOException {
        return loadAllWarranties();
    }

    /**
     * Returns every warranty that is currently active (today's date
     * falls within its coverage range).
     *
     * @return the list of currently active warranties
     * @throws IOException if the warranties file cannot be read
     */
    public List<Warranty> listActiveWarranties() throws IOException {
        List<Warranty> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Warranty warranty : loadAllWarranties()) {
            if (warranty.isActive(today)) {
                result.add(warranty);
            }
        }
        return result;
    }

    /**
     * Returns every warranty whose end date falls within the next
     * given number of days from today.
     *
     * @param daysAhead how many days ahead to look for expiring warranties
     * @return the list of warranties expiring within that window
     * @throws IOException if the warranties file cannot be read
     */
    public List<Warranty> listWarrantiesExpiringSoon(int daysAhead) throws IOException {
        if (daysAhead < 0) {
            throw new IllegalArgumentException("El número de días de anticipación no puede ser negativo");
        }

        List<Warranty> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(daysAhead);

        for (Warranty warranty : loadAllWarranties()) {
            LocalDate endDate = warranty.getEndDate();
            if (!endDate.isBefore(today) && !endDate.isAfter(limit)) {
                result.add(warranty);
            }
        }
        return result;
    }

    /**
     * Loads every raw warranty record from the repository and resolves
     * each one into a real Warranty object, skipping any record whose
     * product or sale can no longer be found.
     */
    private List<Warranty> loadAllWarranties() throws IOException {
        List<Warranty> warranties = new ArrayList<>();
        for (WarrantyRecord record : warrantyRepository.loadAll()) {
            Warranty warranty = resolveWarranty(record);
            if (warranty != null) {
                warranties.add(warranty);
            }
        }
        return warranties;
    }

    /**
     * Resolves a raw warranty record back into a real Warranty object,
     * looking up its product and sale. Returns null if either
     * reference can no longer be resolved.
     */
    private Warranty resolveWarranty(WarrantyRecord record) throws IOException {
        Product product = productService.findProductById(record.getProductId());
        if (product == null) {
            System.out.println("Skipping warranty " + record.getId() + ": product not found");
            return null;
        }

        Sale sale = findSaleById(record.getSaleId());
        if (sale == null) {
            System.out.println("Skipping warranty " + record.getId() + ": sale not found");
            return null;
        }

        if (EXTENDED_TYPE.equals(record.getType())) {
            return new ExtendedWarranty(record.getId(), product, sale, record.getStartDate());
        }
        return new BasicWarranty(record.getId(), product, sale, record.getStartDate());
    }

    private Sale findSaleById(String saleId) throws IOException {
        for (Sale sale : saleDAO.findAll()) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }

    private String generateWarrantyId(List<Warranty> existingWarranties) {
        return "GAR" + String.format("%03d", existingWarranties.size() + 1);
    }
}