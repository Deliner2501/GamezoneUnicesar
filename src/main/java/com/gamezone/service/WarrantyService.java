package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.exceptions.InvalidDataException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * Contains the business logic for assigning and consulting warranties.
 * A basic warranty is generated automatically for consoles at the
 * time of sale, while an extended warranty can be optionally assigned
 * and adds a cost to the sale. Since WarrantyRepository only reads and
 * writes raw records, the WarrantyRecordResolver rebuilds the real
 * Warranty objects from them.
 */
public class WarrantyService {


    private WarrantyRepository warrantyRepository;
    private WarrantyRecordResolver recordResolver;

    /**
     * Creates a WarrantyService with its required collaborators.
     *
     * @param warrantyRepository the repository used to persist raw warranty records
     * @param recordResolver     the resolver that rebuilds warranties from stored records
     */
    public WarrantyService(WarrantyRepository warrantyRepository, WarrantyRecordResolver recordResolver) {
        this.warrantyRepository = warrantyRepository;
        this.recordResolver = recordResolver;
    }

    /**
     * Creates and persists a basic warranty for the given product and sale.
     *
     * @param product   the product covered by the warranty
     * @param sale      the sale the warranty is associated with
     * @param startDate the date the warranty coverage starts
     * @return the newly created basic warranty
     * @throws InvalidDataException if product, sale, or startDate is null
     * @throws IOException if the warranty cannot be persisted
     */
    public BasicWarranty assignBasicWarranty(Product product, Sale sale, LocalDate startDate) throws IOException {
        if (product == null || sale == null) {
            throw new InvalidDataException("producto/venta", "debe indicar el producto y la venta para asignar la garantía");
        }
        if (startDate == null) {
            throw new InvalidDataException("fecha de inicio", "debe indicar la fecha de inicio de la garantía");
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
     * @throws InvalidDataException if product, sale, or startDate is null
     * @throws IOException if the warranty cannot be persisted
     */
    public ExtendedWarranty assignExtendedWarranty(Product product, Sale sale, LocalDate startDate) throws IOException {
        if (product == null || sale == null) {
            throw new InvalidDataException("producto/venta", "debe indicar el producto y la venta para asignar la garantía");
        }
        if (startDate == null) {
            throw new InvalidDataException("fecha de inicio", "debe indicar la fecha de inicio de la garantía");
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
            throw new InvalidDataException("días de anticipación", "no puede ser negativo");
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
     * Cancels every warranty associated with the given product within
     * the given sale, removing them permanently, and returns the total
     * reimbursable cost of the canceled warranties (zero for a basic
     * warranty, the additional cost for an extended one). Used when a
     * console is returned, since a returned console cannot keep an
     * active warranty.
     *
     * @param productId the id of the product whose warranties are canceled
     * @param saleId    the id of the sale the warranties belong to
     * @return the sum of the additional cost of every canceled warranty
     * @throws IOException if the warranties cannot be read or persisted
     */
    public double cancelWarranties(String productId, String saleId) throws IOException {
        List<Warranty> allWarranties = loadAllWarranties();
        List<Warranty> remainingWarranties = new ArrayList<>();
        double reimbursableCost = 0.0;

        for (Warranty warranty : allWarranties) {
            boolean matches = warranty.getProduct().getId().equals(productId)
                    && warranty.getSale().getId().equals(saleId);
            if (matches) {
                reimbursableCost += warranty.getAdditionalCost();
            } else {
                remainingWarranties.add(warranty);
            }
        }

        warrantyRepository.saveAll(remainingWarranties);
        return reimbursableCost;
    }

    /**
     * Loads every registered warranty through the record resolver.
     */
    private List<Warranty> loadAllWarranties() throws IOException {
        return recordResolver.loadAll();
    }

    private String generateWarrantyId(List<Warranty> existingWarranties) {
        return "GAR" + String.format("%03d", existingWarranties.size() + 1);
    }
}