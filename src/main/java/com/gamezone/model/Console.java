package com.gamezone.model;

/**
 * Represents a console product, characterized by brand,
 * model and generation.
 */
public class Console extends Product {

    private String brand;
    private String model;
    private String generation;

    public Console(String id, String title, double price, int stock,
                    String brand, String model, String generation) {
        super(id, title, price, stock);
        this.brand = brand;
        this.model = model;
        this.generation = generation;
    }

       /**
     * Returns the brand of this console.
     *
     * @return the brand
     */
    public String getBrand() { return brand; }

    /**
     * Returns the model of this console.
     *
     * @return the model
     */
    public String getModel() { return model; }

    /**
     * Returns the generation of this console.
     *
     * @return the generation
     */
    public String getGeneration() { return generation; }
    /**
     * Returns a full description integrating this console's
     * specific characteristics.
     *
     * @return the full description of the console
     */
    @Override
    public String getFullDescription() {
        return getTitle() + " - Brand: " + brand + ", Model: " + model
                + ", Generation: " + generation + ", Price: " + getPrice()
                + ", Stock: " + getStock();
    }
}