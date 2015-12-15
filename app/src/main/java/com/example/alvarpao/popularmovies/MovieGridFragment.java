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

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.alvarpao.popularmovies.data.FavoriteMoviesContract;
import com.example.alvarpao.popularmovies.data.FavoriteMoviesDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A fragment containing a grid view of movies
 */
public class MovieGridFragment extends Fragment implements AbsListView.OnScrollListener{

    private GridView mMoviesGridView;
    private ArrayList<Movie> mMovies;
    private ArrayList<Movie> mSavedMovies = new ArrayList<Movie>();
    private MovieAdapter mMovieAdapter;
    private Spinner mSortSpinner;
    private boolean mLoadingMovies = true;
    private int mPageToFetch = 1;
    private int mPreviousTotalItems = 0;
    private int mSelectedMovie = -1;
    private boolean mSelectionOccurred = false;
    private static final String PAGE_1 = "1";
    private int mCurrentScrollPosition = 0;

    private static final String CURRENT_SCROLL_POSITION = "current_scroll_position";
    private static final String LOADED_MOVIES = "loaded_movies";
    private static final String PAGE_TO_FETCH = "page_to_fetch";
    private static final String LOADED_PREVIOUS_ITEMS = "previous_total_items";
    private static final String SELECTED_MOVIE = "selected_movie";
    private static final String SELECTION_OCCURRED = "selection_occurred";

    // This is an interface that the MainActivity containing the MovieGridFragment has to implement
    // in order for the fragment to notify the activity when a movie has been selected, or when
    // the DetailsFragment has to be cleared
    public interface Callback {
        // The MainActivity will have to implement this method
        void onItemSelected(Movie movie);
        void clearDetailsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Add the below line in order for this fragment to display and handle menu options.
        setHasOptionsMenu(true);

        if(savedInstanceState != null)
        {
            //State needs to be restored for this activity, a rotation occurred
            if(savedInstanceState.containsKey(LOADED_MOVIES))
                mMovies = savedInstanceState.getParcelableArrayList(LOADED_MOVIES);
            if(savedInstanceState.containsKey(PAGE_TO_FETCH))
              mPageToFetch = savedInstanceState.getInt(PAGE_TO_FETCH);
            if(savedInstanceState.containsKey(LOADED_PREVIOUS_ITEMS))
                mPreviousTotalItems = savedInstanceState.getInt(LOADED_PREVIOUS_ITEMS);
            if(savedInstanceState.containsKey(SELECTION_OCCURRED))
                mSelectionOccurred = savedInstanceState.getBoolean(SELECTION_OCCURRED);
            if(savedInstanceState.containsKey(CURRENT_SCROLL_POSITION))
                mCurrentScrollPosition = savedInstanceState.getInt(CURRENT_SCROLL_POSITION);
        }

        else
            mMovies = new ArrayList<Movie>();
    }

    private void updateDetailsFragment(){

        if (mMovieAdapter.getCount() > 0) {
            if (mSelectedMovie == -1) {
                // No movie has been selected by user, select the first one by default
                Movie firstMovie = mMovieAdapter.getItem(0);
                ((Callback) getActivity()).onItemSelected(firstMovie);
            } else {
                Movie selectedMovie = mMovieAdapter.getItem(mSelectedMovie);
                ((Callback) getActivity()).onItemSelected(selectedMovie);
            }
        } else
            ((Callback) getActivity()).clearDetailsFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment, menu);

        // Get the array of values for the sort options since the onItemSelected method only
        // returns the string representation of the item, not the value associated
        Resources resources = getResources();
        final TypedArray sortArrayValues = resources.obtainTypedArray(R.array.sort_options_values);

        MenuItem sortMenuItem = menu.findItem(R.id.sort_spinner);
        mSortSpinner = (Spinner) MenuItemCompat.getActionView(sortMenuItem);
        mSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(Utility.getPreferredSortOptionPosition(getActivity()) != position) {
                    // Update the shared preferences with the new sort option election
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    //Save the sort_by value to query the movie database
                    editor.putString(getString(R.string.sort_preference_key),
                            sortArrayValues.getString(position));
                    // Save the current position of the selected sort option
                    editor.putInt(getString(R.string.sort_position_preference_key), position);
                    editor.commit();

                    // Update movie grid to reflect new sort option selected by user
                    resetMovieGrid();
                    getMovies(PAGE_1);
                }

