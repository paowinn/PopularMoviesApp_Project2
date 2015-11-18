package com.example.alvarpao.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by alvarpao on 11/17/2015.
 */
public class Utility {

    public static boolean deviceIsConnected(Context context)
    {
        //Determine if there is an active internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return((activeNetwork != null) && activeNetwork.isConnectedOrConnecting());
    }
}
