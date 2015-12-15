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
