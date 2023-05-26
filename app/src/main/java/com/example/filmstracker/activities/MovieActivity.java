package com.example.filmstracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filmstracker.database.MovieSQLiteHelper;
import com.example.filmstracker.R;
import com.example.filmstracker.webservices.Repository;
import com.example.filmstracker.models.Movie;
import com.example.filmstracker.ui.dialog.DatePickerFragment;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieActivity extends AppCompatActivity {

    Repository repository;
    EditText dateEditText;
    MovieSQLiteHelper mdbh;
    SQLiteDatabase db;
    Movie movie;
    String city;
    String date;
    boolean search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        repository = new Repository();

        // Initialize SQLite helper class
        mdbh = new MovieSQLiteHelper(this);

        Intent intent = getIntent();

        search = intent.getBooleanExtra("search", false);

        // This MovieActivity is used both when we are displaying a search result (that could be then saved in the database)
        // and when we open a saved movie from the Movies list
        if (search) {
            // So, if we come from a search, we have to make the search to OMDbAPI through Retrofit

            String title = intent.getStringExtra("title");
            String year = intent.getStringExtra("year");

            Call<Movie> movieCall = year.isEmpty() ? repository.searchMovieByTitle(title) : repository.searchMovieByTitleAndYear(title, Integer.parseInt(year));

            movieCall.enqueue(new Callback<Movie>() {
                @Override
                public void onResponse(Call<Movie> call, Response<Movie> response) {
                    movie = response.body();
                    showMovie();
                    setAfterSearchButtonListeners();
                    setDatePickerListener();
                }

                @Override
                public void onFailure(Call<Movie> call, Throwable t) {
                    showError(false);
                    setOnErrorButtonListeners();
                }
            });
        }
        else {
            // However, if we come from the list of Movies which are stored in the database,
            // we just have to load the Movie from SQLite using the MovieSQLiteHelper class

            db = mdbh.getReadableDatabase();
            Cursor c = mdbh.getMovieByImdbID(db, getIntent().getStringExtra("imdbID"));
            toMovie(c);
            db.close();

            showMovie();
            setShowSavedMovieButtonListeners();
            setDatePickerListener();
        }


    }

    /**
     * We use this method to initialize the Movie attribute of the class from the Cursor object which contains its values
     * @param c
     */
    private void toMovie(Cursor c) {
        @SuppressLint("Range") String poster = c.getString(c.getColumnIndex("poster"));
        @SuppressLint("Range") String title = c.getString(c.getColumnIndex("title"));
        @SuppressLint("Range") String director = c.getString(c.getColumnIndex("director"));
        @SuppressLint("Range") String actors = c.getString(c.getColumnIndex("actors"));
        @SuppressLint("Range") String imdbIDString = c.getString(c.getColumnIndex("imdbID"));
        @SuppressLint("Range") String cityString = c.getString(c.getColumnIndex("city"));
        @SuppressLint("Range") String dateString = c.getString(c.getColumnIndex("date"));

        movie = new Movie();
        movie.setTitle(title);
        movie.setPoster(poster);
        movie.setDirector(director);
        movie.setActors(actors);
        movie.setImdbID(imdbIDString);
        city = cityString;
        date = dateString;
    }

    private void setOnErrorButtonListeners() {
        Button searchAgainWhenErrorButton = findViewById(R.id.searchAgainWhenErrorButton);
        searchAgainWhenErrorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setShowSavedMovieButtonListeners() {
        Button buttonUpdateMovie = (Button) findViewById(R.id.updateMovieButton);
        buttonUpdateMovie.setOnClickListener(view -> { saveMovie(false); });

        Button buttonDeleteMovie = (Button) findViewById(R.id.deleteMovieButton);
        buttonDeleteMovie.setOnClickListener(view -> {
            // We ask the user if he/she really wants to delete the Movie
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.deleteMovieText);
            alert.setMessage(R.string.deleteMovieQuestionText);
            alert.setPositiveButton(R.string.yes, new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            db = mdbh.getWritableDatabase();
                            if (db != null)
                            {
                                mdbh.deleteMovieByImdbID(db, movie.getImdbID());
                                db.close();

                                if (search) {
                                    // If we come from a search, when we delete the button,
                                    // we will take the user to the MainActivity
                                    Intent intent = new Intent();
                                    intent.setClass(getBaseContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    // Otherwise, we will destroy the current activity and go back to the listing
                                    finish();
                                }
                                Toast.makeText(MovieActivity.this, R.string.movieDeletedToast, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            alert.setNegativeButton(R.string.no, new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // If the user taps on "No", we don't have to do anything
                        }
                    });
            alert.show();
        });

        Button goToListingButton = (Button) findViewById(R.id.goToListingButton);
        goToListingButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(getBaseContext(), List.class);
            startActivity(intent);
            finish();
        });
    }

    private void setDatePickerListener() {
        dateEditText = findViewById(R.id.dateEditText);
        dateEditText.setOnClickListener(view -> { showDatePickerDialog(); });
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {

            /**
             * When the user selects a date, we will write it in the "dd / mm / yyyy" format in its corresponding EditText
             * @param datePicker
             * @param year
             * @param month
             * @param day
             */
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                final String selectedDate = day + " / " + (month + 1) + " / " + year;
                dateEditText.setText(selectedDate);
            }
        });

        newFragment.show(this.getSupportFragmentManager(), "datePicker");
    }

    /**
     * In this method we will only set the button listeners which are needed when we come from a search
     */
    private void setAfterSearchButtonListeners() {
        Button searchAgainButton = findViewById(R.id.searchAgainButton);
        searchAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button addMovieButton = findViewById(R.id.addMovieButton);
        addMovieButton.setOnClickListener(view -> {
            saveMovie(true);
        });
    }

    private void saveMovie(boolean firstSave) {
        String date = dateEditText.getText().toString();
        String city = ((TextView) findViewById(R.id.cityEditText)).getText().toString();

        // Before saving, we first have to validate the data the user has to introduce: date and city
        if (validateMovie(date, city, firstSave)) {
            db = mdbh.getWritableDatabase();
            if (db != null)
            {
                // If it is the first time we are saving the Movie, we have to insert it in the database,
                // hide/show the buttons which correspond when displaying a saved Movie
                // and set the button listeners which are needed for this case
                if (firstSave) {
                    mdbh.insertMovie(db, movie.getTitle(), movie.getImdbID(), date, city, movie.getPoster(), movie.getActors(), movie.getDirector());
                    db.close();

                    Toast.makeText(MovieActivity.this, R.string.movieAddedToast, Toast.LENGTH_LONG).show();

                    findViewById(R.id.updateMovieButton).setVisibility(View.VISIBLE);
                    findViewById(R.id.deleteMovieButton).setVisibility(View.VISIBLE);
                    findViewById(R.id.goToListingButton).setVisibility(View.VISIBLE);
                    findViewById(R.id.searchAgainButton).setVisibility(View.GONE);
                    findViewById(R.id.addMovieButton).setVisibility(View.GONE);

                    setShowSavedMovieButtonListeners();
                }
                // If it is not the first time we are saving the Movie, we are updating an existing one
                else {
                    mdbh.updateMovieByImdbID(db, movie.getImdbID(), date, city);
                    db.close();

                    Toast.makeText(MovieActivity.this, R.string.movieUpdatedToast, Toast.LENGTH_LONG).show();
                }
                if (getCurrentFocus() != null) {
                    getCurrentFocus().clearFocus();
                }
            }
        }
    }

    /**
     * We will be able to save the movie only if the date and the city are not empty
     * and, in case we are saving it for the first time, the Movie doesn't already exist in the database
     * @param date
     * @param city
     * @param firstSave
     * @return
     */
    private boolean validateMovie(String date, String city, boolean firstSave) {
        if (date.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, R.string.dateOrCityAreEmptyToast, Toast.LENGTH_LONG).show();
            return false;
        }
        if (firstSave) {
            db = mdbh.getReadableDatabase();
            if (mdbh.checkMovieExists(db, movie.getImdbID())) {
                Toast.makeText(this, R.string.movieAlreadyExistsToast, Toast.LENGTH_LONG).show();
                db.close();
                return false;
            }
            db.close();
        }
        return true;
    }

    private void showMovie() {
        if (!search || movie.getResponse().equals("True")) {
            // If we are searching and the response from the Retrofit call to OMDbAPI is good,
            // or if we are not searching (i.e., we come from the listing of the Movies saved in the SQLite database),
            // we hide/show the buttons which correspond when displaying a saved Movie
            findViewById(R.id.searchAgainWhenErrorButton).setVisibility(View.GONE);
            // Depending on whether we come from a search or not, we will show some buttons or others
            // and display the saved information (city and date) or not
            if (search) {
                findViewById(R.id.updateMovieButton).setVisibility(View.GONE);
                findViewById(R.id.deleteMovieButton).setVisibility(View.GONE);
                findViewById(R.id.goToListingButton).setVisibility(View.GONE);
            }
            else {
                findViewById(R.id.searchAgainButton).setVisibility(View.GONE);
                findViewById(R.id.addMovieButton).setVisibility(View.GONE);
                ((EditText)findViewById(R.id.cityEditText)).setText(city);
                ((EditText)findViewById(R.id.dateEditText)).setText(date);
            }

            ((TextView)findViewById(R.id.movieTitle)).setText(movie.getTitle());
            ((TextView)findViewById(R.id.directorTextView)).setText(movie.getDirector());
            ((TextView)findViewById(R.id.actorsTextView)).setText(movie.getActors());
            // If the movie doesn't have a poster, we load a placeholder image
            if (movie.getPoster().equals("N/A")) {
                ((ImageView)findViewById(R.id.moviePoster)).setImageResource(R.drawable.movie_not_available);
            }
            else {
                Picasso.get().load(movie.getPoster()).into(((ImageView)findViewById(R.id.moviePoster)));
            }
        }
        else {
            // If we are searching and the response from the Retrofit call to OMDbAPI is bad,
            // we show the error and set the button listeners which are needed for this case
            showError(true);
            setOnErrorButtonListeners();
        }
    }

    /**
     * This method will hide/show the Buttons, EditTexts and TextViews that correspond to tell the user there was a problem
     * @param movieNotFound
     */
    private void showError(boolean movieNotFound) {
        // Depending on whether the movie was just not found or if there was an error on the actual call,
        // we will display a message or another
        ((TextView)findViewById(R.id.movieTitle)).setText(movieNotFound ? R.string.movieNotFoundErrorText : R.string.errorOnCallText);
        ((ImageView)findViewById(R.id.moviePoster)).setImageResource(R.drawable.movie_not_available);
        findViewById(R.id.directorTextView).setVisibility(View.GONE);
        findViewById(R.id.actorsTextView).setVisibility(View.GONE);
        findViewById(R.id.directorTitleTextView).setVisibility(View.GONE);
        findViewById(R.id.actorsTitleTextView).setVisibility(View.GONE);
        findViewById(R.id.cityTitleTextView).setVisibility(View.GONE);
        findViewById(R.id.cityEditText).setVisibility(View.GONE);
        findViewById(R.id.dateTitleTextView).setVisibility(View.GONE);
        findViewById(R.id.dateEditText).setVisibility(View.GONE);
        findViewById(R.id.addMovieButton).setVisibility(View.GONE);
        findViewById(R.id.searchAgainButton).setVisibility(View.GONE);
        findViewById(R.id.updateMovieButton).setVisibility(View.GONE);
        findViewById(R.id.deleteMovieButton).setVisibility(View.GONE);
        findViewById(R.id.goToListingButton).setVisibility(View.GONE);
    }
}