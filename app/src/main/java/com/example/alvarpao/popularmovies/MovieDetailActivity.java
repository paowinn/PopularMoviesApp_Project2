package com.example.alvarpao.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * This class represents the movie details info for a selected movie from the movie grid
 */

public class MovieDetailActivity extends ActionBarActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MovieDetailFragment())
                    .commit();
        }

    }

    /**
     * Movie fragment containing the movie details info
     */

    public static class MovieDetailFragment extends Fragment {

        private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

        private String mImageURL;
        private String mOriginalTitle;
        private String mPlotSynopsis;
        private double mUserRating;
        private String mReleaseYear;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.movie_detail_fragment, container, false);

            // Since this activity was started via an Intent. Inspect the intent to get the
            // passed movie info
            Intent intent = getActivity().getIntent();
            if(intent != null)
            {

                if(intent.hasExtra(MovieGridFragment.EXTRA_IMAGE_URL))
                {
                   mImageURL = intent.getStringExtra(MovieGridFragment.EXTRA_IMAGE_URL);
                   ImageView moviePoster = ((ImageView)rootView.findViewById(R.id.moviePoster));

                   //Using Picasso open source library to facilitate loading images and caching
                   Picasso.with(getContext())
                            .load(mImageURL)
                            .placeholder(R.drawable.image_loading)
                            .error(R.drawable.error_loading_image)
                            .into(moviePoster);
                }

                if(intent.hasExtra(MovieGridFragment.EXTRA_ORIGINAL_TITLE)){
                    mOriginalTitle = intent.getStringExtra(MovieGridFragment.EXTRA_ORIGINAL_TITLE);
                    ((TextView)rootView.findViewById(R.id.originalTitle)).setText(mOriginalTitle);
                }

                if(intent.hasExtra(MovieGridFragment.EXTRA_PLOT_SYNOPSIS)){
                    mPlotSynopsis = intent.getStringExtra(MovieGridFragment.EXTRA_PLOT_SYNOPSIS);
                    ((TextView)rootView.findViewById(R.id.plotSynopsis)).setText(mPlotSynopsis);
                }

                if(intent.hasExtra(MovieGridFragment.EXTRA_USER_RATING)){
                    mUserRating = intent.getDoubleExtra(MovieGridFragment.EXTRA_USER_RATING, Double.parseDouble(getString(R.string.user_rating_default)));
                    ((TextView)rootView.findViewById(R.id.userRating)).setText(Double.valueOf(mUserRating).toString() + getString(R.string.user_rating_of_ten));
                }

                if(intent.hasExtra(MovieGridFragment.EXTRA_RELEASE_YEAR)){
                    mReleaseYear = intent.getStringExtra(MovieGridFragment.EXTRA_RELEASE_YEAR);
                    ((TextView)rootView.findViewById(R.id.releaseYear)).setText(mReleaseYear);
                }

            }

            return rootView;

        }
    }

}
