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

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.alvarpao.popularmovies.data.FavoriteMoviesContract;
import com.example.alvarpao.popularmovies.data.FavoriteMoviesDbHelper;


public class SaveFavoriteMovieDetailsTask extends AsyncTask<Movie, Void, Boolean> {

    private SQLiteDatabase mDatabase;
    private Movie mMovie;
    private final Context mContext;
    private static final String LOG_TAG = SaveFavoriteMovieDetailsTask.class.getSimpleName();

    public SaveFavoriteMovieDetailsTask(Context context) {
        mContext = context;
    }

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
    protected Boolean doInBackground(Movie... params) {

        // If there is no movie passed no point on saving anything
        if (params.length == 0) {
            return null;
        }

        mMovie = params[0];

        // Get reference to writable favorite movies database
        FavoriteMoviesDbHelper dbHelper = new FavoriteMoviesDbHelper(mContext);
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

            final ImageButton btnFavorite =
                    (ImageButton)((Activity)mContext).findViewById(R.id.imgBtnFavorite);
            // Change the look of the "Mark Favorite" button to let know the user that this
            // movie is already in the favorite list
            btnFavorite.setImageResource(R.drawable.favorite);

            // Change the onClick method to drop the movie from the favorite list if
            // clicked again
            btnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(LOG_TAG, "Drop movie from favorite list " + mMovie.getOriginalTitle());
                    //One-pane layout being displayed
                    if (mContext instanceof MovieDetailActivity)
                        ((MovieDetailActivity) mContext).deleteMovieFromFavorites(mMovie);
                    //Two-pane layout being displayed
                    else if(mContext instanceof MainActivity)
                        ((MainActivity) mContext).deleteMovieFromFavorites(mMovie);

                    btnFavorite.setImageResource(R.drawable.star);
                    btnFavorite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //One-pane layout being displayed
                            if (mContext instanceof MovieDetailActivity)
                                ((MovieDetailActivity) mContext).saveFavoriteMovieDetails(mMovie);
                            //Two-pane layout being displayed
                            else if (mContext instanceof MainActivity)
                                ((MainActivity) mContext).saveFavoriteMovieDetails(mMovie);
                        }
                    });
                }
            });

        }

        else if(result == null) {
            Toast.makeText(mContext, mContext.getString(R.string.error_saving_favorite),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
