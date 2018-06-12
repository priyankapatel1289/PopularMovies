package patel.priyanka.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MoviesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "favoriteMovies.db";
    private static final int DATABASE_VERSION = 11;

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITE_MOVIES_TABLE = "CREATE TABLE " +
                FavoriteContract.Favorite.TABLE_NAME + " (" +
                FavoriteContract.Favorite._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavoriteContract.Favorite.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                FavoriteContract.Favorite.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                FavoriteContract.Favorite.COLUMN_MOVIE_POSTER + " TEXT NOT NULL, " +
                FavoriteContract.Favorite.COLUMN_MOVIE_OVERVIEW + " TEXT, " +
                FavoriteContract.Favorite.COLUMN_MOVIE_RATING + " INTEGER, " +
                FavoriteContract.Favorite.COLUMN_MOVIE_RELEASE_DATE + " TEXT, " +
                FavoriteContract.Favorite.COLUMN_MOVIE_FAVORITE + " INTEGER " +
                ");";
        db.execSQL(SQL_CREATE_FAVORITE_MOVIES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteContract.Favorite.TABLE_NAME);
        onCreate(db);

    }
}
