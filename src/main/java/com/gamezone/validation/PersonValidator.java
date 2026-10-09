package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Person;
import java.util.List;

/**
 * Groups the validation rules related to people (customers and
 * sellers). Services call these methods before executing a business
 * operation; each method throws the custom exception that matches the
 * kind of problem found.
 */
public class PersonValidator {

    private PersonValidator() {
        // Utility class: it must not be instantiated.
    }

    /**
     * Validates the basic data of a person: the mandatory fields must
     * not be null or empty, and the email must have a reasonable format
     * (it contains an at sign followed by a dot).
     *
     * @param id    the person identifier
     * @param name  the person name
     * @param email the person email
     * @param phone the person phone number
     * @throws InvalidDataException if any of the values is not valid
     */
    public static void validatePersonData(String id, String name, String email, String phone) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("id", "no puede estar vacío");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("nombre", "no puede estar vacío");
        }
        if (phone == null || phone.isBlank()) {
            throw new InvalidDataException("teléfono", "no puede estar vacío");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidDataException("correo electrónico", "no puede estar vacío");
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || email.lastIndexOf('.') < atIndex) {
            throw new InvalidDataException("correo electrónico",
                    "debe tener un formato válido, por ejemplo nombre@dominio.com");
        }
    }

    /**
     * Validates that a person was found.
     *
     * @param person   the person returned by a search (null if not found)
     * @param personId the identifier that was searched
     * @throws ResourceNotFoundException if the person is null
     */
    public static void validatePersonExists(Person person, String personId) {
        if (person == null) {
            throw new ResourceNotFoundException("persona", personId);
        }
    }

    /**
     * Validates that no other person already uses the given identifier.
     *
     * @param existing the people already registered
     * @param newId    the identifier of the person being registered
     * @throws BusinessRuleException if the identifier is already in use
     */
    public static void validateUniquePerson(List<Person> existing, String newId) {
        for (Person person : existing) {
            if (person.getId().equals(newId)) {
                throw new BusinessRuleException(
                        "Ya existe una persona registrada con el identificador " + newId);
            }
        }
    }
}