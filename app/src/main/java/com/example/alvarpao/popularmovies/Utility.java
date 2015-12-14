package com.example.alvarpao.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * Created by alvarpao on 11/17/2015.
 */
public class Utility {

    public static boolean deviceIsConnected(Context context) {
        //Determine if there is an active internet connection
        if(context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return ((activeNetwork != null) && activeNetwork.isConnectedOrConnecting());
        }

        return false;
    }

    public static String getPreferredSortOption(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return  preferences.getString(context.getString(R.string.sort_preference_key),
                context.getString(R.string.sort_preference_default));
    }

    public static int getPreferredSortOptionPosition(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(context.getString(R.string.sort_position_preference_key), 0);
    }
}
