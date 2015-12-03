package com.example.alvarpao.popularmovies;

/**
 * Created by alvarpao on 12/2/2015.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

        }

        mBtnFavorite = (ImageButton)rootView.findViewById(R.id.imgBtnFavorite);
        mBtnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MovieDetailActivity)getActivity()).saveFavoriteMovieDetails(mMovie);
            }
        });


        // Query the local database to determine if the movie in the current detail view is
        // in the user's favorite list
        checkIfFavorite();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getExtraMovieInfo();
    }

    private void checkIfFavorite() {

        // Query the local database to determine if the movie in the current detail view is
        // in the user's favorite list. If so, change the image of the "Mark Favorite" button
        // to "Favorite" and change the onClick method appropriate to delete the movie from
        // the database if clicked again.
        QueryIfFavoriteMovieTask queryIfFavoriteMovieTask =
                new QueryIfFavoriteMovieTask(getActivity());
        queryIfFavoriteMovieTask.execute(mMovie);

    }

    private void getExtraMovieInfo() {
        // Fetch the runtime, trailers and reviews for the movie currently in the detail
        // view
        FetchExtraMovieInfoTask fetchExtraMovieInfoTask = new FetchExtraMovieInfoTask(getActivity());
        fetchExtraMovieInfoTask.execute(mMovie);
    }
}
