package com.gamezone.persistence;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import java.time.LocalDate;

/**
 * Converts the warranty type to and from its storage code, and builds the
 * concrete warranty for a code (SOLID audit finding V-08). Any code that is
 * not "EXTENDED" builds a basic warranty, as the resolver did before.
 */
public class WarrantyTypeMapper {

    private static final String BASIC_CODE = "BASIC";
    private static final String EXTENDED_CODE = "EXTENDED";

    /**
     * Returns the storage code of a warranty.
     *
     * @param warranty the warranty to identify
     * @return "EXTENDED" for extended warranties, "BASIC" otherwise
     */
    public String toTypeCode(Warranty warranty) {
        return warranty instanceof ExtendedWarranty ? EXTENDED_CODE : BASIC_CODE;
    }

    /**
     * Builds the warranty that corresponds to a storage code.
     *
     * @param typeCode  the storage code read from the file
     * @param id        the warranty id
     * @param product   the covered product
     * @param sale      the sale of the product
     * @param startDate the start date
     * @return an extended warranty for "EXTENDED", a basic one otherwise
     */
    public Warranty create(String typeCode, String id, Product product, Sale sale, LocalDate startDate) {
        if (EXTENDED_CODE.equals(typeCode)) {
            return new ExtendedWarranty(id, product, sale, startDate);
        }
        return new BasicWarranty(id, product, sale, startDate);
    }
}