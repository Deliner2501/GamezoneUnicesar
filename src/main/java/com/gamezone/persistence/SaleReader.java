package com.gamezone.persistence;

import com.gamezone.model.Sale;
import java.io.IOException;
import java.util.List;

/**
 * Read-only view of the stored sales. Classes that only need to look
 * sales up depend on this interface instead of on a concrete DAO
 * (SOLID audit finding V-03).
 */
public interface SaleReader {

    /**
     * Returns every stored sale.
     *
     * @return the list of all persisted sales, or an empty list if none exist
     * @throws IOException if the sales cannot be read
     */
    List<Sale> findAll() throws IOException;
}
