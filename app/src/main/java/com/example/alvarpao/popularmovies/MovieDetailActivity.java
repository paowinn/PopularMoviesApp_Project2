package com.example.alvarpao.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * This class represents the movie details info for a selected movie from the movie grid
 */

public class MovieDetailActivity extends ActionBarActivity{

    private MovieDetailFragment mMovieDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);

        if(savedInstanceState == null){
            mMovieDetailFragment = new MovieDetailFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_details_container, mMovieDetailFragment)
                    .commit();
        }

        else{

            mMovieDetailFragment =  new MovieDetailFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_details_container,
                            mMovieDetailFragment)
                    .commit();
        }
    }

    public void deleteMovieFromFavorites(Movie movie){

        DeleteFavoriteMovieTask deleteFavoriteMovieTask = new DeleteFavoriteMovieTask(this);
        deleteFavoriteMovieTask.execute(movie);
    }

    public void saveFavoriteMovieDetails(Movie movie) {

        // Save the current movie's details into the local database to keep the user's favorite
        // list
        SaveFavoriteMovieDetailsTask saveFavoriteMovieDetailsTask =
                new SaveFavoriteMovieDetailsTask(this);
        saveFavoriteMovieDetailsTask.execute(movie);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // This method gets called from the MovieDetailsRecylerAdapter class to deselect a
        // trailer after the youtube video has been launched

        if (requestCode == MovieDetailsRecyclerAdapter.TRAILER_SELECTED) {
            if (mMovieDetailFragment != null)
                mMovieDetailFragment.deselectTrailerItem();
        }
    }
}
