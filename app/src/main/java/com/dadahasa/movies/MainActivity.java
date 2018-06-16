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

import android.widget.Toast;

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

    MainAdapter mAdapter = null;
    private RecyclerView mRecyclerView = null;

    //for retrieving movie data
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String BASE_URL = "http://api.themoviedb.org/3/";
    private static Retrofit retrofit = null;

    private List<Movie> movieList;

    //to store user selections
    private SharedPreferences pref;
    String myPreference; //(most popular or top rated)

    GridLayoutManager manager;

    private static boolean isFirstRun = true;

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

        //store sorting preference back in case it was originally null
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(SORT_KEY, myPreference);
        editor.apply();

        if (isFirstRun){
            //reset sharedPreferences when the app starts for the first time
            editor.putInt("SCROLL_POS", 0);
            editor.apply();
            isFirstRun = false;
        }

        //set recyclerView with the adapter and click listener
        if (mAdapter == null) {
            mAdapter = new MainAdapter(getApplicationContext(), movieList, this);
            mRecyclerView.setAdapter(mAdapter);

            //Retrieve movie database data
            getApiData();
        }
    }



    @Override
    protected void onPause() {
        super.onPause();

        //save current recyclerView position
        manager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        int firstItem = manager.findFirstVisibleItemPosition();

        if (firstItem != -1) {
            //Preserve first visible view in case of device rotation
            //to be able to scroll back the the current visible row
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("SCROLL_POS", firstItem);
            editor.apply();
        }else{
            //We couldn't retrieve data
            noData();
        }
    }



    //the following two methods are to create the sorting selector (most popular / top rated)
    //display as a the preference in the actionBar, and to respond to clicks
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

                //since we re-sorted, display from the top,  view index 0
                editor.putInt("SCROLL_POS", 0);
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

        //save the position index that was clicked
        //to scroll back to the same movie list row
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("CLICKED_POS", clickedMovieIndex);
        editor.apply();


        //Start movie detail activity
        //convert the selected movie object to JSON to pass it to the detail activity via intent
        Movie movieClicked = movieList.get(clickedMovieIndex);
        Gson gson = new Gson();
        String movieJson = gson.toJson(movieClicked);

        //create intent sending the movie object as extra
        Intent startDetailActivityIntent = new Intent(this, DetailActivity.class);
        startDetailActivityIntent.putExtra("MOVIE", movieJson);

        //start detail activity
        //We use the "forResult" version to call onActivityResults to set the movie clicked
        startActivityForResult(startDetailActivityIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                //store the adapter position for the movie clicked
                // to scroll back to the same movie
                //Since after a rotation MainActivity is recreated with a new adapter in position 0
                // that zero index value got stored.
                //we need to overwrite the previously stored adapter position
                //with the position of the movie clicked

                SharedPreferences.Editor editor = pref.edit();
                int clickedPos = pref.getInt("CLICKED_POS", 0);
                editor.putInt("SCROLL_POS", clickedPos);
                editor.apply();
            }
        }
    }

    public void noData(){
        String toastMessage = "Data unavailable. Verify Wi-Fi/Cellular Network";
        Toast mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();
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

        //Get the movie list into an array of movie objects according to our data model
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                movieList = response.body().getResults();
                mAdapter.addData(movieList);
                Log.d(TAG, "Number of movies received: " + movieList.size());

                //restore previously visible position (before a rotation or detail view)
                int scrollPos = pref.getInt("SCROLL_POS", 0);
                GridLayoutManager manager = (GridLayoutManager) mRecyclerView.getLayoutManager();
                manager.scrollToPosition(scrollPos);
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable throwable) {
                Log.e(TAG, throwable.toString());
                noData();
            }
        });
    }
}



