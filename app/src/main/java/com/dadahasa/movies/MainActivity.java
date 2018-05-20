package com.dadahasa.movies;


import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dadahasa.movies.model.Movie;
import com.dadahasa.movies.model.MovieResponse;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
implements MainAdapter.MovieClickListener {

    MainAdapter mAdapter;
    private RecyclerView mRecyclerView = null;

    //for retrieving movie data
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String BASE_URL = "http://api.themoviedb.org/3/";
    private static Retrofit retrofit = null;

    private List<Movie> movieList;

    //to store user selections
    private SharedPreferences pref;
    String myPreference; //(most popular or top rated)

    public static final String SORT_KEY = "sortKey";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //set recyclerView
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // use a gridlayout manager with two columns
        int numberOfColumns = 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        //save selected sorting option (most popular or top rated)
        //first get a reference to the default shared preferences file
        pref =  PreferenceManager.getDefaultSharedPreferences(this);

        //retrieve the current preference value or, if null, set initial value to most_popular
        myPreference = pref.getString(SORT_KEY, getString(R.string.most_popular));

        //store preference back in case it was originally null
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(SORT_KEY, myPreference);
        editor.apply();

        //set recyclerView with the adapter and click listener
        if (mAdapter == null) {
            mAdapter = new MainAdapter(getApplicationContext(), movieList, this);
            mRecyclerView.setAdapter(mAdapter);

            //Retrieve movie database data
            getApiData();
        }
    }


    // Method to retrieve movie database data
    // This method creates an instance of Retrofit
    public void getApiData(){

        //retrieve the key for the TMDb service
        //the key is stored as a string in an XML file set in .gitignore
        final String API_KEY = getString(R.string.tmdb_key);

        //create an instance of the API using retrofit
        // this instance will handle the REST/JSON requests to the TMDb service
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        //use the methods of the MovieApiService interface to get movie data
        //sorted according to the user's preference
        MovieApiService movieApiService = retrofit.create(MovieApiService.class);
        Call<MovieResponse> call;

        //Set the sort order based on myPreference (most popular or top rated)
        if (myPreference.equals(getString(R.string.top_rated))){
            call = movieApiService.getTopRatedMovies(API_KEY);
        }else{
            call = movieApiService.getPopularMovies(API_KEY);
        }
        //when re-sorting, go back to show movie list from index 0
        GridLayoutManager manager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        manager.scrollToPosition(0);

        //Get the movie list into an array of movie objects according to our data model
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                movieList = response.body().getResults();
                mAdapter.addData(movieList);
                Log.d(TAG, "Number of movies received: " + movieList.size());
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable throwable) {
                Log.e(TAG, throwable.toString());
            }
        });
    }

    //the following two methods are to create the ranking selector (most popular / top rated)
    //displayed as a the preference in the actionBar, and to respond to clicks
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //need to inflate the sort-by menu item to toggle its label
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ranking, menu);
        MenuItem mMenu = menu.findItem(R.id.sort_setting);
        mMenu.setTitle(myPreference);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.sort_setting:
                // User clicked at the "sorted by" item. Toggle sort criteria
                if (item.getTitle().toString().equals(getString(R.string.most_popular))) {
                    myPreference = getString(R.string.top_rated);
                }
                else {
                    myPreference = getString(R.string.most_popular);
                }
                item.setTitle(myPreference);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(SORT_KEY, myPreference);
                editor.apply();

                //grab and display a new set of posters sort-by the new criteria
                getApiData();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMovieClick(int clickedMovieIndex) {

        //Open movie detail activity
        Intent startDetailActivityIntent = new Intent(this, DetailActivity.class);

        // Get the movie clicked
        Movie movieClicked = movieList.get(clickedMovieIndex);

        Gson gson = new Gson();
        String movieJson = gson.toJson(movieClicked);
        startDetailActivityIntent.putExtra("MOVIE", movieJson);

        //start detail activity
        startActivity(startDetailActivityIntent);
    }
}


