package com.example.alvarpao.popularmovies;

/**
 * Created by alvarpao on 11/18/2015.
 */
public class Trailer {

    private String name;
    private String source;

    public Trailer(String name, String source) {
        this.name = name;
        this.source = source;
    }

    public String getName() { return name; }
    public String getSource() { return source; }

}
