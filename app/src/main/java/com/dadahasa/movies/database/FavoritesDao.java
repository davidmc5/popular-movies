package com.dadahasa.movies.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.dadahasa.movies.model.Movie;

import java.util.List;

@Dao
public interface FavoritesDao {

    //to find if a movie exists on local database (i.e., is a favorited)
    @Query("SELECT COUNT(1) FROM favorites WHERE id = :id")
    int isFavorite(int id);

    //to retrieve a list with all favorite movies
    @Query("SELECT * FROM favorites")
    List<Movie> getFavorites();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setFavorite(Movie movie);

    //All of the parameters of the Delete method must either be classes annotated with Entity
    // or collections/array of it.
    @Delete
    void clearFavorite(Movie movie);
}
