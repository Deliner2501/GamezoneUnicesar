package com.gamezone.model;

/**
 * Represents a generic person interacting with the store.
 * This class is abstract because a person must always have
 * a specific role (Customer or Seller); a generic person
 * cannot be instantiated on its own.
 */
public abstract class Person {

    // Atributos comunes a toda persona (cliente o vendedor)
    private String name;
    private String id;
    private String phone;

    /**
     * Creates a new Person with the given basic information.
     *
     * @param name  the person's full name
     * @param id    the person's identification number
     * @param phone the person's contact phone number
     * @throws IllegalArgumentException if any field is null or empty
     */
    public Person(String name, String id, String phone) {
        // Validamos que ningún dato básico venga vacío antes de crear la persona
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id no puede estar vacío");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }
        this.name = name;
        this.id = id;
        this.phone = phone;
    }

    /**
     * Returns the person's name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the person's identification number.
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the person's contact phone number.
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns a readable representation of this person's basic information.
     * @return a formatted string with the person's data
     */
    @Override
    public String toString() {
        return "Name: " + name + ", ID: " + id + ", Phone: " + phone;
    }
}