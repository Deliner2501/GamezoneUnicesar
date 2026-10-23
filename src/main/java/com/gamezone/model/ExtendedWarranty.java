package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents an extended warranty, covering factory defects and
 * accidental damage for 12 months from the sale date, with an
 * additional cost equivalent to 10% of the product's price.
 */
public class ExtendedWarranty extends Warranty {

    public ExtendedWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    /**
     * Returns the duration of an extended warranty: 12 months.
     *
     * @return 12
     */
    @Override
    public int getDurationInMonths() {
        return 12;
    }

    /**
     * Returns the type name of this warranty.
     *
     * @return "Garantía Extendida"
     */
    @Override
    public String getWarrantyType() {
        return "Garantía Extendida";
    }

    /**
     * Returns the additional cost of an extended warranty: 10% of the
     * associated product's price.
     *
     * @return the additional cost
     */
    @Override
    public double getAdditionalCost() {
        return getProduct().getPrice() * 0.10;
    }
}