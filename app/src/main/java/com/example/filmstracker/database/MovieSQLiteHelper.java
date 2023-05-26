package com.example.filmstracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieSQLiteHelper extends SQLiteOpenHelper {

    String createSQL = "CREATE TABLE movie (title TEXT, imdbID TEXT PRIMARY KEY, date TEXT, city TEXT, poster TEXT, actors TEXT, director TEXT)";
    String dropSQL = "DROP TABLE IF EXISTS movie";

    public static final String DATABASE_NAME = "Movie.db";
    public static final int DATABASE_VERSION = 2;

    public MovieSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(dropSQL);
        onCreate(db);
    }

    public void insertMovie(SQLiteDatabase db, String title, String imdbID, String date, String city, String poster, String actors, String director){
        ContentValues newRecord = new ContentValues();
        newRecord.put("title", title);
        newRecord.put("imdbID", imdbID);
        newRecord.put("date", date);
        newRecord.put("city", city);
        newRecord.put("poster", poster);
        newRecord.put("actors", actors);
        newRecord.put("director", director);
        db.insert("movie", null, newRecord);
    }

    public void updateMovieByImdbID(SQLiteDatabase db, String imdbID, String newDate, String newCity) {
        ContentValues values = new ContentValues();
        values.put("date", newDate);
        values.put("city", newCity);
        String[] args = new String[] {imdbID};
        db.update("movie", values, "imdbID=?", args);
    }

    public void deleteMovieByImdbID(SQLiteDatabase db, String imdbID){
        String[] args = new String[] {imdbID};
        db.delete("movie", "imdbID=?", args);
    }

    public void deleteAllMovies(SQLiteDatabase db){
        db.delete("movie", null, null);
    }

    public Cursor getAllMovies(SQLiteDatabase db){
        String[] fields = new String[] {"title", "imdbID", "date", "city", "poster", "actors", "director"};
        Cursor c = db.query("movie", fields, null, null, null, null, null);
        return c;
    }

    public Cursor getMovieByImdbID(SQLiteDatabase db, String imdbID){
        String[] fields = new String[] {"title", "imdbID", "date", "city", "poster", "actors", "director"};
        String[] args = new String[] {imdbID};
        Cursor c =  db.query("movie", fields, "imdbID=?", args, null, null, null);
        c.moveToFirst();
        return c;
    }

    public boolean checkMovieExists(SQLiteDatabase db, String imdbID){
        String[] fields = new String[] {"title", "imdbID", "date", "city", "poster", "actors", "director"};
        String[] args = new String[] {imdbID};
        Cursor c = db.query("movie", fields, "imdbID=?", args, null, null, null);
        return c.moveToFirst();
    }

    public boolean checkTableIsEmpty(SQLiteDatabase db){
        String count = "SELECT count(*) FROM movie";
        Cursor c = db.rawQuery(count, null);
        c.moveToFirst();
        return c.getInt(0) == 0;
    }

}
