package com.gamezone.model;

import java.time.LocalDate;

/**
 * Abstract base class representing a promotional discount campaign.
 * Each concrete promotion type defines its own discount calculation
 * strategy.
 */
public abstract class Promotion {

    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    public Promotion(String id, String name, LocalDate startDate, LocalDate endDate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("The promotion id cannot be empty");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    /**
     * Checks whether this promotion is active on the given date.
     *
     * @param date the date to check
     * @return true if the date falls within the promotion's validity range
     */
    public boolean isActive(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Calculates the discount amount, in currency, that this promotion
     * would grant to the given sale. Each subclass must implement its
     * own calculation strategy.
     *
     * @param sale the sale to evaluate
     * @return the discount amount in currency
     */
    public abstract double calculateDiscount(Sale sale);
    
    /**
     * Returns a short, type-specific description of this promotion's
     * discount rule (e.g. "15% de descuento"). Declaring it here, as
     * part of Promotion's stable interface, protects every caller from
     * ever needing to know which concrete subtype it is dealing with —
     * even when a new Promotion subtype is added later.
     *
     * @return a short description of the discount rule
     */
    public abstract String getPromotionDetail();
}