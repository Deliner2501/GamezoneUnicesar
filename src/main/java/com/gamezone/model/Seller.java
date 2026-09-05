package com.gamezone.model;

/**
 * Represents a seller (employee) who attends customers
 * and registers sales in the store.
 */
public class Seller extends Person {

    private String employeeCode;
    private String workShift;

    public Seller(String name, String id, String phone, String employeeCode, String workShift) {
        super(name, id, phone);
        this.employeeCode = employeeCode;
        this.workShift = workShift;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getWorkShift() {
        return workShift;
    }
}