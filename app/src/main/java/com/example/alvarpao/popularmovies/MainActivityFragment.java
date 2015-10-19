package com.example.alvarpao.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.Arrays;

/**
 * A fragment containing a grid view of movie posters
 */
public class MainActivityFragment extends Fragment {

    private MoviePosterAdapter moviePosterAdapter;

    private MoviePoster[] mPosters = {
            new MoviePoster(R.drawable.poster_0), new MoviePoster(R.drawable.poster_1),
            new MoviePoster(R.drawable.poster_2), new MoviePoster(R.drawable.poster_3),
            new MoviePoster(R.drawable.poster_4), new MoviePoster(R.drawable.poster_5),
            new MoviePoster(R.drawable.poster_6), new MoviePoster(R.drawable.poster_7),
            new MoviePoster(R.drawable.poster_0), new MoviePoster(R.drawable.poster_1),
            new MoviePoster(R.drawable.poster_2), new MoviePoster(R.drawable.poster_3),
            new MoviePoster(R.drawable.poster_4), new MoviePoster(R.drawable.poster_5),
            new MoviePoster(R.drawable.poster_6), new MoviePoster(R.drawable.poster_7),
            new MoviePoster(R.drawable.poster_0), new MoviePoster(R.drawable.poster_1),
            new MoviePoster(R.drawable.poster_2), new MoviePoster(R.drawable.poster_3),
            new MoviePoster(R.drawable.poster_4), new MoviePoster(R.drawable.poster_5)

    };

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        moviePosterAdapter = new MoviePosterAdapter(getActivity(), Arrays.asList(mPosters));

        GridView moviesGridView = (GridView) rootView.findViewById(R.id.movies_grid);
        moviesGridView.setAdapter(moviePosterAdapter);

        /*
        moviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getActivity(), "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
        */

        return rootView;
    }

}
