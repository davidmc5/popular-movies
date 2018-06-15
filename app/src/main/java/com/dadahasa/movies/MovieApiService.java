package com.dadahasa.movies;

import com.dadahasa.movies.model.MovieResponse;
import com.dadahasa.movies.model.ReviewResponse;
import com.dadahasa.movies.model.TrailerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApiService {

    //To retrieve data, use this on mainActivity:
    //call = movieApiService.getTopRatedMovies(API_KEY);
    //call.enqueue(new Callback<MovieResponse>() {...
    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(@Query("api_key") String apiKey);

    //call = movieApiService.getPopularMovies(API_KEY);
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<TrailerResponse> getTrailers(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ReviewResponse> getReviews(@Path("id") int movieId, @Query("api_key") String apiKey);
}
