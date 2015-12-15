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

/**
 * Created by alvarpao on 12/2/2015.
 */

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Movie fragment containing the movie details info
 */

public class MovieDetailFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    static final String MOVIE_DETAILS = "movie_details";
    static final String MOVIE = "movie";
    static final String SCROLL_POSITION = "scroll_position";

    private Movie mMovie;
    private MovieDetailsRecyclerAdapter mMovieDetailsAdapter;
    private RecyclerView mMovieDetailsRecyclerView;
    private int mScrollPosition = 0;

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

        // Restore state of DetailsFragment if there was a rotation
        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(MOVIE))
                mMovie = savedInstanceState.getParcelable(MOVIE);

            if (savedInstanceState.containsKey(SCROLL_POSITION))
                mScrollPosition = savedInstanceState.getInt(SCROLL_POSITION);
        }

        else {
            if (arguments != null)
                mMovie = arguments.getParcelable(MOVIE_DETAILS);
        }

        // Initialize Movie Details' recycler view
        mMovieDetailsRecyclerView = (RecyclerView)
                rootView.findViewById(R.id.movieDetailsRecyclerViewer);
        mMovieDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(mMovieDetailsRecyclerView.getContext()));
        mMovieDetailsRecyclerView.setItemAnimator(null);

        if (arguments != null) {
            // Initially the trailers and reviews arrays are empty, it is not after the
            // getExtraMovieInfo() is called that the adapter is populated.
            mMovieDetailsAdapter = new MovieDetailsRecyclerAdapter(getActivity(), mMovie, mMovie.trailers, mMovie.reviews);
            // Make sure the trailers and reviews for the adapter are reset
            mMovieDetailsRecyclerView.setAdapter(mMovieDetailsAdapter);

            // Restore scroll position for Recycler View
            if(savedInstanceState != null)
                mMovieDetailsRecyclerView.getLayoutManager().scrollToPosition(mScrollPosition);

            //No needed to retrieve trailers and reviews if there was a rotation
            if(savedInstanceState == null) {
                mMovieDetailsAdapter.clearTrailersAndReviews();
                mMovieDetailsAdapter.notifyDataSetChanged();
                getExtraMovieInfo();
            }
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //Save movie details to be restored after rotation
        if(mMovie != null)
            savedInstanceState.putParcelable(MOVIE, mMovie);

        // Save current scroll position for the recycler view
        savedInstanceState.putInt(SCROLL_POSITION, ((LinearLayoutManager) mMovieDetailsRecyclerView.getLayoutManager()).findFirstVisibleItemPosition());
    }

    private void getExtraMovieInfo() {
        // Fetch the runtime, trailers and reviews for the movie currently in the detail
        // view
        FetchExtraMovieInfoTask fetchExtraMovieInfoTask = new FetchExtraMovieInfoTask();
        fetchExtraMovieInfoTask.execute(mMovie);
    }

    // It deselects a trailer in the recycler view after the youtube video has been launched
    public void deselectTrailerItem(){
        mMovieDetailsAdapter.deselectTrailerItem();
    }

    public class FetchExtraMovieInfoTask extends AsyncTask<Movie, Void, Boolean> {

        private final String LOG_TAG = FetchExtraMovieInfoTask.class.getSimpleName();
        final String MOVIE_ENDPOINT_BASE_URL = "http://api.themoviedb.org/3/movie/";
        final String API_KEY_PARAMETER = "api_key";
        final String APPEND_TO_RESPONSE_PARAMETER = "append_to_response";
        final String APPEND_TO_RESPONSE_VALUE = "trailers,reviews";
        final String PAGE_PARAMETER = "page";
        private static final String PAGE_1 = "1";


        private boolean setExtraMovieInfoFromJson(String jsonReply) throws JSONException, ParseException {
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
        protected Boolean doInBackground(Movie... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String movieInfoJsonReply = null;

            // If there is no movie passed no point on fetching anything
            if (params.length == 0) {
                return null;
            }

            try {

                mMovie = params[0];
                // Construction of the URL query for the Movie Database
                // More info about the API: http://docs.themoviedb.apiary.io/#

                Uri.Builder uriQuery = Uri.parse(MOVIE_ENDPOINT_BASE_URL).buildUpon()
                        .appendPath((new Long(mMovie.getId())).toString())
                        .appendQueryParameter(API_KEY_PARAMETER, ApiKey.API_KEY)
                        .appendQueryParameter(APPEND_TO_RESPONSE_PARAMETER, APPEND_TO_RESPONSE_VALUE)
                        .appendQueryParameter(PAGE_PARAMETER, PAGE_1);

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

                if (getActivity() != null) {
                    if (getActivity().findViewById(R.id.runtime) != null) {
                        // Populate the movie details view with the movie's runtime
                        ((TextView) getActivity().findViewById(R.id.runtime))
                                .setText((new Integer(mMovie.getRuntime())).toString() +
                                        getString(R.string.runtime_units));
                        mMovieDetailsAdapter.notifyItemChanged(0);


                        // If the movie has any trailers, populate the adapter with them
                        if (mMovie.trailers.size() != 0) {
                            int trailerIndex = 0;
                            for (Trailer trailer : mMovie.trailers) {
                                mMovieDetailsAdapter.addTrailerItem(trailerIndex, trailer);
                                trailerIndex++;
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.no_trailers_found),
                                    Toast.LENGTH_SHORT).show();
                        }

                        // If the movie has any reviews, populate the adapter with them
                        if (mMovie.reviews.size() != 0) {
                            int reviewIndex = 0;
                            for (Review review : mMovie.reviews) {
                                mMovieDetailsAdapter.addReviewItem(reviewIndex, review);
                                reviewIndex++;
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.no_reviews_found),
                                    Toast.LENGTH_SHORT).show();
                        }

                        // Scroll to position 0 (Movie details header) so the user see the
                        // details first instead of the last item of the reviews or trailers
                        mMovieDetailsRecyclerView.scrollToPosition(0);
                    }
                }

            } else if (result == null && !Utility.deviceIsConnected(getActivity())) {
                if(getActivity() != null)
                Toast.makeText(getActivity(), getString(R.string.no_internet_error),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
