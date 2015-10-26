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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A fragment containing a grid view of movie posters
 */
public class MovieGridFragment extends Fragment implements AbsListView.OnScrollListener{

    private MoviePosterAdapter mPosterAdapter;
    private boolean mLoadingImages = true;
    private int mPageToFetch = 1;
    private int mPreviousTotalItems = 0;


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
        Spinner sortSpinner = (Spinner) MenuItemCompat.getActionView(sortMenuItem);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Update the shared preferences with the new sort option election
                SharedPreferences sharedPreferences =
                  PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.sort_preference_key),
                        sortArrayValues.getString(position));
                editor.commit();

                Toast.makeText(getActivity(), "Preference sort: " + sortArrayValues.getString(position), Toast.LENGTH_SHORT).show();
                // Update posters to reflect new sort option selected by user
                mPosterAdapter.clear();
                mPageToFetch = 1;
                mLoadingImages = true;
                mPreviousTotalItems = 0;
                getPostersPaths();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
              //Do nothing
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // The adapter is initialized with an empty array of movies until it gets data
        // from the website
        mPosterAdapter = new MoviePosterAdapter(getActivity(), new ArrayList<MoviePoster>());

        GridView moviesGridView = (GridView) rootView.findViewById(R.id.movies_grid);
        moviesGridView.setAdapter(mPosterAdapter);

        moviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                Toast.makeText(getActivity(), "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });

        //Handles pagination of movie posters
        moviesGridView.setOnScrollListener(this);

        return rootView;
    }

    @Override
    public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount,
                          int totalItemCount)
    {

        // Since the onScroll method can get called several times at one time when the user is
        // scrolling, need to verify if the images are still loading before fetching another page
        // of results (loading it is first initialized as true)
        if (mLoadingImages)
        {
            if (totalItemCount > mPreviousTotalItems)
            {
                // Loading data to the grid view has been concluded
                mLoadingImages = false;
                mPreviousTotalItems = totalItemCount;
                mPageToFetch++;
            }
        }

        // In order for the next page to be fetched, the previous page has to be loaded and
        // the user has scrolled enough far down that there will be no more items to display in
        // the next scroll
        if (!mLoadingImages && (totalItemCount == (firstVisibleItem + visibleItemCount))) {

            // Retrieve the preferred sort option to retrieve the posters in the appropriate order
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String paramSortValue = preferences.getString(getString(R.string.sort_preference_key),
                    getString(R.string.sort_preference_default));
            new FetchPosterPathsTask().execute(paramSortValue, Integer.toString(mPageToFetch));
            mLoadingImages = true;
        }
    }

    @Override
    public void onScrollStateChanged (AbsListView view, int scrollState)
    {
        // Needs to be part of the class, but no need to implement it for the movie grid
    }



    private void getPostersPaths()
    {
        FetchPosterPathsTask fetchPosterPathsTask = new FetchPosterPathsTask();
        //Get the appropriate sort option to fetch the posters in the right order
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String paramSortValue = preferences.getString(getString(R.string.sort_preference_key),
                getString(R.string.sort_preference_default));
        fetchPosterPathsTask.execute(paramSortValue, "1");
    }


    public class FetchPosterPathsTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchPosterPathsTask.class.getSimpleName();
        final String DISCOVER_MOVIE_ENDPOINT_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
        final String API_KEY_PARAMETER = "api_key";
        final String SORT_PARAMETER = "sort_by";
        final String PAGE_PARAMETER = "page";
        final String VOTE_COUNT_PARAMETER = "vote_count.gte";
        // The number 40 is based on Rotten Tomatoes website that considers 40 reviews (for limited
        // released movies) in order for a movie to be certified fresh. This eliminates movies that
        // have very few votes even though they are highly-rated.
        final String VOTE_COUNT_VALUE = "40";

        private String[] getPosterPathsFromJson(String jsonReply) throws JSONException {
            // Parse out only the poster path in the JSON reply for each movie.
            // I used: https://jsonformatter.curiousconcept.com/ to format a
            // given JSON response and be able to develop code to parse out poster_path

            // The JSON object returned contains a JSON array "results" which contains the
            // info on each movie returned (including the poster_path)

            final String RESULTS = "results";
            final String POSTER_PATH = "poster_path";
            String[] posterPaths;

            JSONObject movieDataJson = new JSONObject(jsonReply);

            // The results array returns info on all the movies sorted by either popularity or
            // rating (in ascending or descending order for both options)
            JSONArray resultsArray = movieDataJson.getJSONArray(RESULTS);

            // Movie data is returned by pages. Each page contains info on 20 movies (except for
            // the last page.
            posterPaths = new String[resultsArray.length()];

            for (int index = 0; index < resultsArray.length(); index++) {
                JSONObject movieInfo = resultsArray.getJSONObject(index);
                posterPaths[index] = movieInfo.getString(POSTER_PATH);
                Log.v(LOG_TAG, "Poster Paths[" + Integer.toString(index) + "]: " + posterPaths[index]);
            }

            return posterPaths;

        }

        @Override
        protected String[] doInBackground(String... params) {

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

            // Parse JSON response to extract the movie posters paths
            try {

                return getPosterPathsFromJson(movieInfoJsonReply);
            } catch (JSONException exception) {
                Log.e(LOG_TAG, exception.getMessage(), exception);
                exception.printStackTrace();
            }

            return null;
        }

        private String buildImageURL(String posterPath)
        {
            // To build the complete URL for the movie poster you need to append a base path ahead
            // of the poster_path (relative path) returned by the FetchPosterPathsTask. You then
            // need to append the size and then finally the poster_path returned by the AsyncTask.
            // For more information check the Movie Database API in the configuration section

            final String BASE_URL = "http://image.tmdb.org/t/p/";
            String size = "w185";

            Log.v(LOG_TAG, "Movie Poster: " + BASE_URL + size + posterPath);
            return BASE_URL + size + posterPath;

        }

        @Override
        protected void onPostExecute(String[] posterPaths) {

            if (posterPaths != null) {

                for(String posterPath : posterPaths) {
                    mPosterAdapter.add(new MoviePoster(buildImageURL(posterPath)));
                }
            }
        }

    }
}
