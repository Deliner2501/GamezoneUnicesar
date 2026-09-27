package com.gamezone.model;

import java.time.LocalDate;

/**
 * Abstract base class representing a warranty associated with a
 * purchased product. The end date is calculated automatically in the
 * constructor based on each concrete warranty type's duration.
 */
public abstract class Warranty {

    private String id;
    private Product product;
    private Sale sale;
    private LocalDate startDate;
    private LocalDate endDate;

    public Warranty(String id, Product product, Sale sale, LocalDate startDate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("The warranty id cannot be empty");
        }
        if (product == null) {
            throw new IllegalArgumentException("A warranty must reference a product");
        }
        if (sale == null) {
            throw new IllegalArgumentException("A warranty must reference a sale");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("The start date cannot be null");
        }
        this.id = id;
        this.product = product;
        this.sale = sale;
        this.startDate = startDate;
        this.endDate = startDate.plusMonths(getDurationInMonths());
    }

    public String getId() { return id; }
    public Product getProduct() { return product; }
    public Sale getSale() { return sale; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    /**
     * Returns the duration of this warranty type, in months.
     *
     * @return the duration in months
     */
    public abstract int getDurationInMonths();

    /**
     * Returns the name of this warranty type.
     *
     * @return the warranty type name
     */
    public abstract String getWarrantyType();

    /**
     * Returns the additional cost, in currency, that this warranty
     * adds to the sale.
     *
     * @return the additional cost
     */
    public abstract double getAdditionalCost();

    /**
     * Checks whether this warranty is active on the given date.
     *
     * @param date the date to check
     * @return true if the date falls within the warranty's coverage range
     */
       public boolean isActive(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Calculates how many days remain until this warranty expires,
     * counting from the given reference date.
     *
     * @param referenceDate the date to calculate from
     * @return the number of days remaining, or a negative number if
     *         the warranty has already expired
     */
    public long getRemainingDays(LocalDate referenceDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(referenceDate, endDate);
    }

    /**
     * Builds a readable certificate for this warranty, in Spanish,
     * showing its id, type, associated product, coverage dates, and
     * additional cost.
     *
     * @return a formatted, multi-line certificate
     */
    public String generateWarrantyCertificate() {
        StringBuilder certificate = new StringBuilder();
        certificate.append("Certificado de garantía ").append(id).append("\n");
        certificate.append("Tipo: ").append(getWarrantyType()).append("\n");
        certificate.append("Producto: ").append(product.getTitle()).append("\n");
        certificate.append("Fecha de inicio: ").append(startDate).append("\n");
        certificate.append("Fecha de fin: ").append(endDate).append("\n");
        certificate.append("Costo adicional: ").append(getAdditionalCost());
        return certificate.toString();
    }
}