package com.dadahasa.movies;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dadahasa.movies.model.Movie;
import com.dadahasa.movies.model.Trailer;
import com.dadahasa.movies.model.TrailerResponse;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.dadahasa.movies.MainActivity.BASE_URL;

public class DetailActivity extends AppCompatActivity
implements TrailerAdapter.TrailerClickListener {

    private TextView mTitle;
    private TextView mReleaseYear;
    private TextView mRating;
    private TextView mOverview;

    private ImageView mPoster;

    private static Retrofit retrofit = null;
    private List<Trailer> trailerList;
    private int movieId;


    private String movieJson;

    private static final String IMAGE_URL_BASE_PATH = "http://image.tmdb.org/t/p/w342//";
    private static final String TAG = MainActivity.class.getSimpleName();

    TrailerAdapter trailerAdapter = null;
    private RecyclerView trailerRecyclerView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //get a reference to the textView to display the movie name
        mTitle = findViewById(R.id.title_tv);
        mReleaseYear = findViewById(R.id.releaseYear_tv);
        mRating = findViewById(R.id.rating_tv) ;
        mOverview = findViewById(R.id.overview_tv);
        mPoster = findViewById(R.id.poster_tv);

        //retrieve the intent extras
        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra("MOVIE")) {
            movieJson = intentThatStartedThisActivity.getStringExtra("MOVIE");
        }

        Gson gson = new Gson();
        Movie movieClicked = gson.fromJson(movieJson, Movie.class);

        //movie id for trailers
        movieId = movieClicked.getId();

        //title
        mTitle.setText(movieClicked.getTitle());

        //Release year
        //get the year substring (first 4 characters)
        String year = movieClicked.getReleaseDate().substring(0,4);
        mReleaseYear.setText(year);

        //Rating
        double rating = movieClicked.getVoteAverage();
        Resources res = getResources();
        String ratingStr = String.format(res.getString(R.string.rating_str), rating);
        mRating.setText(ratingStr);

        //Overview
        mOverview.setText(movieClicked.getOverview());

        String image_url = IMAGE_URL_BASE_PATH + movieClicked.getPosterPath();

        Picasso.with(this)
                .load(image_url)
                .placeholder(R.drawable.popcorn)
                .error(R.drawable.popcorn)
                .fit().centerInside()
                .into(mPoster);

        //*************************************************************
        getTrailersData(movieId);
        // Set the trailers' recycler view
        trailerRecyclerView = findViewById(R.id.detail_recycler_view);
        trailerRecyclerView.setHasFixedSize(true);

        //use linearLayout Manager
        trailerRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false ));

        //set recyclerView with the adapter and click listener
        //trailerRecyclerView.findViewHolderForAdapterPosition();
        //Context context = trailerRecyclerView.viewHolder.context;

        if (trailerAdapter == null) {
            trailerAdapter = new TrailerAdapter(trailerList, this);
            trailerRecyclerView.setAdapter(trailerAdapter);

            //Retrieve movie database data
            //getApiData();
        }





        //*************************************************************

        //This is to flag main activity after returning from  a rotation/onCreate
        // to set the new recreated adapter to show the same movie that was clicked
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);

    }


    public void getTrailersData(int movieId){

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

        //call the method defined on the MovieApiService interface to get trailer data
        MovieApiService movieApiService = retrofit.create(MovieApiService.class);
        Call<TrailerResponse> call;

        //Get the trailers' data into an array of Trailer objects according to our data model
        call = movieApiService.getTrailers(movieId, API_KEY);
        call.enqueue(new Callback<TrailerResponse>() {
            @Override
            public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                trailerList = response.body().getResults();
                trailerAdapter.addData(trailerList);

                Log.d(TAG, "Number of trailers received: " + trailerList.size());

                //restore previously visible position (before a rotation or detail view)
                //int scrollPos = pref.getInt("SCROLL_POS", 0);
                //GridLayoutManager manager = (GridLayoutManager) mRecyclerView.getLayoutManager();
                //manager.scrollToPosition(scrollPos);
            }

            @Override
            public void onFailure(Call<TrailerResponse> call, Throwable throwable) {
                //Log.e(TAG, throwable.toString());
                //noData();
            }
        });
    }

    @Override
    public void onTrailerClick(int clickedTrailerIndex) {
        // Show a Toast when an item is clicked, displaying that item number that was clicked
        String toastMessage = "Item #" + clickedTrailerIndex + " clicked.";
        Toast mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT);
        mToast.show();

    }

}
