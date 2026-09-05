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

    // Los clientes los manejamos completamente nosotros: formato .txt,
    // separado por ; y SIN fila de encabezado (porque nadie más lee este archivo)
    private static final String CUSTOMERS_FILE = "data/customers.txt";
    private static final String CUSTOMERS_SEPARATOR = ";";

    // Los vendedores vienen precargados por el equipo en un archivo .csv
    // que SÍ tiene fila de encabezado y usa comas, así que debemos leerlo
    // exactamente en ese formato para que coincida con lo que ya subieron
    private static final String SELLERS_FILE = "data/sellers.csv";
    private static final String SELLERS_SEPARATOR = ",";

    /**
     * Saves the complete list of customers to the customers file,
     * overwriting any previous content.
     */
    public void saveCustomers(List<Customer> customers) throws IOException {
        File file = new File(CUSTOMERS_FILE);
        // Creamos la carpeta "data" si todavía no existe
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Customer customer : customers) {
                // Armamos la línea: id;nombre;telefono;email
                String line = customer.getId() + CUSTOMERS_SEPARATOR
                        + customer.getName() + CUSTOMERS_SEPARATOR
                        + customer.getPhone() + CUSTOMERS_SEPARATOR
                        + customer.getEmail();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Loads all customers stored in the customers file.
     */
    public List<Customer> loadCustomers() throws IOException {
        List<Customer> customers = new ArrayList<>();
        File file = new File(CUSTOMERS_FILE);

        // Si el archivo aún no existe (primera vez que corre el programa),
        // no hay nada que leer, devolvemos lista vacía sin error
        if (!file.exists()) {
            return customers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Como este archivo lo creamos nosotros mismos, no tiene encabezado,
                // así que cada línea que leamos es directamente un cliente real
                String[] parts = line.split(CUSTOMERS_SEPARATOR);
                customers.add(new Customer(parts[1], parts[0], parts[2], parts[3]));
            }
        }
        return customers;
    }

    /**
     * Saves the complete list of sellers to the sellers file,
     * overwriting any previous content.
     */
    public void saveSellers(List<Seller> sellers) throws IOException {
        File file = new File(SELLERS_FILE);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Escribimos primero la línea de encabezado, para que el archivo
            // mantenga siempre el mismo formato que el que subió el equipo
            writer.write("id,name,phone,employeeCode,workShift");
            writer.newLine();

            for (Seller seller : sellers) {
                // Armamos la línea: id,nombre,telefono,codigoEmpleado,turno
                String line = seller.getId() + SELLERS_SEPARATOR
                        + seller.getName() + SELLERS_SEPARATOR
                        + seller.getPhone() + SELLERS_SEPARATOR
                        + seller.getEmployeeCode() + SELLERS_SEPARATOR
                        + seller.getWorkShift();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Loads all sellers stored in the sellers file.
     */
    public List<Seller> loadSellers() throws IOException {
        List<Seller> sellers = new ArrayList<>();
        File file = new File(SELLERS_FILE);

        if (!file.exists()) {
            return sellers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            // Esta variable nos ayuda a recordar si vamos en la primera línea del archivo.
            // La usamos como una especie de "bandera": empieza en true, y la apagamos
            // (false) apenas pasamos por la primera vuelta del ciclo.
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {

                // La primera línea del CSV es el encabezado: "id,name,phone,..."
                // Eso NO es un vendedor real, es solo el título de las columnas.
                // Si isFirstLine todavía es true, la saltamos con "continue"
                // (esto significa "no proceses esta línea, ve directo a la siguiente vuelta del ciclo")
                if (isFirstLine) {
                    isFirstLine = false; // ya pasamos la primera línea, apagamos la bandera
                    continue;
                }

                // A partir de aquí, cada línea sí es un vendedor real
                String[] parts = line.split(SELLERS_SEPARATOR);
                sellers.add(new Seller(parts[1], parts[0], parts[2], parts[3], parts[4]));
            }
        }
        return sellers;
    }
}