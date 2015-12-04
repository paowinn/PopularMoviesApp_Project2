package com.example.alvarpao.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by alvarpao on 12/3/2015.
 * A custom adapter for the trailer's RecyclerView in the movie detailed view
 */

public class TrailerRecyclerAdapter extends RecyclerView.Adapter<TrailerRecyclerAdapter.CustomViewHolder> {

    private List<Trailer> mTrailerList;
    private Context mContext;

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CustomViewHolder holder = (CustomViewHolder) view.getTag();
            int position = holder.getPosition();
            Trailer trailer = mTrailerList.get(position);
            Toast.makeText(mContext, trailer.getName(), Toast.LENGTH_SHORT).show();
        }
    };

    public TrailerRecyclerAdapter(Context context, List<Trailer> trailerList) {
        this.mTrailerList = trailerList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_row, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
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

    @Override
    public void onBindViewHolder(CustomViewHolder viewHolder, int position) {

        Trailer trailer = mTrailerList.get(position);

        String thumbnailURL = buildThumbnailURL(trailer.getSource());
        // Load the image using picasso library
        Picasso.with(mContext).load(thumbnailURL)
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.error_loading_image)
                .into(viewHolder.trailerThumbnail);

        // Setting the trailer's title
        viewHolder.trailerTitle.setText(Html.fromHtml(trailer.getName()));

        // Handle click event on both the trailer's title and trailer thumbnail
        viewHolder.trailerTitle.setOnClickListener(clickListener);
        viewHolder.trailerThumbnail.setOnClickListener(clickListener);

        viewHolder.trailerTitle.setTag(viewHolder);
        viewHolder.trailerThumbnail.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return mTrailerList.size();
    }

    public void addItem(int position, Trailer trailer) {
        mTrailerList.add(position, trailer);
        notifyItemInserted(position);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView trailerThumbnail;
        protected TextView trailerTitle;

        public CustomViewHolder(View view) {
            super(view);
            this.trailerThumbnail = (ImageView) view.findViewById(R.id.trailerThumbnail);
            this.trailerTitle = (TextView) view.findViewById(R.id.trailerTitle);
        }
    }
}
