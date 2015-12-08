package com.example.alvarpao.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by alvarpao on 12/3/2015.
 * A custom adapter for the trailer's RecyclerView in the movie detailed view
 */

public class MovieDetailsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MOVIE_DETAILS_HEADER = 0;
    private static final int TYPE_TRAILER_ITEM = 1;

    private Movie mMovie;
    private List<Trailer> mTrailerList;
    private Context mContext;

    View.OnClickListener trailerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TrailerViewHolder holder = (TrailerViewHolder) view.getTag();
            int position = holder.getAdapterPosition();
            Trailer trailer = mTrailerList.get(position-1);
            Toast.makeText(mContext, trailer.getName(), Toast.LENGTH_SHORT).show();
        }
    };

    public MovieDetailsRecyclerAdapter(Context context, Movie movie, List<Trailer> trailerList) {
        this.mContext = context;
        this.mMovie = movie;
        this.mTrailerList = trailerList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == TYPE_MOVIE_DETAILS_HEADER){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_detail_header_item, parent, false);
            return new MovieDetailsHeaderViewHolder(view);
        }

        else if(viewType == TYPE_TRAILER_ITEM){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_item, parent, false);
            return new TrailerViewHolder(view);
        }

        throw new RuntimeException(mContext.getString(R.string.no_match_view_type)+ viewType);
    }

    private Trailer getTrailerItem(int position)
    {
        return mTrailerList.get(position);
    }

    private String buildThumbnailURL(String source)
    {
        // This class builds the URL to the thumbnail for a specific trailer, to be displayed
        // in the RecyclerView

        // Check Stack Overflow forum post for more info:
        // http://stackoverflow.com/questions/2068344/how-do-i-get-a-youtube-video-thumbnail-from-the-youtube-api
        // or YouTube API: https://developers.google.com/youtube/v3/docs/thumbnails
        return "http://i1.ytimg.com/vi/" + source + "/default.jpg";
    }

    private void checkIfFavorite() {

        // Query the local database to determine if the movie in the current detail view is
        // in the user's favorite list. If so, change the image of the "Mark Favorite" button
        // to "Favorite" and change the onClick method appropriate to delete the movie from
        // the database if clicked again.
        QueryIfFavoriteMovieTask queryIfFavoriteMovieTask =
                new QueryIfFavoriteMovieTask(mContext);
        queryIfFavoriteMovieTask.execute(mMovie);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        if(viewHolder instanceof MovieDetailsHeaderViewHolder){

            // Decimal format for user rating
            DecimalFormat decimalFormat = new DecimalFormat("##.#");
            decimalFormat.setRoundingMode(RoundingMode.HALF_UP);

            MovieDetailsHeaderViewHolder viewHolderHeader = (MovieDetailsHeaderViewHolder)viewHolder;

            //Using Picasso open source library to facilitate loading images and caching
            Picasso.with(mContext)
                    .load(mMovie.getImageURL())
                    .placeholder(R.drawable.image_loading)
                    .error(R.drawable.error_loading_image)
                    .into(viewHolderHeader.moviePoster);
            viewHolderHeader.originalTitle.setText(mMovie.getOriginalTitle());
            viewHolderHeader.releaseYear.setText(mMovie.getReleaseYear());
            viewHolderHeader.runtime.setText((new Integer(mMovie.getRuntime())).toString() +
                    mContext.getString(R.string.runtime_units));
            viewHolderHeader.userRating.setText(
                    Double.valueOf(decimalFormat.format(mMovie.getUserRating())).toString() +
                            mContext.getString(R.string.user_rating_of_ten));
            viewHolderHeader.plotSynopsis.setText(mMovie.getPlotSynopsis());

            viewHolderHeader.imgBtnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MovieDetailActivity) mContext).saveFavoriteMovieDetails(mMovie);
                }
            });

            checkIfFavorite();

        }

        else if(viewHolder instanceof TrailerViewHolder) {

            Trailer trailer = getTrailerItem(position - 1);
            TrailerViewHolder trailerViewHolder = (TrailerViewHolder)viewHolder;

            String thumbnailURL = buildThumbnailURL(trailer.getSource());
            // Load the image using picasso library
            Picasso.with(mContext).load(thumbnailURL)
                    .placeholder(R.drawable.image_loading)
                    .error(R.drawable.error_loading_image)
                    .into(trailerViewHolder.trailerThumbnail);

            // Setting the trailer's title
            trailerViewHolder.trailerTitle.setText(Html.fromHtml(trailer.getName()));

            // Handle click event on both the trailer's title and trailer thumbnail
            trailerViewHolder.trailerTitle.setOnClickListener(trailerClickListener);
            trailerViewHolder.trailerThumbnail.setOnClickListener(trailerClickListener);

            trailerViewHolder.trailerTitle.setTag(trailerViewHolder);
            trailerViewHolder.trailerThumbnail.setTag(trailerViewHolder);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position))
            return TYPE_MOVIE_DETAILS_HEADER;
        return TYPE_TRAILER_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemCount() {
        return mTrailerList.size() + 1;
    }

    public void addTrailerItem(int position, Trailer trailer) {
        mTrailerList.add(position, trailer);
        notifyItemInserted(position);
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder {
        ImageView trailerThumbnail;
        TextView trailerTitle;

        public TrailerViewHolder(View view) {
            super(view);
            this.trailerThumbnail = (ImageView) view.findViewById(R.id.trailerThumbnail);
            this.trailerTitle = (TextView) view.findViewById(R.id.trailerTitle);
        }
    }

    class MovieDetailsHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView originalTitle;
        ImageView moviePoster;
        TextView releaseYear;
        TextView runtime;
        TextView userRating;
        ImageButton imgBtnFavorite;
        TextView plotSynopsis;

        public MovieDetailsHeaderViewHolder(View view) {
            super(view);
            this.originalTitle = (TextView) view.findViewById(R.id.originalTitle);
            this.moviePoster = (ImageView) view.findViewById(R.id.moviePoster);
            this.releaseYear = (TextView) view.findViewById(R.id.releaseYear);
            this.runtime = (TextView) view.findViewById(R.id.runtime);
            this.userRating = (TextView) view.findViewById(R.id.userRating);
            this.imgBtnFavorite = (ImageButton) view.findViewById(R.id.imgBtnFavorite);
            this.plotSynopsis = (TextView) view.findViewById(R.id.plotSynopsis);
        }
    }
}
