package com.example.alvarpao.popularmovies;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
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
    private MovieAdapter mMovieAdapter;
    private Spinner mSortSpinner;
    private boolean mLoadingMovies = true;
    private int mPageToFetch = 1;
    private int mPreviousTotalItems = 0;
    private static final String PAGE_1 = "1";
    private int mCurrentScrollPosition = GridView.INVALID_POSITION;

    public static final String EXTRA_ID = "com.example.alvarpao.popularmovies.ID";
    public static final String EXTRA_IMAGE_URL = "com.example.alvarpao.popularmovies.IMAGE_URL";
    public static final String EXTRA_ORIGINAL_TITLE = "com.example.alvarpao.popularmovies.ORIGINAL_TITLE";
    public static final String EXTRA_PLOT_SYNOPSIS = "com.example.alvarpao.popularmovies.PLOT_SYNOPSIS";
    public static final String EXTRA_USER_RATING = "com.example.alvarpao.popularmovies.USER_RATING";
    public static final String EXTRA_RELEASE_YEAR = "com.example.alvarpao.popularmovies.RELEASE_YEAR";
    private static final String CURRENT_SCROLL_POSITION = "current_scroll_position";


    // This is an interface that the MainActivity containing the MovieGridFragment has to implement
    // in order for the fragment to notify the activity when a movie has been selected
    public interface Callback {

        // The MainActivity will have to implement this method
        public void onItemSelected(Movie movie);
    }


    public MovieGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add the below line in order for this fragment to display and handle menu options.
        setHasOptionsMenu(true);

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

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });

        // This statement is needed to restore the state of the sort selection in case a screen
        // rotation has occurred. The spinner is restored to the position it was retrieved
        // from the savedInstanceState received in the onCreateView method (after onCreateView
        // finishes onCreateOptionsMenu is called).

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int paramSortPosition = preferences.getInt(
                getString(R.string.sort_position_preference_key),
                0);
        mSortSpinner.setSelection(paramSortPosition);

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

        // The adapter is initialized with an empty array of movies until it gets data
        // from the website
        mMovieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());

        mMoviesGridView = (GridView) rootView.findViewById(R.id.movies_grid);
        mMoviesGridView.setAdapter(mMovieAdapter);

        mMoviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                Movie movie = mMovieAdapter.getItem(position);

                // A movie has been selected so the MainActivity has to be notified to take
                // appropriate action
                ((Callback) getActivity()).onItemSelected(movie);

            }
        });

        //Handles pagination for movie grid
        mMoviesGridView.setOnScrollListener(this);

        // If a screen rotation occurred, the scroll position in the movie grid needs to be restored
        // by retrieving the value from the savedInstanceState variable
        if (savedInstanceState != null)
        {
            if(savedInstanceState.containsKey(CURRENT_SCROLL_POSITION))
            {
                mCurrentScrollPosition = savedInstanceState.getInt(CURRENT_SCROLL_POSITION);
                mMoviesGridView.smoothScrollToPosition(mCurrentScrollPosition);
            }
        }

        return rootView;
    }

    @Override
    public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount,
                          int totalItemCount)
    {

        // Since the onScroll method can get called several times at one time when the user is
        // scrolling, need to verify if the images are still loading before fetching another page
        // of results (loading it is first initialized as true)
        if (mLoadingMovies)
        {
            if (totalItemCount > mPreviousTotalItems)
            {
                // Loading data to the grid view has been concluded
                mLoadingMovies = false;
                mPreviousTotalItems = totalItemCount;

                // Check if device is in two-pane mode. If that is the case and the first page
                // being fetched is page 1 (as a result of a change in the sort option or when
                // the app is first loaded) show an instance of the detail fragment with the
                // info of the first movie in the returned list of movies
                if( (mPageToFetch == 1) && ((MainActivity)getActivity()).getLayoutMode())
                {
                    Movie firstMovie = mMovieAdapter.getItem(0);

                    ((Callback) getActivity()).onItemSelected(firstMovie);
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

    @Override
    public void onScrollStateChanged (AbsListView view, int scrollState)
    {
        // Needs to be part of the class, but no need to implement it for the movie grid
    }

    private void resetMovieGrid()
    {
        // Clear movie grid adapter
        mMovieAdapter.clear();

        // Reset scroll parameters
        mPageToFetch = 1;
        mLoadingMovies = true;
        mPreviousTotalItems = 0;

    }

    private void getMovies(String pageToFetch)
    {
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        //Get the appropriate sort option to fetch the movies in the right order
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String paramSortValue = preferences.getString(getString(R.string.sort_preference_key),
                getString(R.string.sort_preference_default));
        fetchMoviesTask.execute(paramSortValue, pageToFetch);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // When a screen rotation occurs the scroll position needs to be saved to be restored later
        if (mCurrentScrollPosition != GridView.INVALID_POSITION) {
            savedInstanceState.putInt(CURRENT_SCROLL_POSITION,
                    mMoviesGridView.getFirstVisiblePosition());
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    // This method is the equivalent of the onRestoreInstanceState method for activities
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
        {
            if(savedInstanceState.containsKey(CURRENT_SCROLL_POSITION)) {
                //If there was a screen rotation restore the saved scroll position
                mCurrentScrollPosition = savedInstanceState.getInt(CURRENT_SCROLL_POSITION);
            }

        }

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

        @Override
        protected Movie[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String movieInfoJsonReply = null;

            // If there is no sorting criteria (popularity, rating) no point on fetching anything
            if (params.length == 0) {
                return null;
            }

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
                if(params[0].equals(getString(R.string.sort_highest_rated_value)))
                    uriQuery.appendQueryParameter(VOTE_COUNT_PARAMETER, VOTE_COUNT_VALUE);

                Uri query = uriQuery.build();
                URL queryUrl = new URL(query.toString());

                // Creating the GET request to the Movie Database and then open
                // a HTTP connection
                urlConnection = (HttpURLConnection) queryUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer receiveBuffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

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
            }

            catch (IOException exception) {

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
            }

            catch (JSONException exception) {
                Log.e(LOG_TAG, exception.getMessage(), exception);
                exception.printStackTrace();
            }

            catch(ParseException exception) {
                Log.e(LOG_TAG, exception.getMessage(), exception);
                exception.printStackTrace();
            }

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

                for(Movie movie : movies) {
                    mMovieAdapter.add(movie);
                }
            }
        }

    }
}
