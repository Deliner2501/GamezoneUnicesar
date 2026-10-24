package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.model.ProductCategory;
import com.gamezone.model.VideoGame;

/**
 * Factory of video games. Specific fields: platform, genre, age rating.
 */
public class VideoGameFactory implements ProductFactory {

    @Override
    public ProductCategory getCategory() {
        return ProductCategory.VIDEOGAME;
    }

    @Override
    public Product create(String id, String title, double price, int stock, String... specificFields) {
        return new VideoGame(id, title, price, stock,
                specificFields[0], specificFields[1], specificFields[2]);
    }
}