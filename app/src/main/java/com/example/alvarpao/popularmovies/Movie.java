/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
