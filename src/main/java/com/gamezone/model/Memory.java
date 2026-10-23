package com.gamezone.model;

/**
 * Represents a memory accessory, characterized by its storage capacity
 * and memory type.
 */
public class Memory extends Accessory {

    private int capacityInGigabytes;
    private String memoryType;

    public Memory(String id, String title, double price, int stock,
                  int capacityInGigabytes, String memoryType) {
        super(id, title, price, stock);
        this.capacityInGigabytes = capacityInGigabytes;
        this.memoryType = memoryType;
    }

    /**
     * Returns the storage capacity of this memory in gigabytes.
     *
     * @return the capacity in gigabytes
     */
    public int getCapacityInGigabytes() { return capacityInGigabytes; }

    /**
     * Sets the storage capacity of this memory in gigabytes.
     *
     * @param capacityInGigabytes the capacity to set
     */
    public void setCapacityInGigabytes(int capacityInGigabytes) {
        this.capacityInGigabytes = capacityInGigabytes;
    }

    /**
     * Returns the type of this memory.
     *
     * @return the memory type (e.g. SD, microSD, internal card)
     */
    public String getMemoryType() { return memoryType; }

    /**
     * Sets the type of this memory.
     *
     * @param memoryType the memory type to set
     */
    public void setMemoryType(String memoryType) {
        this.memoryType = memoryType;
    }

    /**
     * Returns a full description integrating this memory's
     * specific characteristics.
     *
     * @return the full description of the memory
     */
    @Override
    public String getFullDescription() {
        return getTitle() + " - Capacity: " + capacityInGigabytes + "GB, Type: " + memoryType
                + ", Price: " + getPrice() + ", Stock: " + getStock();
    }
}