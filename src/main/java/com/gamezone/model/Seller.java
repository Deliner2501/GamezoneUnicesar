package com.gamezone.model;

/**
 * Represents a seller (employee) who attends customers
 * and registers sales in the store.
 */
public class Seller extends Person {

    private String employeeCode;
    private String workShift;

    /**
     * Creates a new Seller with the given information.
     *
     * @param name         the seller's full name
     * @param id           the seller's identification number
     * @param phone        the seller's contact phone number
     * @param employeeCode the seller's unique employee code
     * @param workShift    the seller's assigned work shift
     */
    public Seller(String name, String id, String phone, String employeeCode, String workShift) {
        super(name, id, phone);
        this.employeeCode = employeeCode;
        this.workShift = workShift;
    }

    /**
     * Returns the seller's employee code.
     * @return the employee code
     */
    public String getEmployeeCode() {
        return employeeCode;
    }

    /**
     * Returns the seller's assigned work shift.
     * @return the work shift
     */
    public String getWorkShift() {
        return workShift;
    }

    /**
     * Returns a readable representation of this seller, including employee info.
     * @return a formatted string with the seller's data
     */
    @Override
    public String toString() {
        return super.toString() + ", Employee Code: " + employeeCode + ", Shift: " + workShift;
    }
}