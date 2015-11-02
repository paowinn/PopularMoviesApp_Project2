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
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_details_container,
                                new MovieDetailActivity.MovieDetailFragment(), MOVIE_DETAILS_FRAGMENT_TAG)
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

    public boolean getLayoutMode()
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
            //Bundle up the select movie's details and pass them as arguments for the fragment
            args.putLong(MovieGridFragment.EXTRA_ID, movie.getId());
                    args.putString(MovieGridFragment.EXTRA_IMAGE_URL, movie.getImageURL());
                    args.putString(MovieGridFragment.EXTRA_ORIGINAL_TITLE, movie.getOriginalTitle());
                    args.putString(MovieGridFragment.EXTRA_PLOT_SYNOPSIS, movie.getPlotSynopsis());
                    args.putDouble(MovieGridFragment.EXTRA_USER_RATING, movie.getUserRating());
                    args.putString(MovieGridFragment.EXTRA_RELEASE_YEAR, movie.getReleaseYear());

            MovieDetailActivity.MovieDetailFragment detailsFragment =
                    new MovieDetailActivity.MovieDetailFragment();
            detailsFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_details_container, detailsFragment,
                            MOVIE_DETAILS_FRAGMENT_TAG)
                    .commit();
        }

        else
        {
            // When in one-pane layout just start a new MovieDetailActivity and pass the selected
            // movie's info to display it in a different screen
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieGridFragment.EXTRA_ID, movie.getId())
                    .putExtra(MovieGridFragment.EXTRA_IMAGE_URL, movie.getImageURL())
                    .putExtra(MovieGridFragment.EXTRA_ORIGINAL_TITLE, movie.getOriginalTitle())
                    .putExtra(MovieGridFragment.EXTRA_PLOT_SYNOPSIS, movie.getPlotSynopsis())
                    .putExtra(MovieGridFragment.EXTRA_USER_RATING, movie.getUserRating())
                    .putExtra(MovieGridFragment.EXTRA_RELEASE_YEAR, movie.getReleaseYear());

            startActivity(intent);
        }
    }
}
