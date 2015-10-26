package com.example.alvarpao.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by alvarpao on 10/19/2015.
 * Custom adapter that will act as the source for all images displayed in the GridView
 */

public class MovieAdapter extends ArrayAdapter<Movie> {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Activity context, List<Movie> movies) {
        // Because this is a custom adapter for an ImageView, the adapter is not
        // going to use the second argument (like is the case when is an adapter for a TextView)
        // so it can be any value. Here, 0 was used.
        super(context, 0, movies);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Gets the Movie object from the ArrayAdapter at the appropriate position
        Movie movie = getItem(position);

        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View accordingly (in order to recycle)
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.poster_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.poster_image);

        //Using Picasso open source library to facilitate loading images and caching
        Picasso.with(getContext())
                .load(movie.getImageURL())
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.error_loading_image)
                .into(imageView);

        return convertView;
    }
}