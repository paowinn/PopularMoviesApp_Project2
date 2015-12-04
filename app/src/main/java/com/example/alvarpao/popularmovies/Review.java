package com.example.alvarpao.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by alvarpao on 11/18/2015.
 */
public class Review implements Parcelable{

    private String author;
    private String content;

    public Review(String author, String content){

        this.author = author;
        this.content = content;
    }

    public Review(Parcel in){
        author = in.readString();
        content = in.readString();
    }

    public void writeToParcel(Parcel parcel, int content){
        parcel.writeString(author);
        parcel.writeString(this.content);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel parcel) {
            return new Review(parcel);
        }

        @Override
        public Review[] newArray(int i) {
            return new Review[i];
        }

    };

    public String getAuthor(){ return author; }
    public String getContent(){ return content; }

}
