package patel.priyanka.popularmovies.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import java.net.URL;

import patel.priyanka.popularmovies.NetworkUtils;
import patel.priyanka.popularmovies.data.FavoriteContract;

public class MoviesSyncTask {

    private static final String TAG = MoviesSyncTask.class.getSimpleName();

    synchronized public static ContentValues[] syncMovies(Context context, String sortBy) {
        ContentValues[] movieValues = new ContentValues[0];

        try {
            URL moviesRequestUrl = NetworkUtils.buildUrlForPopularMovies(sortBy);
            String moviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
            movieValues = NetworkUtils.getMoviesContentValuesFromJson(moviesResponse);
            if (movieValues == null) {
                Log.v(TAG, "JSON not loading");
            }
            if (movieValues != null && movieValues.length != 0) {
                ContentResolver moviesContentResolver = context.getContentResolver();
                moviesContentResolver.delete(
                        FavoriteContract.Favorite.CONTENT_URI,
                        null,
                        null
                );
                moviesContentResolver.bulkInsert(
                        FavoriteContract.Favorite.CONTENT_URI,
                        movieValues
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return movieValues;
    }

}
