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
    public boolean supports(Product product) {
        return product instanceof VideoGame;
    }

    @Override
    public String toCsvLine(Product product) {
        if (!supports(product)) {
            throw new IllegalArgumentException("This mapper only handles video games");
        }
        VideoGame vg = (VideoGame) product;
        return "VIDEOGAME," + vg.getId() + "," + vg.getTitle() + ","
                + vg.getPrice() + "," + vg.getStock() + ","
                + vg.getPlatform() + "," + vg.getGenre() + "," + vg.getAgeRating();
    }

    @Override
    public Product parse(String[] parts) {
        return new VideoGame(parts[1], parts[2], Double.parseDouble(parts[3]),
                Integer.parseInt(parts[4]), parts[5], parts[6], parts[7]);
    }
}