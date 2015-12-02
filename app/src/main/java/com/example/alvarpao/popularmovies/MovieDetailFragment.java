package com.example.alvarpao.popularmovies;

/**
 * Created by alvarpao on 12/2/2015.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alvarpao.popularmovies.data.FavoriteMoviesContract;
import com.example.alvarpao.popularmovies.data.FavoriteMoviesDbHelper;
import com.squareup.picasso.Picasso;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Movie fragment containing the movie details info
 */

public class MovieDetailFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    static final String MOVIE_DETAILS = "movie_details";

    private Movie mMovie;
    private ImageButton mBtnFavorite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Decimal format for user rating
        DecimalFormat decimalFormat = new DecimalFormat("##.#");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);

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
                    Double.valueOf(decimalFormat.format(mMovie.getUserRating())).toString() +
                            getString(R.string.user_rating_of_ten));
            ((TextView)rootView.findViewById(R.id.releaseYear)).setText(mMovie.getReleaseYear());

            getExtraMovieInfo();

        }

        mBtnFavorite = (ImageButton)rootView.findViewById(R.id.imgBtnFavorite);
        mBtnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFavoriteMovieDetails();
            }
        });


        // Query the local database to determine if the movie in the current detail view is
        // in the user's favorite list
        checkIfFavorite();

        return rootView;
    }

    private void checkIfFavorite() {

        // Query the local database to determine if the movie in the current detail view is
        // in the user's favorite list. If so, change the image of the "Mark Favorite" button
        // to "Favorite" and change the onClick method appropriate to delete the movie from
        // the database if clicked again.
        QueryIfFavoriteMovieTask queryIfFavoriteMovieTask =
                new QueryIfFavoriteMovieTask();
        queryIfFavoriteMovieTask.execute();

    }

    private void deleteMovieFromFavorites(){

        Log.v(LOG_TAG, "Drop movie from favorite list " + mMovie.getOriginalTitle());
        DeleteFavoriteMovieTask deleteFavoriteMovieTask = new DeleteFavoriteMovieTask();
        deleteFavoriteMovieTask.execute();
    }

    private void saveFavoriteMovieDetails() {

        // Save the current movie's details into the local database to keep the user's favorite
        // list
        SaveFavoriteMovieDetailsTask saveFavoriteMovieDetailsTask =
                new SaveFavoriteMovieDetailsTask();
        saveFavoriteMovieDetailsTask.execute();
    }

    private void getExtraMovieInfo() {
        // Fetch the runtime, trailers and reviews for the movie currently in the detail
        // view
        FetchExtraMovieInfoTask fetchExtraMovieInfoTask = new FetchExtraMovieInfoTask(getActivity());
        fetchExtraMovieInfoTask.execute(mMovie);
    }

    public class QueryIfFavoriteMovieTask extends AsyncTask<Void, Void, Boolean> {

        private SQLiteDatabase mDatabase;

        private Boolean queryIfFavorite() {

            // Query the database to find out if the current movie is in the favorite list
            // so the "Mark Favorite" button can change its image and its onClick method
            // appropriately
            Cursor favoriteCursor = mDatabase.query(
                    FavoriteMoviesContract.FavoriteEntry.TABLE_NAME,  // Table to Query
                    null, // leaving "columns" null just returns all the columns.
                    FavoriteMoviesContract.FavoriteEntry.COLUMN_THEMOVIEDB_ID + " = ?", // cols for "where" clause
                    new String[]{(new Long(mMovie.getId())).toString()}, // values for "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null  // sort order
            );

            if(favoriteCursor.moveToFirst()) {
                favoriteCursor.close();
                return true;
            }

            favoriteCursor.close();
            return null;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Get reference to writable favorite movies database
            FavoriteMoviesDbHelper dbHelper = new FavoriteMoviesDbHelper(getActivity());
            mDatabase = dbHelper.getWritableDatabase();

            if (queryIfFavorite() == null) {
                dbHelper.close();
                return null;
            }

            dbHelper.close();
            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {

            // Determine if the movie is in the favorite user's list
            if (result != null) {
                // Movie is in user's favorite list
                mBtnFavorite.setImageResource(R.drawable.favorite);

                // Change the onClick listener method to drop the movie if clicked again and
                // to update the image on the button appropriately
                mBtnFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteMovieFromFavorites();
                        mBtnFavorite.setImageResource(R.drawable.star);
                        mBtnFavorite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                saveFavoriteMovieDetails();
                            }
                        });
                    }
                });
            }
        }
    }

    public class DeleteFavoriteMovieTask extends AsyncTask<Void, Void, Boolean> {

        private SQLiteDatabase mDatabase;

        private Boolean deleteFavoriteMovie() {

            int movieIdToDelete;

            // Query the favorite table to get the id of the movie to be deleted. This id
            // is going to be used to delete the trailer and review entries related to
            // the movie.
            Cursor favoriteCursor = mDatabase.query(
                    FavoriteMoviesContract.FavoriteEntry.TABLE_NAME,  // Table to Query
                    new String[]{ FavoriteMoviesContract.FavoriteEntry._ID }, // Column to query.
                    FavoriteMoviesContract.FavoriteEntry.COLUMN_THEMOVIEDB_ID + " = ?", // cols for "where" clause
                    new String[]{(new Long(mMovie.getId())).toString()}, // values for "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null  // sort order
            );

            if(favoriteCursor.moveToFirst()) {
                movieIdToDelete = favoriteCursor.getInt(0);
                favoriteCursor.close();
            }

            else {
                favoriteCursor.close();
                return null;
            }

            // Delete from the database the given favorite movie
            int favoriteRowsDeleted = mDatabase.delete(
                    FavoriteMoviesContract.FavoriteEntry.TABLE_NAME,  // Table to Query
                    FavoriteMoviesContract.FavoriteEntry.COLUMN_THEMOVIEDB_ID + " = ?", // cols for "where" clause
                    new String[]{(Long.toString(mMovie.getId()))} // values for "where" clause
            );

            if(favoriteRowsDeleted == 0)
                return null;

            // Delete from the database all the trailers for the given movie
            mDatabase.delete(
                    FavoriteMoviesContract.TrailerEntry.TABLE_NAME,  // Table to Query
                    FavoriteMoviesContract.TrailerEntry.COLUMN_FAVE_KEY + " = ?", // cols for "where" clause
                    new String[]{(Integer.toString(movieIdToDelete))} // values for "where" clause
            );

            // Delete from the database all the reviews for the given movie
            mDatabase.delete(
                    FavoriteMoviesContract.ReviewEntry.TABLE_NAME,  // Table to Query
                    FavoriteMoviesContract.ReviewEntry.COLUMN_FAVE_KEY + " = ?", // cols for "where" clause
                    new String[]{(Integer.toString(movieIdToDelete))} // values for "where" clause
            );

            return true;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Get reference to writable favorite movies database
            FavoriteMoviesDbHelper dbHelper = new FavoriteMoviesDbHelper(getActivity());
            mDatabase = dbHelper.getWritableDatabase();

            if (deleteFavoriteMovie() == null) {
                dbHelper.close();
                return null;
            }

            dbHelper.close();
            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {

            // Determine if movie and its associated trailers and reviews were deleted
            // successfully
            if (result == null) {
                Toast.makeText(getActivity(), getString(R.string.error_deleting_favorite),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class SaveFavoriteMovieDetailsTask extends AsyncTask<Void, Void, Boolean> {

        private SQLiteDatabase mDatabase;

        private ContentValues createFavoriteValues()
        {
            // Create content values with the info of the movie currently displayed
            ContentValues favoriteValues = new ContentValues();
            favoriteValues.put(FavoriteMoviesContract.FavoriteEntry.COLUMN_THEMOVIEDB_ID,
                    mMovie.getId());
            favoriteValues.put(FavoriteMoviesContract.FavoriteEntry.COLUMN_IMAGE_URL,
                    mMovie.getImageURL());
            favoriteValues.put(FavoriteMoviesContract.FavoriteEntry.COLUMN_ORGINAL_TITLE,
                    mMovie.getOriginalTitle());
            favoriteValues.put(FavoriteMoviesContract.FavoriteEntry.COLUMN_RELEASE_YEAR,
                    mMovie.getReleaseYear());
            favoriteValues.put(FavoriteMoviesContract.FavoriteEntry.COLUMN_RUNTIME,
                    mMovie.getRuntime());
            favoriteValues.put(FavoriteMoviesContract.FavoriteEntry.COLUMN_USER_RATING,
                    mMovie.getUserRating());
            favoriteValues.put(FavoriteMoviesContract.FavoriteEntry.COLUMN_PLOT_SYNOPSIS,
                    mMovie.getPlotSynopsis());

            return favoriteValues;
        }

        private ContentValues[] createTrailersValues(long favoriteRowId){

            // Create content values for the trailers of the movie currently displayed
            ContentValues[] trailersContentValues = new ContentValues[mMovie.trailers.size()];

            for(int index = 0; index < mMovie.trailers.size(); index ++){

                ContentValues trailerValues = new ContentValues();
                trailerValues.put(FavoriteMoviesContract.TrailerEntry.COLUMN_FAVE_KEY, favoriteRowId);
                trailerValues.put(FavoriteMoviesContract.TrailerEntry.COLUMN_NAME,
                        (mMovie.trailers.get(index)).getName());
                trailerValues.put(FavoriteMoviesContract.TrailerEntry.COLUMN_SOURCE,
                        (mMovie.trailers.get(index)).getSource());
                trailersContentValues[index] = trailerValues;
            }

            return trailersContentValues;
        }

        private ContentValues[] createReviewsValues(long favoriteRowId){

            // Create content values for the reviews of the movie currently displayed
            ContentValues[] reviewsContentValues = new ContentValues[mMovie.reviews.size()];

            for(int index = 0; index < mMovie.reviews.size(); index ++){

                ContentValues reviewValues = new ContentValues();
                reviewValues.put(FavoriteMoviesContract.ReviewEntry.COLUMN_FAVE_KEY, favoriteRowId);
                reviewValues.put(FavoriteMoviesContract.ReviewEntry.COLUMN_AUTHOR,
                        (mMovie.reviews.get(index)).getAuthor());
                reviewValues.put(FavoriteMoviesContract.ReviewEntry.COLUMN_CONTENT,
                        (mMovie.reviews.get(index)).getContent());
                reviewsContentValues[index] = reviewValues;
            }

            return reviewsContentValues;
        }

        private int bulkInsert(ContentValues[] values, String tableName){

            // Method that will insert in a give table more than one record
            int count = 0;

            mDatabase.beginTransaction();
            try{
                for(ContentValues value : values){
                    long rowId = mDatabase.insert(tableName, null, value);
                    if(rowId != -1){
                        count++;
                    }
                }
                mDatabase.setTransactionSuccessful();
            }
            finally {
                mDatabase.endTransaction();
            }

            return count;
        }

        private Boolean saveMovie()
        {
            // Set up movie values to be inserted in favorite movie table
            ContentValues favoriteValues = createFavoriteValues();

            // Insert ContentValues into database and get a row ID back
            long favoriteRowId = mDatabase.insert
                    (FavoriteMoviesContract.FavoriteEntry.TABLE_NAME, null, favoriteValues);

            // Determine if insertion to the database was successful
            if(favoriteRowId == -1)
                return null;

            // Check if the movie has trailers before doing any trailer insertions into the
            // database
            if(mMovie.trailers.size() > 0)
            {
                ContentValues[] trailersValues = createTrailersValues(favoriteRowId);
                if(trailersValues.length > 1){
                    int count = bulkInsert(trailersValues, FavoriteMoviesContract.TrailerEntry.TABLE_NAME);
                    if(count != mMovie.trailers.size())
                        return null;
                }

                else {
                    long trailerRowId = mDatabase.insert
                            (FavoriteMoviesContract.TrailerEntry.TABLE_NAME, null, trailersValues[0]);
                    // Error inserting entry
                    if(trailerRowId == -1)
                        return null;
                }
            }

            // Check if the movie has reviews before doing any review insertions into the
            // database
            if(mMovie.reviews.size() > 0)
            {
                ContentValues[] reviewsValues = createReviewsValues(favoriteRowId);
                if(reviewsValues.length > 1){
                    int count = bulkInsert(reviewsValues, FavoriteMoviesContract.ReviewEntry.TABLE_NAME);
                    if(count != mMovie.reviews.size())
                        return null;
                }

                else {
                    long reviewRowId = mDatabase.insert
                            (FavoriteMoviesContract.ReviewEntry.TABLE_NAME, null, reviewsValues[0]);
                    // Error inserting entry
                    if(reviewRowId == -1)
                        return null;
                }
            }

            /////////////

            Cursor favoriteCursor = mDatabase.query(
                    FavoriteMoviesContract.FavoriteEntry.TABLE_NAME,  // Table to Query
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null  // sort order
            );

            Cursor trailerCursor = mDatabase.query(
                    FavoriteMoviesContract.TrailerEntry.TABLE_NAME,  // Table to Query
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null  // sort order
            );

            Cursor reviewCursor = mDatabase.query(
                    FavoriteMoviesContract.ReviewEntry.TABLE_NAME,  // Table to Query
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null  // sort order
            );

            Log.v(LOG_TAG, "Number of records in favorite table: " + favoriteCursor.getCount());

            favoriteCursor.moveToFirst();
            while (favoriteCursor.isAfterLast() == false) {
                Log.v(LOG_TAG, "Record: " + favoriteCursor.getInt(0) + " " +
                        favoriteCursor.getInt(1) + " " +
                        favoriteCursor.getString(2) + " " +
                        favoriteCursor.getString(3) + " " +
                        favoriteCursor.getString(4) + " " +
                        favoriteCursor.getString(5) + " " +
                        favoriteCursor.getInt(6) + " " +
                        favoriteCursor.getFloat(7));
                favoriteCursor.moveToNext();
            }

            favoriteCursor.close();

            Log.v(LOG_TAG, "Number of records in trailer table: " + trailerCursor.getCount());

            trailerCursor.moveToFirst();
            while (trailerCursor.isAfterLast() == false) {
                Log.v(LOG_TAG, "Record: " + trailerCursor.getInt(0) + " " +
                        trailerCursor.getInt(1) + " " +
                        trailerCursor.getString(2) + " " +
                        trailerCursor.getString(3));
                trailerCursor.moveToNext();
            }

            trailerCursor.close();

            Log.v(LOG_TAG, "Number of records in review table: " + reviewCursor.getCount());

            reviewCursor.moveToFirst();
            while (reviewCursor.isAfterLast() == false) {
                Log.v(LOG_TAG, "Record: " + reviewCursor.getInt(0) + " " +
                        reviewCursor.getInt(1) + " " +
                        reviewCursor.getString(2) + " " +
                        reviewCursor.getString(3));
                reviewCursor.moveToNext();
            }

            reviewCursor.close();

            ///////////

            return true;

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Get reference to writable favorite movies database
            FavoriteMoviesDbHelper dbHelper = new FavoriteMoviesDbHelper(getActivity());
            mDatabase = dbHelper.getWritableDatabase();

            if (saveMovie() == null) {
                dbHelper.close();
                return null;
            }

            dbHelper.close();
            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {

            if (result != null) {

                // Change the look of the "Mark Favorite" button to let know the user that this
                // movie is already in the favorite list
                mBtnFavorite.setImageResource(R.drawable.favorite);

                // Change the onClick method to drop the movie from the favorite list if
                // clicked again
                mBtnFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteMovieFromFavorites();
                        mBtnFavorite.setImageResource(R.drawable.star);
                        mBtnFavorite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                saveFavoriteMovieDetails();
                            }
                        });
                    }
                });

            }

            else if(result == null) {
                Toast.makeText(getActivity(), getString(R.string.error_saving_favorite),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }
}
