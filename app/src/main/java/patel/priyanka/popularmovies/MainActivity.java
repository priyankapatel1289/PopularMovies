package patel.priyanka.popularmovies;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import patel.priyanka.popularmovies.data.FavoriteContract;
import patel.priyanka.popularmovies.sync.MoviesSyncTask;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private int mPosition = RecyclerView.NO_POSITION;
    private static final int ID_MOVIE_LOADER = 44;
    private static final int ID_MOVIE_LOADER_FAVORITE = 55;
    SharedPreferences sharedPreferences;

    Parcelable mListState;
    private static final String SCROLL_POSITION = "SCROLL_POSITION";
    private static final String SORT_CRITERIA = "SORT_CRITERIA";

    public static final String[] MAIN_MOVIES = {
            FavoriteContract.Favorite.COLUMN_MOVIE_POSTER,
            FavoriteContract.Favorite.COLUMN_MOVIE_ID
    };

    public static final int INDEX_MOVIE_POSTER = 0;
    public static final int INDEX_MOVIE_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_main_activity);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, calculateNoOfColumns(this)));
        mMoviesAdapter = new MoviesAdapter(this, this);
        mRecyclerView.setAdapter(mMoviesAdapter);

        if (Build.VERSION.SDK_INT > 10) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String sortBy = sharedPreferences.getString(SORT_CRITERIA, "popular");
        if (sharedPreferences.contains(SORT_CRITERIA)) {
            MoviesSyncTask.syncMovies(this, sharedPreferences.getString(SORT_CRITERIA, sortBy));
        } else {
            MoviesSyncTask.syncMovies(this, sharedPreferences.getString(SORT_CRITERIA, sortBy));
        }

        getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mListState = savedInstanceState.getParcelable(SCROLL_POSITION);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(SCROLL_POSITION, mListState);
    }

    @Override
    public void onClick(int movieId) {
        Intent intent = new Intent(MainActivity.this, MovieDetails.class);
        Uri uriForPosterClicked = FavoriteContract.Favorite.buildMovieDetailsUri(movieId);
        intent.setData(uriForPosterClicked);
        intent.putExtra("movieId", movieId);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        CursorLoader loader;
        Uri movieQueryUri = FavoriteContract.Favorite.CONTENT_URI;
        Uri favoriteQueryUri = FavoriteContract.Favorite.CONTENT_URI_FAVORITE;

        String selection = FavoriteContract.Favorite.COLUMN_MOVIE_FAVORITE + " = " + 1;
        switch (loaderId) {
            case ID_MOVIE_LOADER: {
                loader = new CursorLoader(this,
                        movieQueryUri,
                        MAIN_MOVIES,
                        null,
                        null,
                        null);
                break;
            }
            case ID_MOVIE_LOADER_FAVORITE: {
                loader = new CursorLoader(this,
                        favoriteQueryUri,
                        MAIN_MOVIES,
                        selection,
                        null,
                        null
                );
                break;
            }

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMoviesAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.getLayoutManager().onRestoreInstanceState(mListState);
            }
        }, 300);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.swapCursor(null);
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 120;
        int noOfColumns = (int) (dpWidth / scalingFactor);
        return noOfColumns;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int id = item.getItemId();

        if (id == R.id.top_rated) {
            editor.putString(SORT_CRITERIA, "top_rated");
            editor.apply();
            getSupportLoaderManager().restartLoader(ID_MOVIE_LOADER, null, this);
            MoviesSyncTask.syncMovies(this,getString(R.string.sort_criteria_top_rated));
            return true;
        } else if (id == R.id.popular_movies) {
            editor.putString(SORT_CRITERIA, "popular");
            editor.apply();
            getSupportLoaderManager().restartLoader(ID_MOVIE_LOADER, null, this);
            MoviesSyncTask.syncMovies(this, getString(R.string.sort_criteria_popular));
            return true;
        } else if (id == R.id.menu_favorite) {
            getSupportLoaderManager().destroyLoader(ID_MOVIE_LOADER);;
            getSupportLoaderManager().initLoader(ID_MOVIE_LOADER_FAVORITE, null, this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

