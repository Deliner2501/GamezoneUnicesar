package com.gamezone.ui;

/**
 * A menu for one area of the application (products, people, sales...).
 * {@link MainMenu} only knows this interface, so adding a new area means
 * writing a new implementation and registering it in {@code Main}, without
 * editing {@code MainMenu} (SOLID audit finding V-06).
 */
public interface AreaMenu {

    /**
     * Returns the text shown for this area in the main menu.
     *
     * @return the title of the area, in Spanish
     */
    String getTitle();

    /**
     * Shows the menu of this area and handles the chosen options until
     * the user goes back.
     */
    void show();
}
