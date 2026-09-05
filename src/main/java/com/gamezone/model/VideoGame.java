package com.gamezone.model;

/**
 * Represents a video game product, characterized by platform,
 * genre and age rating.
 */
public class VideoGame extends Product {

    private String platform;
    private String genre;
    private String ageRating;

    public VideoGame(String id, String title, double price, int stock,
                      String platform, String genre, String ageRating) {
        super(id, title, price, stock);
        this.platform = platform;
        this.genre = genre;
        this.ageRating = ageRating;
    }

    /**
     * Returns a full description integrating this video game's
     * specific characteristics.
     *
     * @return the full description of the video game
     */
    @Override
    public String getFullDescription() {
        return getTitle() + " - Platform: " + platform + ", Genre: " + genre
                + ", Age rating: " + ageRating + ", Price: " + getPrice()
                + ", Stock: " + getStock();
    }
}