                else {
                    // If the sort option didn't change, the event most likely was triggered by
                    // either a rotation or the activity being created. In case of the rotation
                    // the state has already been restored in the onCreate() method, after onCreate()
                    // the onCreateOptionsMenu() is called. If the activity has been created, the
                    // favorite movies need to be fetched
                    if (Utility.getPreferredSortOption(getActivity()).equals(getString(R.string.sort_favorites_value))) {
                        if(!mMovies.isEmpty() && ((MainActivity) getActivity()).inTwoPaneLayout())
                            updateDetailsFragment();
                        else if(mMovies.isEmpty())
                            getMovies(Integer.valueOf(mPageToFetch).toString());
                    }

                    else {
                        // For the other sort options since pagination is involved the default
                        // movie to display in the details fragment when the user hasn't selected
                        // any movie is either the first one in the returned list when it is
                        // first loaded or the first visible movie before rotation. The first one
                        // when is first loaded is instantiated in the onScroll method
                        if(((MainActivity) getActivity()).inTwoPaneLayout()) {
                            if((mSelectedMovie!= -1) && (mMovieAdapter.getCount() != 0)){
                                Movie selectedMovie = mMovieAdapter.getItem(mSelectedMovie);
                                ((Callback) getActivity()).onItemSelected(selectedMovie);
                            }
                        }
                        // Determine if another page needs to be retrieved by making an API call
                        getMovies(Integer.valueOf(mPageToFetch).toString());
                    }

                    // Restoring the scroll position
                    mMoviesGridView.smoothScrollToPosition(mCurrentScrollPosition);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });

        // This statement is needed to restore the state of the sort selection in case a screen
        // rotation has occurred. The spinner is restored to the position it was retrieved
        // from the SharedPreferences;
        mSortSpinner.setSelection(Utility.getPreferredSortOptionPosition(getActivity()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // The adapter is initialized with an empty array if state wasn't restore on rotation, if
        // it was, the array of movies already has the saved data.
        //Toast.makeText(getActivity(), "mMovies size restored: " + mMovies.size(), Toast.LENGTH_SHORT).show();
        mMovieAdapter = new MovieAdapter(getActivity(), mMovies);
        mMoviesGridView = (GridView) rootView.findViewById(R.id.movies_grid);
        mMoviesGridView.setDrawSelectorOnTop(true);
        mMoviesGridView.setAdapter(mMovieAdapter);
        mMoviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                // This will force to draw the selector on top of the poster for the selected movie
                view.setSelected(true);
                mSelectedMovie = position;
                // A selection occurred as opposed to selecting the first visible item by default
                // so the details fragment is not empty in the two-pane layout.
                mSelectionOccurred = true;
                //Toast.makeText(getActivity(), "Selected Movie: " +
                // ((Movie) mMoviesGridView.getItemAtPosition(position)).getOriginalTitle(),
                // Toast.LENGTH_SHORT).show();
                Movie movie = mMovieAdapter.getItem(position);
                // A movie has been selected so the MainActivity has to be notified to take
                // appropriate action
                ((Callback) getActivity()).onItemSelected(movie);
            }
        });

        // Handles pagination for movie grid, except in the case the sort option is "Favorites",
        // since we don't need pagination in that case
        mMoviesGridView.setOnScrollListener(this);

        // If a rotation occurred, restore the selected movie (either selected by the user or
        // "selected" by default (first visible movie before rotation))
        // The restore of the details fragment with this selected after rotation is handled in the
        // onCreateOptionsMenu() method which is called after onCreateView()
        if(savedInstanceState != null && savedInstanceState.containsKey((SELECTED_MOVIE)))
            mSelectedMovie = savedInstanceState.getInt(SELECTED_MOVIE);

        return rootView;
    }

    @Override
    public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // Get the current selected sort option. If it is "Favorites" don't fetch another page
        // when scrolling since we have the whole list of movies in the local database and we don't
        // need to make requests to the API

        if(!(Utility.getPreferredSortOption(getActivity())).equals(getString(R.string.sort_favorites_value))) {
            // Since the onScroll method can get called several times at one time when the user is
            // scrolling, need to verify if the images are still loading before fetching another page
            // of results (loading it is first initialized as true)
            if (mLoadingMovies) {
                if (totalItemCount > mPreviousTotalItems) {
                    // Loading data to the grid view has been concluded
                    mLoadingMovies = false;
                    mPreviousTotalItems = totalItemCount;

                    // Check if device is in two-pane mode. If that is the case and the first page
                    // being fetched is page 1 (as a result of a change in the sort option or when
                    // the app is first loaded) show an instance of the detail fragment with the
                    // info of the first movie in the returned list of movies
                    if ((mPageToFetch == 1) && ((MainActivity) getActivity()).inTwoPaneLayout()) {
                        if(mMovieAdapter.getCount() > 0) {
                            Movie firstMovie = mMovieAdapter.getItem(0);
                            ((Callback) getActivity()).onItemSelected(firstMovie);
                        }
                        else
                            ((Callback) getActivity()).clearDetailsFragment();
                    }

                    mPageToFetch++;
                }
            }

            // In order for the next page to be fetched, the previous page has to be loaded and
            // the user has scrolled enough far down that there will be no more items to display in
            // the next scroll
            if (!mLoadingMovies && (totalItemCount == (firstVisibleItem + visibleItemCount))) {
                getMovies(Integer.toString(mPageToFetch));
                mLoadingMovies = true;
            }
        }
    }

    @Override
    public void onScrollStateChanged (AbsListView view, int scrollState) {
        // Needs to be part of the class, but no need to implement it for the movie grid
    }

    private void resetMovieGrid() {

        // When a change is sort option occurs the movie grid parameters need to be reset since
        // we are going to be working with a different list of movies
        // Toast.makeText(getActivity(), "Reset Movie Grid", Toast.LENGTH_SHORT).show();
        // Reset movie grid variables
        mMovieAdapter.clear();
        mSavedMovies.clear();
        mSelectedMovie = -1;
        mSelectionOccurred = false;

        // Reset scroll parameters
        mPageToFetch = 1;
        mLoadingMovies = true;
        mPreviousTotalItems = 0;
        mCurrentScrollPosition = 0;
    }

    private void getMovies(String pageToFetch) {

        //Toast.makeText(getActivity(), "Fetching page: " + Integer.valueOf(mPageToFetch).toString(),
        // Toast.LENGTH_SHORT).show();
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        //Get the appropriate sort option to fetch the movies in the right order
        fetchMoviesTask.execute(Utility.getPreferredSortOption(getActivity()), pageToFetch);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        mSavedMovies.clear();
        for(int index = 0; index < mMovieAdapter.getCount(); index++)
            mSavedMovies.add(mMovieAdapter.getItem(index));
        //Toast.makeText(getActivity(), "Number of movies saved: " + mSavedMovies.size(),
        // Toast.LENGTH_SHORT).show();
        savedInstanceState.putParcelableArrayList(LOADED_MOVIES, mSavedMovies);
        savedInstanceState.putInt(PAGE_TO_FETCH, mPageToFetch);
        savedInstanceState.putInt(LOADED_PREVIOUS_ITEMS, mPreviousTotalItems);
        // The user actually selected a movie, it was not pre-selected by the app to keep the
        // details fragment from being empty in the two-pane layout
        if((mSelectedMovie != -1) && (mSelectionOccurred))
            savedInstanceState.putInt(SELECTED_MOVIE, mSelectedMovie);
        else {
            // No movie selected save the position of the first visible item
            //Toast.makeText(getActivity(), "No position selected, select first visible",
            // Toast.LENGTH_SHORT).show();
            savedInstanceState.putInt(SELECTED_MOVIE, mMoviesGridView.getFirstVisiblePosition());
        }
        savedInstanceState.putBoolean(SELECTION_OCCURRED, mSelectionOccurred);
        savedInstanceState.putInt(CURRENT_SCROLL_POSITION, mMoviesGridView.getFirstVisiblePosition());

        super.onSaveInstanceState(savedInstanceState);
    }


    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        final String DISCOVER_MOVIE_ENDPOINT_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
        final String API_KEY_PARAMETER = "api_key";
        final String SORT_PARAMETER = "sort_by";
        final String PAGE_PARAMETER = "page";
        final String VOTE_COUNT_PARAMETER = "vote_count.gte";

        // The number 40 is based on Rotten Tomatoes website that considers 40 reviews (for limited
        // released movies) in order for a movie to be certified fresh. This eliminates movies that
        // have very few votes even though they are highly-rated.
        final String VOTE_COUNT_VALUE = "40";

        private Movie[] getMovieInfoFromJson(String jsonReply) throws JSONException, ParseException{
            // Parse out the required movie info from the JSON reply for each movie.
            // I used: https://jsonformatter.curiousconcept.com/ to format a given
            // JSON response and be able to develop code to parse out the required info

            // The JSON object returned contains a JSON array "results" which contains the
            // info on each movie returned

            final String RESULTS = "results";
            final String ID = "id";
            final String POSTER_PATH = "poster_path";
            final String ORIGINAL_TITLE = "original_title";
            final String PLOT_SYNOPSIS = "overview";
            final String USER_RATING = "vote_average";
            final String RELEASE_DATE = "release_date";

            SimpleDateFormat releaseDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();

            Movie[] movies;
            JSONObject movieDataJson = new JSONObject(jsonReply);

            // The results array returns info on all the movies sorted by either popularity or
            // rating (in ascending or descending order for both options)
            JSONArray resultsArray = movieDataJson.getJSONArray(RESULTS);

            // Movie data is returned by pages. Each page contains info on 20 movies (except for
            // the last page.
            movies = new Movie[resultsArray.length()];

            for (int index = 0; index < resultsArray.length(); index++)
            {
                JSONObject movieInfo = resultsArray.getJSONObject(index);

                // To match the UX mockups, get the release date from the JSON object and
                // extract the year from it to save it in the movie object
                Date releaseDate = releaseDateFormat.parse(movieInfo.getString(RELEASE_DATE));
                calendar.setTime(releaseDate);

                movies[index] = new Movie(movieInfo.getLong(ID),
                        buildImageURL(movieInfo.getString(POSTER_PATH)),
                        movieInfo.getString(ORIGINAL_TITLE),
                        movieInfo.getString(PLOT_SYNOPSIS),
                        movieInfo.getDouble(USER_RATING),
                        Integer.toString(calendar.get(Calendar.YEAR)));
                Log.v(LOG_TAG, "Poster Paths[" + Integer.toString(index) + "]: " + movieInfo.getString(POSTER_PATH));
            }

            return movies;

        }

        private Movie[] getFavoriteMovies(){

            // Query the local database for all the user's favorite movies
            Movie[] movies = {};
            int index = 0;
            FavoriteMoviesDbHelper dbHelper = new FavoriteMoviesDbHelper(getActivity());
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            Cursor favoriteCursor = database.query(
                    FavoriteMoviesContract.FavoriteEntry.TABLE_NAME,  // Table to query
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    FavoriteMoviesContract.FavoriteEntry.COLUMN_THEMOVIEDB_ID  // sort order
            );

            if(favoriteCursor.moveToFirst()) {
                movies = new Movie[favoriteCursor.getCount()];
                while (favoriteCursor.isAfterLast() == false) {
                    movies[index] = new Movie(favoriteCursor.getInt(1),
                            favoriteCursor.getString(2),
                            favoriteCursor.getString(3),
                            favoriteCursor.getString(5),
                            favoriteCursor.getFloat(7),
                            favoriteCursor.getString(4));
                    index++;
                    favoriteCursor.moveToNext();
                }
                favoriteCursor.close();
            }

            return movies;
        }

        @Override
        protected Movie[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String movieInfoJsonReply = null;

            // If there is no sorting criteria (popularity, rating, favorites) no point on fetching
            // anything
            if (params.length == 0) {
                return null;
            }

            if(!params[0].equals(getString(R.string.sort_favorites_value))) {
                try {
                    // Construction of the URL query for the Movie Database
                    // More info about the API: http://docs.themoviedb.apiary.io/#
                    Uri.Builder uriQuery = Uri.parse(DISCOVER_MOVIE_ENDPOINT_BASE_URL).buildUpon()
                            .appendQueryParameter(API_KEY_PARAMETER, ApiKey.API_KEY)
                            .appendQueryParameter(SORT_PARAMETER, params[0])
                            .appendQueryParameter(PAGE_PARAMETER, params[1]);

                    // Adding this parameter eliminates getting highly-rated movies with very few votes.
                    // The minimum amount of votes I chose is based on a similar criteria used by rotten
                    // tomatoes (Certified Fresh)
                    if (params[0].equals(getString(R.string.sort_highest_rated_value)))
                        uriQuery.appendQueryParameter(VOTE_COUNT_PARAMETER, VOTE_COUNT_VALUE);

                    Uri query = uriQuery.build();
                    URL queryUrl = new URL(query.toString());

                    InputStream inputStream;
                    StringBuffer receiveBuffer;

                    // Determine if the device has an internet connection
                    if (Utility.deviceIsConnected(getActivity())) {
                        // Creating the GET request to the Movie Database and then open
                        // a HTTP connection
                        urlConnection = (HttpURLConnection) queryUrl.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();

                        inputStream = urlConnection.getInputStream();
                        receiveBuffer = new StringBuffer();

                        if (inputStream == null) {
                            return null;
                        }
                    }

                    // Device doesn't have an internet connection
                    else
                        return null;

                    // Preparing to read JSON response line by line
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        // Adding a new line to every line of JSON response read for
                        // debugging purposes
                        receiveBuffer.append(line + "\n");
                    }

                    if (receiveBuffer.length() == 0) {
                        // If not response received then do nothing
                        return null;
                    }

                    // Completing reading JSON response
                    movieInfoJsonReply = receiveBuffer.toString();
                    Log.v(LOG_TAG, "JSON Movie Reply: " + movieInfoJsonReply);
                } catch (IOException exception) {

                    Log.e(LOG_TAG, getString(R.string.io_error_message), exception);
                    return null;
                }

                //Close HTTP connection and buffered reader even if there is an exception
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }

                    if (bufferedReader != null) {

                        try {
                            bufferedReader.close();
                        } catch (IOException exception) {
                            Log.e(LOG_TAG, getString(R.string.error_closing_reader), exception);
                        }
                    }
                }

                // Parse JSON response to extract the movie required movie infor
                try {
                    return getMovieInfoFromJson(movieInfoJsonReply);
                } catch (JSONException exception) {
                    Log.e(LOG_TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                } catch (ParseException exception) {
                    Log.e(LOG_TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }

            // No need to query the API, just the local database
            else
                return getFavoriteMovies();

            return null;
        }

        private String buildImageURL(String posterPath)
        {
            // To build the complete URL for the movie poster you need to append a base path ahead
            // of the poster_path (relative path) returned by the FetchMoviesTask. You then
            // need to append the size and then finally the poster_path returned by the AsyncTask.
            // For more information check the Movie Database API in the configuration section
            final String BASE_URL = "http://image.tmdb.org/t/p/";
            String size = "w185";

            Log.v(LOG_TAG, "Movie Poster: " + BASE_URL + size + posterPath);
            return BASE_URL + size + posterPath;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {

            if (movies != null) {

                for(Movie movie : movies)
                    mMovieAdapter.add(movie);

                // Get the current selected sort option. If it is "Favorites", Check if device is
                // in two-pane mode. If that is the case show an instance of the detail fragment
                // with the info of the first movie in the list or the restored selected movie
                // (in case there was a rotation)
                if ((Utility.getPreferredSortOption(getActivity())).equals(getString(R.string.sort_favorites_value))) {
                    if (((MainActivity) getActivity()).inTwoPaneLayout())
                        updateDetailsFragment();
                }
            }

            else if(movies == null && !Utility.deviceIsConnected(getActivity()))
                Toast.makeText(getActivity(), getString(R.string.no_internet_error),
                        Toast.LENGTH_SHORT).show();
        }
    }
}
