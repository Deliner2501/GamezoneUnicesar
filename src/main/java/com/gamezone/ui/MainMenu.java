package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.GameZoneException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Console-based user interface for the GameZone Unicesar system.
 * Shows the main menu and delegates each area to the {@link AreaMenu}
 * it receives, so this class is only responsible for the main loop and
 * translating system exceptions into user-facing messages (GRASP audit
 * finding V-06: High Cohesion). It does not know the concrete menus, so a
 * new area can be added without editing it (SOLID audit finding V-06).
 * All text shown to the end user is written in Spanish, while the code
 * itself follows English naming conventions.
 */
public class MainMenu {

    private static final String EXIT_OPTION = "Salir";

    private final List<AreaMenu> areaMenus;

    /**
     * Creates the main menu with the area menus it shows, in the order
     * they appear in the list.
     *
     * @param areaMenus the menus of every area of the application
     */
    public MainMenu(List<AreaMenu> areaMenus) {
        this.areaMenus = areaMenus;
    }

    /**
     * Starts the main application loop, showing the menu until
     * the user chooses to exit.
     */
    public void start() {
        List<String> titles = new ArrayList<>();
        for (AreaMenu areaMenu : areaMenus) {
            titles.add(areaMenu.getTitle());
        }
        titles.add(EXIT_OPTION);
        String[] options = titles.toArray(new String[0]);

        boolean running = true;
        while (running) {
            Object selectedOption = JOptionPane.showInputDialog(
                    null,
                    "Seleccione una opción:",
                    "GameZone Unicesar",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            int choice = (selectedOption == null)
                    ? -1
                    : java.util.Arrays.asList(options).indexOf(selectedOption);

            try {
                if (choice >= 0 && choice < areaMenus.size()) {
                    areaMenus.get(choice).show();
                } else {
                    running = false;
                }
            } catch (ResourceNotFoundException e) {
                MenuDialogs.showResourceNotFound(e);
            } catch (BusinessRuleException e) {
                MenuDialogs.showBusinessRuleViolation(e);
            } catch (InvalidDataException e) {
                MenuDialogs.showInvalidData(e);
            } catch (PersistenceException e) {
                MenuDialogs.showPersistenceError();
            } catch (GameZoneException e) {
                MenuDialogs.showGenericError(e);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                        "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        JOptionPane.showMessageDialog(null, "Cerrando GameZone Unicesar. ¡Hasta pronto!",
                "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
    }
}