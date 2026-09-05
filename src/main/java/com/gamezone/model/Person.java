package com.gamezone.model;

/**
 * Represents a generic person interacting with the store.
 * This class is abstract because a person must always have
 * a specific role (Customer or Seller); a generic person
 * cannot be instantiated on its own.
 */
public abstract class Person {

    private String name;
    private String id;
    private String phone;

    public Person(String name, String id, String phone) {
        this.name = name;
        this.id = id;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }
}