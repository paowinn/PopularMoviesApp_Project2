package com.example.alvarpao.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.Arrays;

/**
 * A fragment containing a grid view of movie posters
 */
public class MainActivityFragment extends Fragment {

    private MoviePosterAdapter mPosterAdapter;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add the below line in order for this fragment to display and handle menu options.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            Toast.makeText(getActivity(), "Refresh", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mPosterAdapter = new MoviePosterAdapter(getActivity(), Arrays.asList(mPosters));

        GridView moviesGridView = (GridView) rootView.findViewById(R.id.movies_grid);
        moviesGridView.setAdapter(mPosterAdapter);

        moviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                Toast.makeText(getActivity(), "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });


        return rootView;
    }

}
