package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Customer and Seller data
 * to and from text files, so information persists between executions.
 */
public class PersonDAO {

    // Rutas donde se van a guardar los archivos de texto.
    // Son "constantes" (static final) porque nunca cambian durante la ejecución.
    private static final String CUSTOMERS_FILE = "data/customers.txt";
    private static final String SELLERS_FILE = "data/sellers.txt";

    // Este es el caracter que usamos para separar cada dato dentro de una línea.
    // Ejemplo de línea real: "C001;Juan Perez;3001234567;juan@email.com"
    private static final String SEPARATOR = ";";

    /**
     * Saves the complete list of customers to the customers file,
     * overwriting any previous content.
     *
     * @param customers the list of customers to save
     * @throws IOException if the file cannot be written
     */
    public void saveCustomers(List<Customer> customers) throws IOException {
        // Creamos un objeto File que representa la ruta del archivo (a\u00fan no existe en disco)
        File file = new File(CUSTOMERS_FILE);

        // getParentFile() nos da la carpeta "data". mkdirs() la crea si no existe.
        // Sin esta línea, si la carpeta "data" no existe, el programa fallaría al escribir.
        file.getParentFile().mkdirs();

        // "try-with-resources": abre el escritor y lo cierra automáticamente al terminar,
        // incluso si ocurre un error en el medio. Evita tener que hacer writer.close() manual.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            // Recorremos cada cliente de la lista, uno por uno
            for (Customer customer : customers) {

                // Armamos el texto de la línea uniendo cada dato con el separador ";"
                String line = customer.getId() + SEPARATOR
                        + customer.getName() + SEPARATOR
                        + customer.getPhone() + SEPARATOR
                        + customer.getEmail();

                // Escribimos esa línea en el archivo
                writer.write(line);

                // Saltamos a la siguiente línea, si no, todos los clientes quedarían pegados
                writer.newLine();
            }
        }
        // Al salir del "try", el archivo se cierra y guarda automáticamente
    }

    /**
     * Loads all customers stored in the customers file.
     * If the file does not exist yet, an empty list is returned.
     *
     * @return the list of customers found in the file
     * @throws IOException if the file exists but cannot be read
     */
    public List<Customer> loadCustomers() throws IOException {
        // Creamos la lista vacía donde vamos a ir agregando los clientes que encontremos
        List<Customer> customers = new ArrayList<>();
        File file = new File(CUSTOMERS_FILE);

        // Si el archivo no existe (por ejemplo, la primerísima vez que se abre el programa),
        // no hay nada que leer, así que devolvemos la lista vacía sin error.
        if (!file.exists()) {
            return customers;
        }

        // Abrimos el archivo para leerlo línea por línea
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            // readLine() devuelve null cuando ya no hay más líneas que leer, ahí termina el ciclo
            while ((line = reader.readLine()) != null) {

                // split(";") separa el texto de la línea en un arreglo, cortando por cada ";"
                // Ejemplo: "C001;Juan Perez;3001234567;juan@email.com" se vuelve:
                // parts[0] = "C001", parts[1] = "Juan Perez", parts[2] = "3001234567", parts[3] = "juan@email.com"
                String[] parts = line.split(SEPARATOR);

                // Reconstruimos el objeto Customer con esos datos y lo agregamos a la lista
                customers.add(new Customer(parts[1], parts[0], parts[2], parts[3]));
            }
        }
        return customers;
    }

    /**
     * Saves the complete list of sellers to the sellers file,
     * overwriting any previous content.
     *
     * @param sellers the list of sellers to save
     * @throws IOException if the file cannot be written
     */
    public void saveSellers(List<Seller> sellers) throws IOException {
        // Misma lógica que saveCustomers, pero para vendedores
        File file = new File(SELLERS_FILE);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Seller seller : sellers) {
                // Los vendedores tienen 2 datos extra: employeeCode y workShift
                String line = seller.getId() + SEPARATOR
                        + seller.getName() + SEPARATOR
                        + seller.getPhone() + SEPARATOR
                        + seller.getEmployeeCode() + SEPARATOR
                        + seller.getWorkShift();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Loads all sellers stored in the sellers file.
     * If the file does not exist yet, an empty list is returned.
     *
     * @return the list of sellers found in the file
     * @throws IOException if the file exists but cannot be read
     */
    public List<Seller> loadSellers() throws IOException {
        // Misma lógica que loadCustomers, pero para vendedores
        List<Seller> sellers = new ArrayList<>();
        File file = new File(SELLERS_FILE);

        if (!file.exists()) {
            return sellers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Aquí el arreglo tiene 5 posiciones en vez de 4, porque Seller tiene más datos
                String[] parts = line.split(SEPARATOR);
                sellers.add(new Seller(parts[1], parts[0], parts[2], parts[3], parts[4]));
            }
        }
        return sellers;
    }
}