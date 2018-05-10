package com.dadahasa.movies;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.dadahasa.movies.model.Movie;
import com.dadahasa.movies.model.MovieResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity{

    MainAdapter mAdapter;
    private RecyclerView mRecyclerView = null;

    //RecyclerView.LayoutManager mLayoutManager;

    //for retrieving movie data
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String BASE_URL = "http://api.themoviedb.org/3/";
    private static Retrofit retrofit = null;

    //SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set recyclerView
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // use a gridlayout manager
        int numberOfColumns = 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        //for API
        connectAndGetApiData();
    }


    // This method creates an instance of Retrofit
    public void connectAndGetApiData(){

        //retrieve the key for the TMDb service
        final String API_KEY = getString(R.string.tmdb_key);

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        MovieApiService movieApiService = retrofit.create(MovieApiService.class);

        //Select the ranking (most popular or top rated
        Call<MovieResponse> call = movieApiService.getTopRatedMovies(API_KEY);

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                List<Movie> movies = response.body().getResults();
                mAdapter = new MainAdapter(getApplicationContext(), movies);
                mRecyclerView.setAdapter(mAdapter);
                Log.d(TAG, "Number of movies received: " + movies.size());
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable throwable) {
                Log.e(TAG, throwable.toString());
            }
        });
    }

    //the following two methods are to create the ranking selector (most popular / top rated)
    // and to respond to clicks

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ranking, menu);
        //menu.findItem(R.id.sort_setting).setVisible(true);
        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.sort_setting:
                // User chose the "Settings" item, show the app settings UI...

                if (item.getTitle().toString().equals(getString(R.string.most_popular))) {
                    item.setTitle(getString(R.string.top_rated));
                    return true;
                }
                else {
                    item.setTitle(getString(R.string.most_popular));
                    return true;
                }

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }



    }
}


