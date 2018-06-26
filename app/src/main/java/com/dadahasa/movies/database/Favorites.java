package com.dadahasa.movies.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

import com.dadahasa.movies.model.Movie;

@Database(entities = {Movie.class}, version = 1, exportSchema = false)
public abstract class Favorites extends RoomDatabase {
    private static final String LOG_TAG = Favorites.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "favorites";
    private static Favorites sInstance;

    public static Favorites getInstance(Context context) {
        if (sInstance == null) {

            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        Favorites.class, Favorites.DATABASE_NAME)
                        // Queries should be done in a separate thread to avoid locking the UI
                        // We will allow this ONLY TEMPORALLY to see that our DB is working
                        //.allowMainThreadQueries()
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract FavoritesDao favoritesDao();

}
