package com.example.filmstracker.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filmstracker.database.MovieSQLiteHelper;
import com.example.filmstracker.R;

import java.time.Year;

public class SearchMovieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movie);

        // Automatically set the focus in the title EditText
        ((EditText) findViewById(R.id.titleEditText)).requestFocus();

        setButtonListeners();
    }

    private void setButtonListeners() {
        Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(view -> {
            String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
            String year = ((EditText) findViewById(R.id.yearEditText)).getText().toString();

            if (validateSearch(title, year)) {
                Intent intent = new Intent(getApplicationContext(), MovieActivity.class);
                // If the search fields are valid, we send them to the MovieActivity, which will do the search in OMDbAPI through Retrofit
                intent.putExtra("title", title);
                intent.putExtra("year", year);
                // We also send a search boolean so the MovieActivity knows it has to do the search through Retrofit,
                // instead of loading it from the local SQLite database
                intent.putExtra("search", true);
                startActivity(intent);
            }
        });
    }

    private boolean validateSearch(String title, String year) {
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.titleIsEmptyToast, Toast.LENGTH_LONG).show();
            return false;
        }
        // We check if the year introduced is between 1800 and the current year
        if (!year.isEmpty() && (Integer.parseInt(year) < 1800 || Integer.parseInt(year) > Year.now().getValue())) {
            Toast.makeText(this, R.string.wrongYearToast, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // When we get back to the SearchMovieActivity, we clear the forms so the user can start a new search
        clearForms();
    }

    private void clearForms() {
        EditText title = ((EditText) findViewById(R.id.titleEditText));
        title.requestFocus();
        title.setText("");

        ((EditText) findViewById(R.id.yearEditText)).setText("");
    }
}