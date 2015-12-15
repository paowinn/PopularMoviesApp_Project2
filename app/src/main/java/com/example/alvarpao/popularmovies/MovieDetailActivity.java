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
