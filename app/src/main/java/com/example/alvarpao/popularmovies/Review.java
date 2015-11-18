package com.example.alvarpao.popularmovies;

/**
 * Created by alvarpao on 11/18/2015.
 */
public class Review {

    private String author;
    private String content;

    public Review(String author, String content){

        this.author = author;
        this.content = content;
    }

    public String getAuthor(){ return author; }
    public String getContent(){ return content; }

}
