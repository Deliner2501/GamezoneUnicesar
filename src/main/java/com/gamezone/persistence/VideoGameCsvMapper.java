package com.gamezone.persistence;

import com.gamezone.model.Product;
import com.gamezone.model.ProductCategory;
import com.gamezone.model.VideoGame;

/**
 * CSV mapper for video games.
 */
public class VideoGameCsvMapper implements ProductCsvMapper {

    @Override
    public ProductCategory getCategory() {
        return ProductCategory.VIDEOGAME;
    }

    @Override
    public String getTypeCode() {
        return "VIDEOGAME";
    }

    @Override
    public String toCsvLine(Product product) {
        VideoGame vg = (VideoGame) product;
        return "VIDEOGAME," + vg.getId() + "," + vg.getTitle() + ","
                + vg.getPrice() + "," + vg.getStock() + ","
                + vg.getPlatform() + "," + vg.getGenre() + "," + vg.getAgeRating();
    }

    @Override
    public Product create(String id, String title, double price, int stock, String[] parts) {
        return new VideoGame(id, title, price, stock, parts[5], parts[6], parts[7]);
    }
}