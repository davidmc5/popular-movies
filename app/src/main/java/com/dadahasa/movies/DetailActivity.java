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
import com.dadahasa.movies.model.Review;
import com.dadahasa.movies.model.ReviewResponse;
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
    private List<Review> reviewList;
    private int movieId;


    private String movieJson;

    private static final String IMAGE_URL_BASE_PATH = "http://image.tmdb.org/t/p/w342//";
    private static final String TAG = MainActivity.class.getSimpleName();

    TrailerAdapter trailerAdapter = null;
    private RecyclerView trailerRecyclerView = null;

    ReviewAdapter reviewAdapter = null;
    private RecyclerView reviewRecyclerView = null;


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
        //Retrieve trailers AND reviews from API and place them into trailerList and reviewList.
        getTrailersData(movieId);

        //TRAILERS
        //********

        // Set the trailers' recycler view
        trailerRecyclerView = findViewById(R.id.trailers_recycler_view);
        trailerRecyclerView.setHasFixedSize(true);


        //use linearLayout Manager
        trailerRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false ));

        //set recyclerView with the adapter and click listener
        if (trailerAdapter == null) {
            trailerAdapter = new TrailerAdapter(trailerList, this);
            trailerRecyclerView.setAdapter(trailerAdapter);
        }

        //REVIEWS
        //*******

        // Set the reviews' recycler view
        reviewRecyclerView = findViewById(R.id.reviews_recycler_view);
        reviewRecyclerView.setHasFixedSize(true);


        //use linearLayout Manager
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false ));

        //set recyclerView with the adapter and click listener
        if (reviewAdapter == null) {
            reviewAdapter = new ReviewAdapter(reviewList);
            reviewRecyclerView.setAdapter(reviewAdapter);
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

        //Create an instance of retrofit API service to place the calls
        MovieApiService movieApiService = retrofit.create(MovieApiService.class);


        //GET TRAILERS
        //declare the variable to get trailer data
        Call<TrailerResponse> call;

        //Get the trailers' data into an array of Trailer objects according to our data model
        call = movieApiService.getTrailers(movieId, API_KEY);
        call.enqueue(new Callback<TrailerResponse>() {

            @Override
            public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                trailerList = response.body().getResults();
                trailerAdapter.addData(trailerList);
            }

            @Override
            public void onFailure(Call<TrailerResponse> call, Throwable throwable) {
                //Log.e(TAG, throwable.toString());
                //noData();
            }
        });


        //GET REVIEWS
        //declare the variable to get review data
        Call<ReviewResponse> reviews;


        //Get the Reviews
        //Get the trailers' data into an array of Trailer objects according to our data model
        reviews = movieApiService.getReviews(movieId, API_KEY);
        reviews.enqueue(new Callback<ReviewResponse>() {

            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                reviewList = response.body().getResults();
                reviewAdapter.addData(reviewList);

                Log.d(TAG, "Number of REVIEWS received: " + reviewList.size());
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable throwable) {
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
