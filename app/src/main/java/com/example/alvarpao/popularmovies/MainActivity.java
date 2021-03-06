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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements MovieGridFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String MOVIE_DETAILS_FRAGMENT_TAG = "MOVIE_DETAILS_FRAG_TAG";
    private boolean mTwoPaneLayout;
    private String mSortOption;
    MovieDetailFragment mDetailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Retrieving the user's preferred sort option as save it as a member variable
        mSortOption = getPreferredSortOption();

        // The movie grid fragment is static now. The only dynamic fragment is the movie details
        // fragment, this to accommodate a two-pane layout in sw720dp and larger
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.movie_details_container) != null) {
            // If this view is present it means that the app is displaying the layout for large
            // screens (sw720dp). So the activity should be displayed in two-pane mode.
            // Replace the movie details fragment accordingly when in two-pane mode.

            mTwoPaneLayout = true;
            if (savedInstanceState == null) {

                mDetailsFragment = new MovieDetailFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_details_container,
                                mDetailsFragment, MOVIE_DETAILS_FRAGMENT_TAG)
                        .commit();
            }
        }

        // Activity must be in one-pane layout (handsets or tablets 600dp or smaller)
        else
            mTwoPaneLayout = false;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            return true;

        return super.onOptionsItemSelected(item);
    }

    private String getPreferredSortOption()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        return preferences.getString(getString(R.string.sort_preference_key),
                getString(R.string.sort_preference_default));
    }

    public boolean inTwoPaneLayout()
    {
        return mTwoPaneLayout;
    }


    // The MainActivity now implements a Callback for the MovieGridFragment so this fragment
    // can notify the activity when a movie was selected
    @Override
    public void onItemSelected(Movie movie)
    {
        if (mTwoPaneLayout)
        {
            // In two-pane mode, show the movie details by replacing the MovieDetailFragment
            // using a fragment transaction.

            Bundle args = new Bundle();
            // Pass the movie object as argument for the fragment
            args.putParcelable(MovieDetailFragment.MOVIE_DETAILS, movie);

            mDetailsFragment = new MovieDetailFragment();
            mDetailsFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_details_container, mDetailsFragment,
                            MOVIE_DETAILS_FRAGMENT_TAG)
                    .commit();
        }

        else
        {
            // When in one-pane layout just start a new MovieDetailActivity and pass the selected
            // movie object to display it in a different screen
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieDetailFragment.MOVIE_DETAILS, movie);
            startActivity(intent);
        }
    }

    @Override
    public void clearDetailsFragment() {

        // Only called when in two-pane layout, make sure the MovieDetailFragment is empty
        mDetailsFragment = new MovieDetailFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_details_container, mDetailsFragment,
                        MOVIE_DETAILS_FRAGMENT_TAG)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // This method gets called from the MovieDetailsRecylerAdapter class to deselect a
        // trailer after the youtube video has been launched (called when in two-pane layout since
        // Main Activity contains the details fragment in this case)
        if(requestCode == MovieDetailsRecyclerAdapter.TRAILER_SELECTED)
           mDetailsFragment.deselectTrailerItem();
    }

    public void saveFavoriteMovieDetails(Movie movie) {

        // Save the current movie's details into the local database to keep the user's favorite
        // list (called from MovieDetailsRecyclerAdapter when in two-pane layout)
        SaveFavoriteMovieDetailsTask saveFavoriteMovieDetailsTask =
                new SaveFavoriteMovieDetailsTask(this);
        saveFavoriteMovieDetailsTask.execute(movie);
    }

    public void deleteMovieFromFavorites(Movie movie){

        // Delete the current movie from the user's favorite list (called from
        // QueryIfFavoriteMovieTask class when in two-pane layout)
        DeleteFavoriteMovieTask deleteFavoriteMovieTask = new DeleteFavoriteMovieTask(this);
        deleteFavoriteMovieTask.execute(movie);
    }
}
