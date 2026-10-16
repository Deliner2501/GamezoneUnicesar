package com.gamezone.ui;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;
import com.gamezone.service.PersonService;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Handles the "Gestionar clientes y vendedores" menu: registering
 * customers and listing customers/sellers. Extracted out of MainMenu
 * so this area's UI logic is cohesive on its own (GRASP audit finding
 * V-06: High Cohesion).
 */
public class PersonMenu {

    private final PersonService personService;

    /**
     * Creates the person menu with the service it depends on.
     *
     * @param personService the service used to register and list customers and sellers
     */
    public PersonMenu(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Shows the person menu and dispatches the chosen option.
     */
    public void show() {
        String[] options = {
                "Registrar un cliente",
                "Listar clientes",
                "Listar vendedores",
                "Volver"
        };
        Object selectedOption = JOptionPane.showInputDialog(
                null,
                "Gestión de clientes y vendedores - Seleccione una opción:",
                "GameZone Unicesar",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        int choice = (selectedOption == null)
                ? -1
                : java.util.Arrays.asList(options).indexOf(selectedOption);

        switch (choice) {
            case 0 -> registerCustomer();
            case 1 -> listCustomers();
            case 2 -> listSellers();
            default -> { }
        }
    }

    private void registerCustomer() {
        String id = MenuDialogs.askInput("Id:");
        if (id == null) return;
        String name = MenuDialogs.askInput("Nombre:");
        if (name == null) return;
        String phone = MenuDialogs.askInput("Teléfono:");
        if (phone == null) return;
        String email = MenuDialogs.askInput("Correo electrónico:");
        if (email == null) return;

        personService.registerCustomer(name, id, phone, email);
        JOptionPane.showMessageDialog(null, "Cliente registrado exitosamente.",
                "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
    }

    private void listCustomers() {
        List<Customer> customers = personService.listCustomers();
        if (customers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Aún no hay clientes registrados.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<String> entries = new ArrayList<>();
        for (Customer customer : customers) {
            entries.add(customer.toString());
        }
        MenuDialogs.showList("Clientes registrados", MenuDialogs.formatEntries(entries));
    }

    private void listSellers() {
        List<Seller> sellers = personService.listSellers();
        if (sellers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Aún no hay vendedores registrados.",
                    "GameZone Unicesar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<String> entries = new ArrayList<>();
        for (Seller seller : sellers) {
            entries.add(seller.toString());
        }
        MenuDialogs.showList("Vendedores registrados", MenuDialogs.formatEntries(entries));
    }
}
