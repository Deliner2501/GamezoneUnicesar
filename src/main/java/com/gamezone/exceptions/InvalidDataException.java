package com.gamezone.exceptions;

/**
 * Thrown when an input value does not meet the expected format,
 * range or type, such as a negative price, a negative quantity,
 * or an empty text where content is required.
 */
public class InvalidDataException extends GameZoneException {

    private static final String ERROR_CODE = "INVALID_DATA";

    private final String fieldName;

    /**
     * Creates the exception building a descriptive Spanish message
     * from the name of the invalid field and a description of the problem.
     *
     * @param fieldName   the name of the field that holds the invalid value
     * @param description what is wrong with the value
     */
    public InvalidDataException(String fieldName, String description) {
        super("El campo '" + fieldName + "' no es válido: " + description, ERROR_CODE);
        this.fieldName = fieldName;
    }

    /**
     * Returns the name of the field that holds the invalid value.
     *
     * @return the invalid field name
     */
    public String getFieldName() {
        return fieldName;
    }
}