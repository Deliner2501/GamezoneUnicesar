package com.gamezone.model;

/**
 * Represents a controller accessory, characterized by its connection type.
 */
public class Controller extends Accessory {

    private String connectionType;

    public Controller(String id, String title, double price, int stock,
                       String connectionType) {
        super(id, title, price, stock);
        this.connectionType = connectionType;
    }

    /**
     * Returns the connection type of this controller.
     *
     * @return the connection type (e.g. wireless or wired)
     */
    public String getConnectionType() { return connectionType; }

    /**
     * Sets the connection type of this controller.
     *
     * @param connectionType the connection type to set
     */
    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * Returns a full description integrating this controller's
     * specific characteristics.
     *
     * @return the full description of the controller
     */
    @Override
    public String getFullDescription() {
        return getTitle() + " - Connection: " + connectionType
                + ", Price: " + getPrice() + ", Stock: " + getStock();
    }
        /**
     * Returns the type code of this accessory.
     *
     * @return "CONTROLLER"
     */
    @Override
    public String getTypeCode() {
        return "CONTROLLER";
    }

    /**
     * Returns the CSV line that represents this controller.
     *
     * @return the line in the format CONTROLLER,id,title,price,stock,connectionType,consoles
     */
    @Override
    public String toCsvLine() {
        return "CONTROLLER," + getId() + "," + getTitle() + ","
                + getPrice() + "," + getStock() + ","
                + connectionType + "," + compatibleConsolesAsCsv();
    }
}