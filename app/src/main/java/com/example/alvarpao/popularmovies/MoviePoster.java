package com.example.alvarpao.popularmovies;

/**
 * Created by alvarpao on 10/19/2015.
 * Object that contains an id reference to a movie poster image
 */

public class MoviePoster {

    // Drawable reference id
    private String imageURL;

    public MoviePoster(String imageURL) { this.imageURL = imageURL; }

    public String getImageURL() {
        return imageURL;
    }
}
