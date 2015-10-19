package com.example.alvarpao.popularmovies;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a GridView
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView moviesGridView = (GridView) rootView.findViewById(R.id.movies_grid);
        moviesGridView.setAdapter(new ImageAdapter(getActivity()));

        moviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getActivity(), "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    // Custom adapter that will act as the source for all images displayed in the GridView
    private class ImageAdapter extends BaseAdapter {

        private Context mContext;
        private Integer[] mPosterIds = {
                R.drawable.poster_0, R.drawable.poster_1,
                R.drawable.poster_2, R.drawable.poster_3,
                R.drawable.poster_4, R.drawable.poster_5,
                R.drawable.poster_6, R.drawable.poster_7,
                R.drawable.poster_0, R.drawable.poster_1,
                R.drawable.poster_2, R.drawable.poster_3,
                R.drawable.poster_4, R.drawable.poster_5,
                R.drawable.poster_6, R.drawable.poster_7,
                R.drawable.poster_0, R.drawable.poster_1,
                R.drawable.poster_2, R.drawable.poster_3,
                R.drawable.poster_4, R.drawable.poster_5

        };

        public ImageAdapter(Context context) {
            mContext = context;
        }

        public int getCount(){
            return mPosterIds.length;
        }

        public ImageView getItem(int position){
            return null;
        }

        public long getItemId(int position){
            return 0;
        }

        // Create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;

            if(convertView == null){
               // If it is not recycled, initialize image attibutes

                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                //imageView.setPadding(8, 8, 8, 8);
            }

            else{
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mPosterIds[position]);
            return imageView;
        }

    }
}
