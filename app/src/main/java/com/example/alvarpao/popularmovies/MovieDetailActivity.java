package com.example.alvarpao.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
        private static final Long MOVIE_ID_DEFAULT = -1L;
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

        private void getExtraMovieInfo(long movieId)
        {
            /*
            FetchExtraMovieInfoTask fetchExtraMovieInfoTask = new FetchExtraMovieInfoTask();
            fetchExtraMovieInfoTask.execute((new Long(movieId)).toString(), PAGE_1);
            */
        }

        /*
        public class FetchExtraMovieInfoTask extends AsyncTask<String, Void, Void> {

            private final String LOG_TAG = FetchExtraMovieInfoTask.class.getSimpleName();
            final String MOVIE_ENDPOINT_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String API_KEY_PARAMETER = "api_key";
            final String APPEND_TO_RESPONSE_PARAMETER = "append_to_response";
            final String APPEND_TO_RESPONSE_VALUE = "trailers,reviews";
            final String PAGE_PARAMETER = "page";


            private Movie[] getMovieInfoFromJson(String jsonReply) throws JSONException, ParseException{
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
            protected Void doInBackground(String... params) {

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
                return null;
            }

        }

       */

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
