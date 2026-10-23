package com.gamezone.persistence;

import com.gamezone.model.Sale;
import java.io.IOException;
import java.util.List;

/**
 * Defines the storage operations the service layer needs for sales:
 * everything in {@link SaleReader} plus saving and the per-person
 * queries (SOLID audit finding V-03).
 */
public interface SaleRepository extends SaleReader {

    /**
     * Saves a new sale.
     *
     * @param sale the sale to persist
     * @throws IOException if the sale cannot be written
     */
    void save(Sale sale) throws IOException;

    /**
     * Returns every sale made by the customer with the given id.
     *
     * @param customerId the id of the customer
     * @return the sales made by that customer
     * @throws IOException if the sales cannot be read
     */
    List<Sale> findByCustomer(String customerId) throws IOException;

    /**
     * Returns every sale attended by the seller with the given id.
     *
     * @param sellerId the id of the seller
     * @return the sales attended by that seller
     * @throws IOException if the sales cannot be read
     */
    List<Sale> findBySeller(String sellerId) throws IOException;
}
