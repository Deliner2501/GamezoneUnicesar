package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonDAO;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Handles the business rules for registering and listing
 * customers and sellers.
 */
public class PersonService {

    // El service usa el DAO para guardar/cargar, nunca accede a archivos directamente
    private PersonDAO personDAO;

    // Mantenemos las listas en memoria mientras el programa está corriendo,
    // así no hay que leer el archivo cada vez que alguien pide la lista de clientes
    private List<Customer> customers;
    private List<Seller> sellers;

    /**
     * Creates the service and loads the existing customers and sellers
     * from persistence, so previously registered data is available
     * right after the application starts.
     *
     * @throws IOException if the stored data cannot be read
     */
    public PersonService() throws IOException {
        // Creamos nuestra propia instancia del DAO para usarla internamente
        this.personDAO = new PersonDAO();

        // Apenas se crea el service, cargamos todo lo que ya estaba guardado en los archivos.
        // Si es la primera vez que se ejecuta el programa, esto devuelve listas vacías (no falla).
        this.customers = personDAO.loadCustomers();
        this.sellers = personDAO.loadSellers();
    }

    /**
     * Registers a new customer, validating that the id is not already used,
     * and persists the updated list immediately.
     *
     * @param name  the customer's full name
     * @param id    the customer's identification number
     * @param phone the customer's contact phone number
     * @param email the customer's email address
     * @throws IllegalArgumentException if a customer with the same id already exists
     * @throws IOException              if the data cannot be saved
     */
    public void registerCustomer(String name, String id, String phone, String email) throws IOException {
        // Antes de registrar, recorremos todos los clientes actuales
        // para asegurarnos de que no exista ya alguien con ese mismo id.
        // Esto es una regla de negocio: no puede haber dos clientes con la misma identificación.
        for (Customer customer : customers) {
            if (customer.getId().equals(id)) {
                // Si encontramos uno repetido, lanzamos una excepción y detenemos el registro
                throw new IllegalArgumentException("Ya existe un cliente con el id: " + id);
            }
        }

        // Si pasó la validación, creamos el nuevo cliente
        Customer newCustomer = new Customer(name, id, phone, email);

        // Lo agregamos a la lista en memoria
        customers.add(newCustomer);

        // Y de una vez lo guardamos en el archivo, para no perder el dato
        // si el programa se cierra inesperadamente después de este punto
        personDAO.saveCustomers(customers);
    }

    /**
     * Returns the list of all registered customers.
     * @return an unmodifiable view of the customers list
     */
    public List<Customer> listCustomers() {
        // Collections.unmodifiableList() devuelve una "copia protegida" de la lista.
        // Esto evita que quien reciba la lista pueda modificarla por fuera del service
        // (por ejemplo, agregando o borrando clientes sin pasar por registerCustomer()).
        return Collections.unmodifiableList(customers);
    }

    /**
     * Returns the list of all registered sellers.
     * @return an unmodifiable view of the sellers list
     */
    public List<Seller> listSellers() {
        // Misma idea que arriba, pero para vendedores.
        // Nota: no hay un método "registerSeller" porque el taller dice que los vendedores
        // ya vienen precargados desde el archivo, no se registran desde el menú.
        return Collections.unmodifiableList(sellers);
    }
}