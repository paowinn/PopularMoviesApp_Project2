package com.example.alvarpao.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by alvarpao on 10/19/2015.
 * Object that contains the info for each movie queried
 */

public class Movie implements Parcelable {

    private long id;
    private String imageURL;
    private String originalTitle;
    private String plotSynopsis;
    private double userRating;
    private String releaseYear;
    private int runtime;
    ArrayList<Trailer> trailers;
    ArrayList<Review> reviews;

    public Movie(long id, String imageURL, String originalTitle, String plotSynopsis, double userRating,
                 String releaseYear) {

        this.id = id;
        this.imageURL = imageURL;
        this.originalTitle = originalTitle;
        this.plotSynopsis = plotSynopsis;
        this.userRating = userRating;
        this.releaseYear = releaseYear;
        runtime = 0;
        trailers = new ArrayList<Trailer>();
        reviews = new ArrayList<Review>();
    }

    public Movie(Parcel in){
        id = in.readLong();
        imageURL = in.readString();
        originalTitle = in.readString();
        plotSynopsis = in.readString();
        userRating = in.readDouble();
        releaseYear = in.readString();
        runtime = in.readInt();
        trailers = (ArrayList<Trailer>) in.readArrayList(Trailer.class.getClassLoader());
        reviews = (ArrayList<Review>) in.readArrayList(Review.class.getClassLoader());
    }

    public void writeToParcel(Parcel parcel, int content){
        parcel.writeLong(id);
        parcel.writeString(imageURL);
        parcel.writeString(originalTitle);
        parcel.writeString(plotSynopsis);
        parcel.writeDouble(userRating);
        parcel.writeString(releaseYear);
        parcel.writeInt(runtime);
        parcel.writeList(trailers);
        parcel.writeList(reviews);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }

    };

    public long getId(){ return id; }

    public String getImageURL() { return imageURL; }

    public String getOriginalTitle(){ return originalTitle; }

    public String getPlotSynopsis(){ return plotSynopsis; }

    public double getUserRating(){ return userRating; }

    public String getReleaseYear(){ return releaseYear; }

    public void setRuntime(int runtime){ this.runtime = runtime; }

    public int getRuntime(){ return runtime; }

}
