package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.GameZoneException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Shared Swing dialog helpers used by every menu area (products, people,
 * sales, accessories, promotions, returns, warranties). Pulled out of
 * MainMenu so each area's own menu class only knows its own operations,
 * not how dialogs are drawn or how system exceptions are translated into
 * user-facing messages (GRASP audit finding V-06: High Cohesion).
 */
public final class MenuDialogs {

    private MenuDialogs() {
        // utility class, not meant to be instantiated
    }

    // ===================== ERROR HANDLING =====================

    /**
     * Shows the message for a resource that does not exist in the system
     * (product, customer, seller or sale).
     *
     * @param e the exception describing the missing resource
     */
    public static void showResourceNotFound(ResourceNotFoundException e) {
        JOptionPane.showMessageDialog(null, "No se encontró el recurso solicitado: " + e.getMessage(),
                "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows the message for an operation that violates a business rule
     * (insufficient stock, duplicated customer, sale without products).
     *
     * @param e the exception describing the violated rule
     */
    public static void showBusinessRuleViolation(BusinessRuleException e) {
        JOptionPane.showMessageDialog(null, "No se puede completar la operación: " + e.getMessage(),
                "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows the message for input data that fails format or range checks.
     *
     * @param e the exception describing the invalid field
     */
    public static void showInvalidData(InvalidDataException e) {
        JOptionPane.showMessageDialog(null, "Los datos ingresados no son válidos: " + e.getMessage(),
                "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a generic message for storage problems. It never exposes file
     * names or technical details to the user.
     */
    public static void showPersistenceError() {
        JOptionPane.showMessageDialog(null,
                "Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.",
                "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a fallback message for any GameZoneException subtype that has
     * no dedicated handler, so new exception types never reach the user raw.
     *
     * @param e the unclassified GameZone exception
     */
    public static void showGenericError(GameZoneException e) {
        JOptionPane.showMessageDialog(null, "No se puede completar la operación: " + e.getMessage(),
                "GameZone Unicesar - Error", JOptionPane.ERROR_MESSAGE);
    }

    // ===================== UI HELPERS =====================

    /**
     * Shows a single-line input dialog and returns what the user typed.
     *
     * @param label the label describing the requested value
     * @return the text entered, or null if the dialog was cancelled/closed
     */
    public static String askInput(String label) {
        return JOptionPane.showInputDialog(null, label, "GameZone Unicesar", JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Shows a block of text inside a scrollable, read-only text area,
     * used to display listings that may be too long for a plain dialog.
     *
     * @param title   the dialog title
     * @param content the text content to display
     */
    public static void showList(String title, String content) {
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        textArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        textArea.setMargin(new java.awt.Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(560, 420));

        JOptionPane.showMessageDialog(null, scrollPane, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Formats a list of entries as a numbered block of text, with a
     * separator line between entries, for display in a scrollable list.
     *
     * @param entries the entries to format, in display order
     * @return the formatted, ready-to-display text
     */
    public static String formatEntries(List<String> entries) {
        String separator = "-".repeat(60);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            content.append(i + 1).append(". ").append(entries.get(i)).append("\n");
            if (i < entries.size() - 1) {
                content.append(separator).append("\n");
            }
        }
        return content.toString();
    }
}
