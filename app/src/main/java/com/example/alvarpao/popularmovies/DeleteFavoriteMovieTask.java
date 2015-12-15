package com.example.alvarpao.popularmovies;

/**
 * Created by alvarpao on 12/2/2015.
 */

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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.alvarpao.popularmovies.data.FavoriteMoviesContract;
import com.example.alvarpao.popularmovies.data.FavoriteMoviesDbHelper;


public class DeleteFavoriteMovieTask extends AsyncTask<Movie, Void, Boolean> {

    private SQLiteDatabase mDatabase;
    private Movie mMovie;
    private final Context mContext;

    public DeleteFavoriteMovieTask(Context context) {
        mContext = context;
    }


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
    protected Boolean doInBackground(Movie... params) {

        // If there is no movie passed no point on deleting anything
        if (params.length == 0) {
            return null;
        }

        mMovie = params[0];

        // Get reference to writable favorite movies database
        FavoriteMoviesDbHelper dbHelper = new FavoriteMoviesDbHelper(mContext);
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
            Toast.makeText(mContext, mContext.getString(R.string.error_deleting_favorite),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
