package patel.priyanka.popularmovies;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import patel.priyanka.popularmovies.data.FavoriteContract;

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String BASE_URL = "http://api.themoviedb.org/3/movie";
    private static final String API_KEY = BuildConfig.TMDB_MOVIE_API;

    public static URL buildUrlForPopularMovies(String sortBy) {
        Uri popularMoviesUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(sortBy)
                .appendQueryParameter("api_key", API_KEY)
                .build();
        try {
            URL popularMoviesUrl = new URL(popularMoviesUri.toString());
            Log.v(TAG, "URL: " + popularMoviesUrl);
            return popularMoviesUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public  static URL buildUrlForVideosAndReviews(int movieId, String sortBy) {
        Uri movieIdDetailsUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(sortBy)
                .appendQueryParameter("api_key", API_KEY)
                .build();
        try {
            URL movieIdDetailsUrl = new URL(movieIdDetailsUri.toString());
            Log.v(TAG, "URL for movie with ID: " + movieIdDetailsUrl);
            return movieIdDetailsUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream stream = urlConnection.getInputStream();

            Scanner scanner = new Scanner(stream);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }

    public static ContentValues[] getMoviesContentValuesFromJson(String moviesJsonStr)
            throws JSONException {
        JSONObject parentObject = new JSONObject(moviesJsonStr);
        JSONArray parentArray = parentObject.getJSONArray("results");

        ContentValues[] moviesContentValues = new ContentValues[parentArray.length()];

        for (int i = 0; i < parentArray.length(); i++) {
            JSONObject finalObject = parentArray.getJSONObject(i);
            String title = finalObject.getString("title");
            String posterPath = finalObject.getString("poster_path");
            String overview = finalObject.getString("overview");
            double voteAverage = finalObject.getDouble("vote_average");
            String releaseDate = finalObject.getString("release_date");
            int movieID = finalObject.getInt("id");

            ContentValues movieValues = new ContentValues();
            movieValues.put(FavoriteContract.Favorite.COLUMN_MOVIE_TITLE, title);
            movieValues.put(FavoriteContract.Favorite.COLUMN_MOVIE_POSTER, posterPath);
            movieValues.put(FavoriteContract.Favorite.COLUMN_MOVIE_OVERVIEW, overview);
            movieValues.put(FavoriteContract.Favorite.COLUMN_MOVIE_RATING, voteAverage);
            movieValues.put(FavoriteContract.Favorite.COLUMN_MOVIE_RELEASE_DATE, releaseDate);
            movieValues.put(FavoriteContract.Favorite.COLUMN_MOVIE_ID, movieID);

            moviesContentValues[i] = movieValues;
        }

        return moviesContentValues;
    }

    public static List<VideosModel> getVideosValuesFromJson(String moviesJsonStr)
            throws JSONException {
        JSONObject videosObject = new JSONObject(moviesJsonStr);
        JSONArray videosArray = videosObject.getJSONArray("results");

        List<VideosModel> videosList = new ArrayList<>();

        for (int j = 0; j < videosArray.length(); j++) {
            JSONObject finalVideoKey = videosArray.getJSONObject(j);
            VideosModel videosModel = new VideosModel();
            videosModel.setVideoKey(finalVideoKey.getString("key"));
            videosList.add(videosModel);
        }
        return videosList;

    }

    public static List<ReviewsModel> getReviewsValueFromJson(String moviesJsonStr)
            throws JSONException {
        JSONObject reviewsObject = new JSONObject(moviesJsonStr);
        JSONArray reviewsArray = reviewsObject.getJSONArray("results");

        List<ReviewsModel> reviewsList = new ArrayList<>();

        for (int k = 0; k < reviewsArray.length(); k++) {
            JSONObject finalReviewsContent = reviewsArray.getJSONObject(k);
            ReviewsModel reviewsModel = new ReviewsModel();
            reviewsModel.setAuthor(finalReviewsContent.getString("author"));
            reviewsModel.setContent(finalReviewsContent.getString("content"));

            reviewsList.add(reviewsModel);
        }
        return reviewsList;
    }
}

