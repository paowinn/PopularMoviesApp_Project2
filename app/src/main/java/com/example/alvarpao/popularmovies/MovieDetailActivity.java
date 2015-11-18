package com.example.alvarpao.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
import java.util.ArrayList;

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

    /**
     * Movie fragment containing the movie details info
     */

    public static class MovieDetailFragment extends Fragment {

        private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
        private static final String PAGE_1 = "1";
        static final String MOVIE_DETAILS = "movie_details";

        private Movie mMovie;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.movie_detail_fragment, container, false);

            // Get the fragmnent's arguments in case the fragment has been created when in
            // two-pane mode. If that is the case, retrieve the selected movie's info from the
            // arguments not the intent.
            Bundle arguments = getArguments();

            // If in one-pane mode get intent instead to get the passed movie info
            if(arguments == null)
             arguments = getActivity().getIntent().getExtras();

            if(arguments != null)
            {
                mMovie = arguments.getParcelable(MOVIE_DETAILS);

                ImageView moviePoster = ((ImageView) rootView.findViewById(R.id.moviePoster));

                //Using Picasso open source library to facilitate loading images and caching
                Picasso.with(getContext())
                        .load(mMovie.getImageURL())
                        .placeholder(R.drawable.image_loading)
                        .error(R.drawable.error_loading_image)
                        .into(moviePoster);

                ((TextView)rootView.findViewById(R.id.originalTitle)).setText(mMovie.getOriginalTitle());
                ((TextView)rootView.findViewById(R.id.plotSynopsis)).setText(mMovie.getPlotSynopsis());
                ((TextView)rootView.findViewById(R.id.userRating)).setText(
                        Double.valueOf(mMovie.getUserRating()).toString() +
                                getString(R.string.user_rating_of_ten));
                ((TextView)rootView.findViewById(R.id.releaseYear)).setText(mMovie.getReleaseYear());

                getExtraMovieInfo(mMovie.getId());

            }

            ImageButton btnFavorite = (ImageButton)rootView.findViewById(R.id.imgBtnFavorite);
            btnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "FAVORITE", Toast.LENGTH_SHORT).show();

                    /*
                    SaveFavoriteMovieDetailsTask saveFavoriteMovieDetailsTask =
                            new SaveFavoriteMovieDetailsTask();
                    saveFavoriteMovieDetailsTask.execute();
                    */
                }
            });


            return rootView;

        }

        private void getExtraMovieInfo(long movieId) {
            FetchExtraMovieInfoTask fetchExtraMovieInfoTask = new FetchExtraMovieInfoTask();
            fetchExtraMovieInfoTask.execute((new Long(movieId)).toString(), PAGE_1);

        }


        public class FetchExtraMovieInfoTask extends AsyncTask<String, Void, Boolean> {

            private final String LOG_TAG = FetchExtraMovieInfoTask.class.getSimpleName();
            final String MOVIE_ENDPOINT_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String API_KEY_PARAMETER = "api_key";
            final String APPEND_TO_RESPONSE_PARAMETER = "append_to_response";
            final String APPEND_TO_RESPONSE_VALUE = "trailers,reviews";
            final String PAGE_PARAMETER = "page";


            private boolean setExtraMovieInfoFromJson(String jsonReply) throws JSONException, ParseException{
                // Parse out the required movie info from the JSON reply for the movie in the detail
                // view.
                // I used: https://jsonformatter.curiousconcept.com/ to format a given
                // JSON response and be able to develop code to parse out the required info

                // The JSON object returned contains a JSON object which contains the info for the
                // runtime, trailers and reviews

                final String RUNTIME = "runtime";
                final String TRAILERS = "trailers";
                final String YOUTUBE = "youtube";
                final String TRAILER_NAME = "name";
                final String TRAILER_SOURCE = "source";
                final String REVIEWS = "reviews";
                final String REVIEWS_RESULTS = "results";
                final String REVIEW_AUTHOR = "author";
                final String REVIEW_CONTENT = "content";

                JSONObject movieDataJson = new JSONObject(jsonReply);

                // The runtime for the movie is part of the main JSON object returned by the query
                mMovie.setRuntime(movieDataJson.getInt(RUNTIME));

                // The trailers for the movie are stored as a JSON object in the main JSON object
                // returned by the query
                JSONObject trailersDataJson = movieDataJson.getJSONObject(TRAILERS);

                // The trailer data in the JSON object has two JSON Arrays, we are only interested
                // in the youtube array.
                JSONArray youtubeArray = trailersDataJson.getJSONArray(YOUTUBE);

                mMovie.trailers = new ArrayList<Trailer>(youtubeArray.length());

                // Store all the trailers info in the trailers array list that is part of the
                // displayed movie object
                for (int index = 0; index < youtubeArray.length(); index++)
                {
                    JSONObject trailerInfo = youtubeArray.getJSONObject(index);

                    mMovie.trailers.add(index, new Trailer(trailerInfo.getString(TRAILER_NAME),
                            trailerInfo.getString(TRAILER_SOURCE)));
                    Log.v(LOG_TAG, "Trailers[" + Integer.toString(index) + "]: " +
                            trailerInfo.getString(TRAILER_NAME));
                }

                // The reviews for the movie are stored as a JSON object in the main JSON object
                // returned by the query
                JSONObject reviewsDataJson = movieDataJson.getJSONObject(REVIEWS);

                // The reviews result data is stored in a JSON array in the reviews JSON object
                JSONArray reviewsResultsArray = reviewsDataJson.getJSONArray(REVIEWS_RESULTS);

                mMovie.reviews = new ArrayList<Review>(reviewsResultsArray.length());

                // Store all the reviews info in the reviews array list that is part of the
                // displayed movie object
                for (int index = 0; index < reviewsResultsArray.length(); index++)
                {
                    JSONObject reviewInfo = reviewsResultsArray.getJSONObject(index);

                    mMovie.reviews.add(index, new Review(reviewInfo.getString(REVIEW_AUTHOR),
                            reviewInfo.getString(REVIEW_CONTENT)));
                    Log.v(LOG_TAG, "Reviews[" + Integer.toString(index) + "]: " +
                            reviewInfo.getString(REVIEW_AUTHOR));
                }


                return true;

            }

            @Override
            protected Boolean doInBackground(String... params) {

                HttpURLConnection urlConnection = null;
                BufferedReader bufferedReader = null;
                String movieInfoJsonReply = null;

                // If there is no movie id no point on fetching anything
                if (params.length == 0) {
                    return null;
                }

                try {
                    // Construction of the URL query for the Movie Database
                    // More info about the API: http://docs.themoviedb.apiary.io/#

                    Uri.Builder uriQuery = Uri.parse(MOVIE_ENDPOINT_BASE_URL).buildUpon()
                            .appendPath(params[0])
                            .appendQueryParameter(API_KEY_PARAMETER, ApiKey.API_KEY)
                            .appendQueryParameter(APPEND_TO_RESPONSE_PARAMETER, APPEND_TO_RESPONSE_VALUE)
                            .appendQueryParameter(PAGE_PARAMETER, params[1]);

                    Uri query = uriQuery.build();
                    URL queryUrl = new URL(query.toString());

                    InputStream inputStream;
                    StringBuffer receiveBuffer;

                    // Determine if the device has an internet connection
                    if(Utility.deviceIsConnected(getActivity()))
                    {
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

                // Parse JSON response to extract the movie required info
                try {

                    return setExtraMovieInfoFromJson(movieInfoJsonReply);
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

            @Override
            protected void onPostExecute(Boolean result) {

                if (result != null) {

                    // Obtain the movie details view with the movie's runtime
                    ((TextView)(getActivity().findViewById(R.id.runtime)))
                            .setText((new Integer(mMovie.getRuntime())).toString() +
                            getString(R.string.runtime_units));

                }

                else if(result == null && !Utility.deviceIsConnected(getActivity()))
                    Toast.makeText(getActivity(), getString(R.string.no_internet_error),
                            Toast.LENGTH_SHORT).show();
            }

        }



        /*
        public class SaveFavoriteMovieDetailsTask extends AsyncTask<String, Void, Void> {

            @Override
            protected Void doInBackground(String... params) {
                return null;
            }

        }
        */
    }

}
