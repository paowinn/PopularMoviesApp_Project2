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
