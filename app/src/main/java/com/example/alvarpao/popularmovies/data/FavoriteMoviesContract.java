package com.example.alvarpao.popularmovies.data;

/**
 * Created by alvarpao on 11/17/2015.
 */

import android.provider.BaseColumns;

/**
 * Defines table and column names for the favorite movies database.
 */
public class FavoriteMoviesContract {

    // Inner class that defines the table contents of the review table

    public static final class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "review";

        // Column with the foreign key from the favorite table.
        public static final String COLUMN_FAVE_KEY = "favorite_id";
        // Name of the author of this review
        public static final String COLUMN_AUTHOR = "author";
        // The content of this review
        public static final String COLUMN_CONTENT = "content";

    }

    // Inner class that defines the table contents of the trailer table
    public static final class TrailerEntry implements BaseColumns {

        public static final String TABLE_NAME = "trailer";

        // Column with the foreign key from the favorite table.
        public static final String COLUMN_FAVE_KEY = "favorite_id";
        // Name of the returned youtube trailer
        public static final String COLUMN_NAME = "name";
        // Source and last part in the youtube URL for the trailer
        public static final String COLUMN_SOURCE = "source";

    }

    /* Inner class that defines the table contents of the favorite table */
    public static final class FavoriteEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorite";

        // The id provided by themoviedb.org to the movie
        public static final String COLUMN_THEMOVIEDB_ID = "themoviedb_id";
        // The full path to the movie's poster
        public static final String COLUMN_IMAGE_URL = "image_url";

        // Original title for the favorited movie
        public static final String COLUMN_ORGINAL_TITLE = "original_title";
        // Release year stored as a string for the favorited movie
        public static final String COLUMN_RELEASE_YEAR = "release_year";

        // Runtime for the favorited movie stored as integer
        public static final String RUNTIME = "runtime";

        // User rating stored as float
        public static final String USER_RATING = "user_rating";
        public static final String PLOT_SYNOPSIS = "overview";

    }
}