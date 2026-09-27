package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents a basic warranty, covering factory defects for 6 months
 * from the sale date, with no additional cost.
 */
public class BasicWarranty extends Warranty {

    public BasicWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    /**
     * Returns the duration of a basic warranty: 6 months.
     *
     * @return 6
     */
    @Override
    public int getDurationInMonths() {
        return 6;
    }

    /**
     * Returns the type name of this warranty.
     *
     * @return "Garantía Básica"
     */
    @Override
    public String getWarrantyType() {
        return "Garantía Básica";
    }

    /**
     * Returns the additional cost of a basic warranty: none.
     *
     * @return 0.0
     */
    @Override
    public double getAdditionalCost() {
        return 0.0;
    }
}