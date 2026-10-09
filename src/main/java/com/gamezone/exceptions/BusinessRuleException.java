package com.gamezone.exceptions;

/**
 * Thrown when an operation violates a business rule of the store,
 * such as insufficient stock, a duplicated customer, or a sale
 * without products.
 */
public class BusinessRuleException extends GameZoneException {

    private static final String ERROR_CODE = "BUSINESS_RULE_VIOLATION";

    /**
     * Creates the exception with a message describing the violated rule.
     *
     * @param message the description of the business rule that was violated
     */
    public BusinessRuleException(String message) {
        super(message, ERROR_CODE);
    }
}