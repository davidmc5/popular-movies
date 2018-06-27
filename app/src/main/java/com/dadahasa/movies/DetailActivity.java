package com.dadahasa.movies;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dadahasa.movies.database.Favorites;
import com.dadahasa.movies.database.FavoritesDao;
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
    private CheckBox mFavorite;

    private static Retrofit retrofit = null;
    private List<Trailer> trailerList;
    private List<Review> reviewList;
    private int movieId;


    private String movieJson;
    private Movie movieClicked;

    private static final String IMAGE_URL_BASE_PATH = "http://image.tmdb.org/t/p/w342//";
    private static final String TAG = MainActivity.class.getSimpleName();

    TrailerAdapter trailerAdapter = null;
    private RecyclerView trailerRecyclerView = null;

    ReviewAdapter reviewAdapter = null;
    private RecyclerView reviewRecyclerView = null;

    //these declarations are used to store & retrieve
    // the trailers and reviews adapters' position
    // to restore position after rotations
    LinearLayoutManager layoutManager;
    int trailerPosition=0, reviewPosition = 0;
    public static int scrollY = 0;
    private NestedScrollView scrollView;

    private SharedPreferences pref;
    private int previousMovieID = 0;

    private Favorites mDb;




    public void setStar(int isFavorite){
        if (isFavorite == 1){
            mFavorite.setChecked(true);
        }else{
            mFavorite.setChecked(false);
        }
    }

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
        mFavorite = findViewById(R.id.favorite);

        pref =  PreferenceManager.getDefaultSharedPreferences(this);

        //Get a reference to the Room database
        mDb = Favorites.getInstance(getApplicationContext());



        //retrieve the intent extras
        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra("MOVIE")) {
            movieJson = intentThatStartedThisActivity.getStringExtra("MOVIE");
        }

        Gson gson = new Gson();
        movieClicked = gson.fromJson(movieJson, Movie.class);

        //movie id for trailers and reviews
        movieId = movieClicked.getId();

        //This is needed (and only used) to preserve movie and scroll positions during screen rotations
        if (savedInstanceState != null) {
            //On rotation the movie selected wil always be the same as previous
            //restore previous scroll positions
            trailerPosition = savedInstanceState.getInt("trailerPosition");
            reviewPosition = savedInstanceState.getInt("reviewPosition");
            scrollY = savedInstanceState.getInt("scrollY");
        }

        //Set the activity views

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

        //Set the favorite flag if movie ID is on the database.
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final int isFavorite = mDb.favoritesDao().isFavorite(movieId);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setStar(isFavorite);
                    }
                });
            }
        });


        String image_url = IMAGE_URL_BASE_PATH + movieClicked.getPosterPath();

        Picasso.with(this)
                .load(image_url)
                .placeholder(R.drawable.popcorn)
                .error(R.drawable.popcorn)
                .fit().centerInside()
                .into(mPoster);

        //*************************************************************
        //Retrieve trailers AND reviews from API and place them into trailerList and reviewList.
        getTrailersAndReviews(movieId);

        //Bind the updated trailerList and reviewList to the recyclerview adapters

        //TRAILERS
        //********

        // Set the trailers' recycler view
        trailerRecyclerView = findViewById(R.id.trailers_recycler_view);
        trailerRecyclerView.setHasFixedSize(true);

        //use linearLayout Manager
        trailerRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false ));

        //set recyclerView with the adapter and click listener and bind the trailerList data
        if (trailerAdapter == null) {
            trailerAdapter = new TrailerAdapter(trailerList, this);
            trailerRecyclerView.setAdapter(trailerAdapter);
        }

        //REVIEWS
        //*******

        // Set the reviews' recycler view
        reviewRecyclerView = findViewById(R.id.reviews_recycler_view);
        reviewRecyclerView.setHasFixedSize(true);
        reviewRecyclerView.setNestedScrollingEnabled(false);

        //use linearLayout Manager
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false ));

        //set recyclerView with the adapter and click listener and bind the reviewList data
        if (reviewAdapter == null) {
            reviewAdapter = new ReviewAdapter(reviewList);
            reviewRecyclerView.setAdapter(reviewAdapter);
        }

        //This is to flag main activity after returning from  a rotation/onCreate...
        // to set the new recreated adapter to show the same movie that was clicked
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);



        // When toggle Favorite Star Button, save or remove movie from DB
        mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFavorite = mFavorite.isChecked();
                toggleFavorite(isFavorite, movieClicked);

                if (isFavorite) {
                    String toastMessage = "Added to Favorites";
                    Toast mToast = Toast.makeText(DetailActivity.this, toastMessage, Toast.LENGTH_SHORT);
                    mToast.show();
                }else {
                    String toastMessage = "Removed from Favorites";
                    Toast mToast = Toast.makeText(DetailActivity.this, toastMessage, Toast.LENGTH_SHORT);
                    mToast.show();
                }
            }
        });
    }


    public void toggleFavorite(final boolean isFavorite, final Movie movieClicked) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (isFavorite) {
                    mDb.favoritesDao().setFavorite(movieClicked);
                } else {
                    mDb.favoritesDao().clearFavorite(movieClicked);
                }
            }
        });
    }



    @Override
    protected void onPause(){
        super.onPause();
        //get current trailers' adapter position to retrieve after rotation
        //then save it on shared preferences
        layoutManager = (LinearLayoutManager) trailerRecyclerView.getLayoutManager();
        trailerPosition = layoutManager.findFirstCompletelyVisibleItemPosition();

        //save current reviews' adapter position to retrieve after rotation
        layoutManager = (LinearLayoutManager) reviewRecyclerView.getLayoutManager();
        reviewPosition = layoutManager.findFirstCompletelyVisibleItemPosition();

        //record the current Y position of the scrollView for rotation
        //it will be save on the bundle with onSaveInstanceState method
        scrollView = findViewById(R.id.scrollViewId);
        scrollY = scrollView.getScrollY();

        previousMovieID = movieId;

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("PREVIOUS_MOVIE", movieId);
        editor.putInt("TRAILER_POSITION", trailerPosition);
        editor.apply();
    }


    @Override
    protected void onResume(){
        super.onResume();
        //Restore variables after clicking back to movie detail
        previousMovieID = pref.getInt("PREVIOUS_MOVIE", 0);
        trailerPosition = pref.getInt("TRAILER_POSITION", 0);

        if (movieId != previousMovieID) {
            scrollY = 0;
            trailerPosition = 0;
            reviewPosition = 0;
        }
    }

    //The next two methods are only called during rotations
    //or only the very first time a movie is clicked!
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("trailerPosition", trailerPosition);
        outState.putInt("reviewPosition", reviewPosition);
        outState.putInt("scrollY", scrollY);
        outState.putInt("previousMovieID", movieId);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        trailerPosition = savedInstanceState.getInt("trailerPosition");
        reviewPosition = savedInstanceState.getInt("reviewPosition");
        scrollY = savedInstanceState.getInt("scrollY");
    }


    public void getTrailersAndReviews(int movieId){

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

                //restore previously visible position (before a rotation or detail view)
                layoutManager = (LinearLayoutManager) trailerRecyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(trailerPosition, 0);
                Log.d(TAG, "TRAILERS RECEIVED -------------->: " + trailerList.size());
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

        //Get the Reviews' data into an array of Review objects according to our data model
        reviews = movieApiService.getReviews(movieId, API_KEY);
        reviews.enqueue(new Callback<ReviewResponse>() {

            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                reviewList = response.body().getResults();
                reviewAdapter.addData(reviewList);

                //restore Reviews scroll position
                layoutManager = (LinearLayoutManager) reviewRecyclerView.getLayoutManager();
                layoutManager.scrollToPosition(reviewPosition);


                //since we now have retrieved and laid out all the data, we can restore
                // the original scroll position
                scrollView = findViewById(R.id.scrollViewId);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.scrollTo(0, scrollY);
                        //scrollView.scrollTo(0, 0);
                    }
                });
                //Log.d(TAG, "Number of REVIEWS received: " + reviewList.size());
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable throwable) {
                Log.e(TAG, throwable.toString());
            }
        });
    }

    @Override
    public void onTrailerClick(int clickedTrailerIndex) {

        //retrieve trailer
        Trailer trailer =  trailerList.get(clickedTrailerIndex);
        String trailerKey = trailer.getKey();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("www.youtube.com")
                .appendPath("watch")
                .appendQueryParameter("v", trailerKey);

        String trailerUrl = builder.build().toString();
        Uri trailerWebpage = Uri.parse(trailerUrl);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, trailerWebpage);
        startActivity(webIntent);

        // Show a Toast when an item is clicked, displaying that item number that was clicked
        //String toastMessage = "Item #" + clickedTrailerIndex + " clicked.";
        /*
        String toastMessage = "URL = " + trailerUrl;
        Toast mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT);
        mToast.show();
        */



    }



}
