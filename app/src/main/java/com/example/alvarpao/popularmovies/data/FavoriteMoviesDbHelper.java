package com.example.alvarpao.popularmovies.data;

/**
 * Created by alvarpao on 11/17/2015.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.alvarpao.popularmovies.data.FavoriteMoviesContract.FavoriteEntry;
import com.example.alvarpao.popularmovies.data.FavoriteMoviesContract.TrailerEntry;
import com.example.alvarpao.popularmovies.data.FavoriteMoviesContract.ReviewEntry;

/**
 * Manages a local database for the favorites movies data.
 */
public class FavoriteMoviesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "favorite_movies.db";

    public FavoriteMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +

                FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                FavoriteEntry.COLUMN_THEMOVIEDB_ID + " INTEGER NOT NULL, " +
                FavoriteEntry.COLUMN_IMAGE_URL + " TEXT, " +
                FavoriteEntry.COLUMN_ORGINAL_TITLE + " TEXT NOT NULL," +

                FavoriteEntry.COLUMN_RELEASE_YEAR + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_PLOT_SYNOPSIS + " TEXT NOT NULL, " +

                FavoriteEntry.COLUMN_RUNTIME + " INTEGER NOT NULL, " +
                FavoriteEntry.COLUMN_USER_RATING + " REAL NOT NULL, " +

                // To assure the application have just one favorite entry per movie
                // a UNIQUE constraint is created
                                " UNIQUE (" + FavoriteEntry.COLUMN_THEMOVIEDB_ID
                + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +

                TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                TrailerEntry.COLUMN_FAVE_KEY + " INTEGER NOT NULL, " +
                TrailerEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_SOURCE + " TEXT NOT NULL, " +

                // Set up the favorite movie column id as a foreign key to favorite table.
                " FOREIGN KEY (" + TrailerEntry.COLUMN_FAVE_KEY + ") REFERENCES " +
                FavoriteEntry.TABLE_NAME + " (" + FavoriteEntry._ID + "));";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +

                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                ReviewEntry.COLUMN_FAVE_KEY + " INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +

                // Set up the favorite movie column id as a foreign key to favorite table.
                " FOREIGN KEY (" + ReviewEntry.COLUMN_FAVE_KEY + ") REFERENCES " +
                FavoriteEntry.TABLE_NAME + " (" + FavoriteEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        onCreate(sqLiteDatabase);
    }
}
