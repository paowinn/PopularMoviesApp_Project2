package com.example.alvarpao.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by alvarpao on 11/18/2015.
 */
public class Trailer implements Parcelable{

    private String name;
    private String source;

    public Trailer(String name, String source) {
        this.name = name;
        this.source = source;
    }

    public Trailer(Parcel in){
        name = in.readString();
        source = in.readString();
    }

    public void writeToParcel(Parcel parcel, int content){
        parcel.writeString(name);
        parcel.writeString(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel parcel) {
            return new Trailer(parcel);
        }

        @Override
        public Trailer[] newArray(int i) {
            return new Trailer[i];
        }

    };

    public String getName() { return name; }
    public String getSource() { return source; }

}
