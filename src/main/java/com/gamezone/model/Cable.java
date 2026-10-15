package com.gamezone.model;

/**
 * Represents a cable accessory, characterized by its length and connector type.
 */
public class Cable extends Accessory {

    private double lengthInMeters;
    private String connectorType;

    public Cable(String id, String title, double price, int stock,
                 double lengthInMeters, String connectorType) {
        super(id, title, price, stock);
        this.lengthInMeters = lengthInMeters;
        this.connectorType = connectorType;
    }

    /**
     * Returns the length of this cable in meters.
     *
     * @return the length in meters
     */
    public double getLengthInMeters() { return lengthInMeters; }

    /**
     * Sets the length of this cable in meters.
     *
     * @param lengthInMeters the length to set
     */
    public void setLengthInMeters(double lengthInMeters) {
        this.lengthInMeters = lengthInMeters;
    }

    /**
     * Returns the connector type of this cable.
     *
     * @return the connector type (e.g. HDMI, USB, optical)
     */
    public String getConnectorType() { return connectorType; }

    /**
     * Sets the connector type of this cable.
     *
     * @param connectorType the connector type to set
     */
    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    /**
     * Returns a full description integrating this cable's
     * specific characteristics.
     *
     * @return the full description of the cable
     */
    @Override
    public String getFullDescription() {
        return getTitle() + " - Length: " + lengthInMeters + "m, Connector: " + connectorType
                + ", Price: " + getPrice() + ", Stock: " + getStock();
    }
        /**
     * Returns the type code of this accessory.
     *
     * @return "CABLE"
     */
    @Override
    public String getTypeCode() {
        return "CABLE";
    }

    /**
     * Returns the CSV line that represents this cable.
     *
     * @return the line in the format CABLE,id,title,price,stock,length,connector,consoles
     */
    @Override
    public String toCsvLine() {
        return "CABLE," + getId() + "," + getTitle() + ","
                + getPrice() + "," + getStock() + ","
                + lengthInMeters + "," + connectorType + "," + compatibleConsolesAsCsv();
    }
}