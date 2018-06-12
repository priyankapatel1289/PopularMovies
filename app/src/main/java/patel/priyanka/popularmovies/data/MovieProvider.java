package patel.priyanka.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static patel.priyanka.popularmovies.data.FavoriteContract.Favorite.TABLE_NAME;


public class MovieProvider extends ContentProvider {


    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIE_ID = 101;
    public static final int CODE_MOVIE_FAVORITE = 102;
    private MoviesDbHelper mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoriteContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, FavoriteContract.PATH_MOVIE, CODE_MOVIES);
        matcher.addURI(authority, FavoriteContract.PATH_MOVIE + "/#", CODE_MOVIE_ID);
        matcher.addURI(authority, FavoriteContract.PATH_FAVORITE_MOVIE, CODE_MOVIE_FAVORITE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        String[] result_columns = new String[] {FavoriteContract.Favorite.COLUMN_MOVIE_POSTER, FavoriteContract.Favorite.COLUMN_MOVIE_ID};
        String[] details_columns = new String[] {FavoriteContract.Favorite.COLUMN_MOVIE_ID,
                FavoriteContract.Favorite.COLUMN_MOVIE_TITLE, FavoriteContract.Favorite.COLUMN_MOVIE_POSTER,
                FavoriteContract.Favorite.COLUMN_MOVIE_RATING, FavoriteContract.Favorite.COLUMN_MOVIE_RELEASE_DATE,
                FavoriteContract.Favorite.COLUMN_MOVIE_OVERVIEW, FavoriteContract.Favorite.COLUMN_MOVIE_FAVORITE, FavoriteContract.Favorite._ID};
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIE_ID:
            {
                String normalizedPosterString = uri.getLastPathSegment();
                String[] selectionArgument = new String[] {normalizedPosterString};
                cursor = mOpenHelper.getReadableDatabase().query(
                        FavoriteContract.Favorite.TABLE_NAME,
                        details_columns,
                        FavoriteContract.Favorite.COLUMN_MOVIE_ID + " = ? ",
                        selectionArgument,
                        null,
                        null,
                        null,
                        null
                );
                break;
            }
            case CODE_MOVIES:
            {
                cursor = mOpenHelper.getReadableDatabase().query(
                        FavoriteContract.Favorite.TABLE_NAME,
                        result_columns,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                break;
            }
            case CODE_MOVIE_FAVORITE:
            {
                cursor = mOpenHelper.getReadableDatabase().query(
                        FavoriteContract.Favorite.TABLE_NAME,
                        result_columns,
                        FavoriteContract.Favorite.COLUMN_MOVIE_FAVORITE + "=" + 1,
                        null,
                        null,
                        null,
                        null
                );
                break;
            }
            default:
                throw new RuntimeException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        switch (match) {
            case CODE_MOVIES:
                int rowsInsesrted = 0;
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(FavoriteContract.Favorite.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInsesrted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (rowsInsesrted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInsesrted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CODE_MOVIE_FAVORITE:
                db.beginTransaction();
                long movieId = db.insert(TABLE_NAME, null, values);
                if (movieId > 0) {
                    returnUri = ContentUris.withAppendedId(FavoriteContract.Favorite.CONTENT_URI_FAVORITE, movieId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int numberOfRowsDeleted;

        if (null == selection) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                numberOfRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        FavoriteContract.Favorite.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case CODE_MOVIE_FAVORITE:
                numberOfRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        FavoriteContract.Favorite.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numberOfRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numberOfRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

}

