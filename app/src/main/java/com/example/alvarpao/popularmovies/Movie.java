package com.example.alvarpao.popularmovies;

/**
 * Created by alvarpao on 10/19/2015.
 * Object that contains the info for each movie queried
 */

public class Movie {

    private long id;
    private String imageURL;
    private String originalTitle;
    private String plotSynopsis;
    private double userRating;
    private String releaseYear;

    public Movie(long id, String imageURL, String originalTitle, String plotSynopsis, double userRating,
                 String releaseYear)
    {
        this.id = id;
        this.imageURL = imageURL;
        this.originalTitle = originalTitle;
        this.plotSynopsis = plotSynopsis;
        this.userRating = userRating;
        this.releaseYear = releaseYear;
    }

    public long getId(){ return id; }

    public String getImageURL() { return imageURL; }

    public String getOriginalTitle(){ return originalTitle; }

    public String getPlotSynopsis(){ return plotSynopsis; }

    public double getUserRating(){ return userRating; }

    public String getReleaseYear(){ return releaseYear; }

}
