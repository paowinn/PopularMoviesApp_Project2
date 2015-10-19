package com.example.alvarpao.popularmovies;

/**
 * Created by alvarpao on 10/19/2015.
 * Object that contains an id reference to a movie poster image
 */

public class MoviePoster {

    // Drawable reference id
    private int image;

    public MoviePoster(int image) { this.image = image; }

    public int getImage() {
        return image;
    }
}
