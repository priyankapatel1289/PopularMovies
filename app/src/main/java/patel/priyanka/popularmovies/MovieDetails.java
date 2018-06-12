package patel.priyanka.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

import patel.priyanka.popularmovies.data.FavoriteContract;
import patel.priyanka.popularmovies.data.MoviesDbHelper;

public class MovieDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private SQLiteDatabase mDb;
    private MoviesDbHelper dbHelper;

    private List<ReviewsModel> reviewsList;
    private List<VideosModel> videosList;

    private int movieId;
    private String title;
    private String posterPath;
    public int favorite;
    public int rowID;

    private ImageView mPosterView;
    private TextView mTitleView;
    private TextView mVoteAverageView;
    private TextView mReleaseDateView;
    private TextView mOverviewView;
    private ImageButton mFavoriteButton;
    private TextView reviews;
    private RecyclerView mReviewsRecyclerView;
    private ReviewsAdapter mReviewsAdapter;
    private RecyclerView mVideosRecyclerView;
    private VideosAdapter mVideosAdapter;
    private boolean isSelected;

    private static final String[] MOVIE_DETAIL = {
            FavoriteContract.Favorite.COLUMN_MOVIE_ID,
            FavoriteContract.Favorite.COLUMN_MOVIE_TITLE,
            FavoriteContract.Favorite.COLUMN_MOVIE_POSTER,
            FavoriteContract.Favorite.COLUMN_MOVIE_RELEASE_DATE,
            FavoriteContract.Favorite.COLUMN_MOVIE_RATING,
            FavoriteContract.Favorite.COLUMN_MOVIE_OVERVIEW,
            FavoriteContract.Favorite.COLUMN_MOVIE_FAVORITE,
            FavoriteContract.Favorite._ID
    };

    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_TITLE = 1;
    public static final int INDEX_MOVIE_POSTER = 2;
    public static final int INDEX_MOVIE_RELEASE_DATE = 3;
    public static final int INDEX_MOVIE_RATING = 4;
    public static final int INDEX_MOVIE_OVERVIEW = 5;
    public static final int INDEX_MOVIE_FAVORITE = 6;
    public static final int INDEX_MOVIE_ROW_ID = 7;

    private static final int ID_DETAILS_LOADER = 630;
    private Uri mUri;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mPosterView = (ImageView) findViewById(R.id.iv_movie_poster);
        mTitleView = (TextView) findViewById(R.id.tv_movie_title);
        mVoteAverageView = (TextView) findViewById(R.id.tv_vote_average);
        mReleaseDateView = (TextView) findViewById(R.id.tv_release_date);
        mOverviewView = (TextView) findViewById(R.id.tv_overview);
        reviews = (TextView) findViewById(R.id.tv_reviews);
        mFavoriteButton = (ImageButton) findViewById(R.id.ic_favorite);

        mReviewsRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_reviews);
        mReviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mReviewsRecyclerView.setNestedScrollingEnabled(true);

        mVideosRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_videos);
        mVideosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            mVideosRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
//        } else {
//            mVideosRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
//        }
        mVideosRecyclerView.setNestedScrollingEnabled(true);

        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        dbHelper = new MoviesDbHelper(this);
        mDb = dbHelper.getWritableDatabase();

        getSupportLoaderManager().initLoader(ID_DETAILS_LOADER, null, this);

        setFavorite();
    }

    public void insertMovies(Context context) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoriteContract.Favorite.COLUMN_MOVIE_ID, movieId);
        contentValues.put(FavoriteContract.Favorite.COLUMN_MOVIE_TITLE, title);
        contentValues.put(FavoriteContract.Favorite.COLUMN_MOVIE_POSTER, posterPath);
        contentValues.put(FavoriteContract.Favorite.COLUMN_MOVIE_FAVORITE, 1);

        ContentResolver moviesResolver = context.getContentResolver();
        moviesResolver.insert(
                FavoriteContract.Favorite.CONTENT_URI_FAVORITE,
                contentValues);
    }

    public void deleteMovies(Context context) {
        ContentResolver moviesResolver = context.getContentResolver();
        moviesResolver.delete(
                FavoriteContract.Favorite.CONTENT_URI_FAVORITE,
                FavoriteContract.Favorite._ID + "=" + rowID,
                null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case ID_DETAILS_LOADER:
                return new CursorLoader(this,
                        mUri,
                        MOVIE_DETAIL,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        }
        if (!cursorHasValidData) {
            return;
        }
        movieId = data.getInt(INDEX_MOVIE_ID);
        getVideosData(movieId, "videos");
        getReviewsData(movieId, "reviews");

        title = data.getString(INDEX_MOVIE_TITLE);
        mTitleView.setText(title);

        posterPath = data.getString(INDEX_MOVIE_POSTER);
        Picasso.with(this).load("https://image.tmdb.org/t/p/w185" + posterPath).error(android.R.drawable.stat_notify_error).into(mPosterView);

        mReleaseDateView.setText(data.getString(INDEX_MOVIE_RELEASE_DATE));
        mVoteAverageView.setText(data.getString(INDEX_MOVIE_RATING));
        mOverviewView.setText(data.getString(INDEX_MOVIE_OVERVIEW));

        favorite = data.getInt(INDEX_MOVIE_FAVORITE);
        if (favorite != 0) {
            mFavoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.star_yellow));
            isSelected = true;
        }

        rowID = data.getInt(INDEX_MOVIE_ROW_ID);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void setFavorite() {
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSelected) {
                    mFavoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(MovieDetails.this, R.drawable.star_yellow));
                    Toast.makeText(MovieDetails.this, R.string.movie_added_to_favorite, Toast.LENGTH_SHORT).show();
                    insertMovies(MovieDetails.this);
                    isSelected = true;
                } else {
                    mFavoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(MovieDetails.this, R.drawable.star));
                    Toast.makeText(MovieDetails.this, R.string.movie_removed_from_favorite, Toast.LENGTH_SHORT).show();
                    deleteMovies(MovieDetails.this);
                    isSelected = false;
                }
            }
        });
    }

    public void getVideosData(int movieID, String sortBy) {
        try {
            URL urlForVideos = NetworkUtils.buildUrlForVideosAndReviews(movieID, sortBy);
            String videosResponse = NetworkUtils.getResponseFromHttpUrl(urlForVideos);
            videosList = NetworkUtils.getVideosValuesFromJson(videosResponse);
            mVideosAdapter = new VideosAdapter(videosList);
            mVideosRecyclerView.setAdapter(mVideosAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getReviewsData(int movieID, String sortBy) {
        try {
            URL urlForReviews = NetworkUtils.buildUrlForVideosAndReviews(movieID, sortBy);
            String reviewsResponse = NetworkUtils.getResponseFromHttpUrl(urlForReviews);
            reviewsList = NetworkUtils.getReviewsValueFromJson(reviewsResponse);
            mReviewsAdapter = new ReviewsAdapter(reviewsList);
            mReviewsRecyclerView.setAdapter(mReviewsAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


