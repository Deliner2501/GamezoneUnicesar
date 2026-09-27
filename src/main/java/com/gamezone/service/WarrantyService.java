package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.WarrantyRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the business logic for assigning and consulting warranties.
 * A basic warranty is generated automatically for consoles at the
 * time of sale, while an extended warranty can be optionally assigned
 * and adds a cost to the sale.
 */
public class WarrantyService {

    private WarrantyRepository warrantyRepository;

    /**
     * Creates a WarrantyService with its required collaborator.
     *
     * @param warrantyRepository the repository used to persist and load warranties
     */
    public WarrantyService(WarrantyRepository warrantyRepository) {
        this.warrantyRepository = warrantyRepository;
    }

    /**
     * Creates and persists a basic warranty for the given product and sale.
     *
     * @param product   the product covered by the warranty
     * @param sale      the sale the warranty is associated with
     * @param startDate the date the warranty coverage starts
     * @return the newly created basic warranty
     * @throws IOException if the warranty cannot be persisted
     */
    public BasicWarranty assignBasicWarranty(Product product, Sale sale, LocalDate startDate) throws IOException {
        List<Warranty> existingWarranties = warrantyRepository.loadAll();
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
     * @throws IOException if the warranty cannot be persisted
     */
    public ExtendedWarranty assignExtendedWarranty(Product product, Sale sale, LocalDate startDate) throws IOException {
        List<Warranty> existingWarranties = warrantyRepository.loadAll();
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
        for (Warranty warranty : warrantyRepository.loadAll()) {
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
        return warrantyRepository.loadAll();
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
        for (Warranty warranty : warrantyRepository.loadAll()) {
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
        List<Warranty> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(daysAhead);

        for (Warranty warranty : warrantyRepository.loadAll()) {
            LocalDate endDate = warranty.getEndDate();
            if (!endDate.isBefore(today) && !endDate.isAfter(limit)) {
                result.add(warranty);
            }
        }
        return result;
    }

    private String generateWarrantyId(List<Warranty> existingWarranties) {
        return "GAR" + String.format("%03d", existingWarranties.size() + 1);
    }
}