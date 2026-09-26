package com.gamezone.model;

import java.util.ArrayList;
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
     * Returns the list of console ids this accessory is compatible with.
     *
     * @return the list of compatible console ids
     */
    public List<String> getCompatibleConsoleIds() {
        return compatibleConsoleIds;
    }
        /**
     * Replaces the entire list of compatible console ids.
     * Useful when reconstructing an accessory from persisted data.
     *
     * @param compatibleConsoleIds the new list of compatible console ids
     */
    public void setCompatibleConsoleIds(List<String> compatibleConsoleIds) {
        this.compatibleConsoleIds = compatibleConsoleIds;
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
}