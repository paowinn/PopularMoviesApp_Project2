package com.example.alvarpao.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.alvarpao.popularmovies.data.FavoriteMoviesContract;
import com.example.alvarpao.popularmovies.data.FavoriteMoviesDbHelper;

/**
 * Created by alvarpao on 12/7/2015.
 */
public class QueryIfFavoriteMovieTask extends AsyncTask<Movie, Void, Boolean> {

    private SQLiteDatabase mDatabase;
    private final String LOG_TAG = QueryIfFavoriteMovieTask.class.getSimpleName();
    private Movie mMovie;
    private Context mContext;

    public QueryIfFavoriteMovieTask(Context context) { mContext = context; }

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
    protected Boolean doInBackground(Movie... params) {

        // If there is no movie passed no point on querying anything
        if (params.length == 0) {
            return null;
        }

        mMovie = params[0];

        // Get reference to writable favorite movies database
        FavoriteMoviesDbHelper dbHelper = new FavoriteMoviesDbHelper(mContext);
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

        final ImageButton btnFavorite =
                (ImageButton)((Activity)mContext).findViewById(R.id.imgBtnFavorite);
        // Determine if the movie is in the favorite user's list
        if (result != null) {
            // Movie is in user's favorite list
            btnFavorite.setImageResource(R.drawable.favorite);

            // Change the onClick listener method to drop the movie if clicked again and
            // to update the image on the button appropriately
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
                            else if(mContext instanceof MainActivity)
                                ((MainActivity) mContext).saveFavoriteMovieDetails(mMovie);
                        }
                    });
                }
            });
        }
    }
}
