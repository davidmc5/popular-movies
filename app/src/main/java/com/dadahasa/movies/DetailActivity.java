package com.dadahasa.movies;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.dadahasa.movies.model.Movie;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private TextView mTitle;
    private TextView mReleaseYear;
    private TextView mRating;
    private TextView mOverview;

    private ImageView mPoster;


    private String movieJson;

    private static final String IMAGE_URL_BASE_PATH = "http://image.tmdb.org/t/p/w342//";

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

        //This is to flag main activity after returning from  a rotation/onCreate
        // to set the new recreated adapter to show the same movie that was clicked
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);

    }
}
