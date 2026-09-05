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
     * Returns the platform this video game was developed for.
     *
     * @return the platform
     */
    public String getPlatform() { return platform; }

    /**
     * Returns the genre of this video game.
     *
     * @return the genre
     */
    public String getGenre() { return genre; }

    /**
     * Returns the recommended age rating for this video game.
     *
     * @return the age rating
     */
    public String getAgeRating() { return ageRating; }
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