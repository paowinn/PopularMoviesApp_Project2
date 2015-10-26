package com.example.alvarpao.popularmovies;

/**
 * Created by alvarpao on 10/19/2015.
 * Object that contains the info for each movie queried
 */

public class Movie {

    private String imageURL;
    private String originalTitle;
    private String plotSynopsis;
    private double userRating;
    private String releaseDate;

    public Movie(String imageURL, String originalTitle, String plotSynopsis, double userRating,
                 String releaseDate)
    {
        this.imageURL = imageURL;
        this.originalTitle = originalTitle;
        this.plotSynopsis = plotSynopsis;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }

    public String getImageURL() { return imageURL; }

    public String getOriginalTitle(){ return originalTitle; }

    public String getPlotSynopsis(){ return plotSynopsis; }

    public double getUserRating(){ return userRating; }

    public String getReleaseDate(){ return releaseDate; }

}
