package com.example.alvarpao.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageButton;

/**
 * This class represents the movie details info for a selected movie from the movie grid
 */

public class MovieDetailActivity extends ActionBarActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_details_container, new MovieDetailFragment())
                    .commit();
        }
    }

    public void deleteMovieFromFavorites(Movie movie){

        DeleteFavoriteMovieTask deleteFavoriteMovieTask = new DeleteFavoriteMovieTask(this);
        deleteFavoriteMovieTask.execute(movie);
    }

    public void saveFavoriteMovieDetails(ImageButton btnFavorite, Movie movie) {

        // Save the current movie's details into the local database to keep the user's favorite
        // list
        SaveFavoriteMovieDetailsTask saveFavoriteMovieDetailsTask =
                new SaveFavoriteMovieDetailsTask(this, btnFavorite);
        saveFavoriteMovieDetailsTask.execute(movie);
    }
}
