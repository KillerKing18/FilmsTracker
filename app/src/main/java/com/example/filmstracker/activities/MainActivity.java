package com.example.filmstracker.activities;

import static java.lang.System.exit;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.filmstracker.database.MovieSQLiteHelper;
import com.example.filmstracker.R;

public class MainActivity extends AppCompatActivity {
    MovieSQLiteHelper mdbh;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SQLite helper class
        mdbh = new MovieSQLiteHelper(this);

        setButtonListeners();

        // If there are movies in the database, we enable the deleteAllMovies button
        enableDeleteButton(!checkTableIsEmpty());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The number of entries in the movie table could have changed when we go back to the MainActivity
        // so we have to check if it has to be enabled/disabled
        enableDeleteButton(!checkTableIsEmpty());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            // We override the return button when we are in the MainActivity to ask the user if he/she really wants to exit the application
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.exitAppTitleText);
            alert.setMessage(R.string.exitAppQuestionText);
            alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            exit(1);
                        }
                    });
            alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // If the user taps on "No", we don't have to do anything
                        }
                    });
            alert.show();
        }
        return false;
    }

    private void setButtonListeners() {
        Button goToSearchMovieButton = (Button) findViewById(R.id.goToSearchMovieButton);
        goToSearchMovieButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(getBaseContext(), SearchMovieActivity.class);
            startActivity(intent);
        });

        Button checkMoviesButton = (Button)findViewById(R.id.checkMoviesButton);
        checkMoviesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), List.class);
                startActivity(intent);
            }
        });

        Button deleteAllMoviesButton = (Button)findViewById(R.id.deleteAllMoviesButton);
        deleteAllMoviesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We ask the user if he/she really wants to delete all movies from the database
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle(R.string.deleteAllMoviesText);
                alert.setMessage(R.string.deleteAllMoviesQuestionText);
                alert.setPositiveButton(R.string.yes, new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                db = mdbh.getWritableDatabase();
                                if (db != null)
                                {
                                    mdbh.deleteAllMovies(db);
                                    db.close();

                                    // Once all entries are deleted, we have to disable the button
                                    enableDeleteButton(false);

                                    Toast.makeText(MainActivity.this, R.string.allMoviesDeletedToast, Toast.LENGTH_LONG).show();
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
            }
        });
    }

    private boolean checkTableIsEmpty() {
        db = mdbh.getReadableDatabase();
        boolean empty = mdbh.checkTableIsEmpty(db);
        db.close();
        return empty;
    }

    private void enableDeleteButton(boolean enabled) {
        Button deleteAllMoviesButton = (Button)findViewById(R.id.deleteAllMoviesButton);
        deleteAllMoviesButton.setEnabled(enabled);
        // We change the button opacity depending on if it's enabled or disabled
        deleteAllMoviesButton.setAlpha(enabled ? 1f : .5f);
    }
}