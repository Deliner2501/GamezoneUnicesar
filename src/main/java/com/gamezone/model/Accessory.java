package com.gamezone.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class representing an accessory sold at the store.
 * Extends Product to reuse common attributes and behaviors of a
 * sellable item, adding console compatibility information.
 */
public abstract class Accessory extends Product {

    private List<String> compatibleConsoleIds;

    public Accessory(String id, String title, double price, int stock) {
        super(id, title, price, stock);
        this.compatibleConsoleIds = new ArrayList<>();
    }

    /**
     * Returns an unmodifiable view of the console ids this accessory is
     * compatible with. Use {@link #addCompatibleConsole(String)} to add one.
     *
     * @return the unmodifiable list of compatible console ids
     */
    public List<String> getCompatibleConsoleIds() {
        return Collections.unmodifiableList(compatibleConsoleIds);
    }

    /**
     * Replaces the entire list of compatible console ids with a copy of
     * the given list. Useful when reconstructing an accessory from
     * persisted data.
     *
     * @param compatibleConsoleIds the new list of compatible console ids
     */
    public void setCompatibleConsoleIds(List<String> compatibleConsoleIds) {
        this.compatibleConsoleIds = new ArrayList<>(compatibleConsoleIds);
    }

    /**
     * Adds a console id to the list of compatible consoles.
     *
     * @param consoleId the id of the compatible console
     */
    public void addCompatibleConsole(String consoleId) {
        if (!compatibleConsoleIds.contains(consoleId)) {
            compatibleConsoleIds.add(consoleId);
        }
    }

    /**
     * Checks whether this accessory is compatible with the given console.
     *
     * @param consoleId the id of the console to check
     * @return true if compatible, false otherwise
     */
    public boolean isCompatibleWith(String consoleId) {
        return compatibleConsoleIds.contains(consoleId);
    }

    /**
     * Returns the code that identifies the concrete type of this accessory
     * (for example "CONTROLLER"). Each subclass must implement this on its own.
     *
     * @return the type code of the accessory
     */
    public abstract String getTypeCode();

    /**
     * Returns the CSV line used to persist this accessory, starting with its
     * type code. Each subclass must implement this on its own.
     *
     * @return the CSV line representing this accessory
     */
    public abstract String toCsvLine();

    /**
     * Returns the compatible console ids joined with ";", as stored in the
     * last CSV column.
     *
     * @return the joined list of compatible console ids
     */
    protected String compatibleConsolesAsCsv() {
        return String.join(";", compatibleConsoleIds);
    }

    /**
     * Returns the category of this product.
     *
     * @return {@link ProductCategory#ACCESSORY}
     */
    @Override
    public ProductCategory getCategory() {
        return ProductCategory.ACCESSORY;
    }
}