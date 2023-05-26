package com.example.filmstracker.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.filmstracker.R;
import com.example.filmstracker.activities.MainActivity;
import com.example.filmstracker.activities.MovieActivity;
import com.example.filmstracker.database.MovieSQLiteHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class List extends Activity {


    MovieSQLiteHelper mdbh;
    SQLiteDatabase db;
    ListView listView;
    CustomAdapter customAdapter = new CustomAdapter();
    java.util.List<String> titles = new ArrayList<String>();
    java.util.List<String> posters = new ArrayList<String>();
    java.util.List<String> imdbIDs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        fillData();

        listView = (ListView) findViewById(android.R.id.list);

        listView.setEmptyView(findViewById(android.R.id.empty));

        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MovieActivity.class);
                // Using the imdbID, we will be able to find the movie in the database, as it is a primary key
                intent.putExtra("imdbID", imdbIDs.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // We have to update the data lists each time we go back to this activity,
        // in case a movie has been added or deleted
        clearData();
        fillData();
        customAdapter.notifyDataSetChanged();
    }

    private void fillData() {
        mdbh = new MovieSQLiteHelper(this);

        db = mdbh.getReadableDatabase();
        Cursor c = mdbh.getAllMovies(db);
        // We check if at least one register exists
        if (c.moveToFirst()) {
            do {
                @SuppressLint("Range") String title = c.getString(c.getColumnIndex("title"));
                @SuppressLint("Range") String poster = c.getString(c.getColumnIndex("poster"));
                @SuppressLint("Range") String imdbID = c.getString(c.getColumnIndex("imdbID"));
                titles.add(title);
                posters.add(poster);
                imdbIDs.add(imdbID);
            } while(c.moveToNext());
        }
        db.close();
    }

    private void clearData() {
        titles.clear();
        posters.clear();
        imdbIDs.clear();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            Intent intent = new Intent();
            intent.setClass(getBaseContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        return false;
    }


    private class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return imdbIDs.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View view1 = getLayoutInflater().inflate(R.layout.movie_row, null);

            TextView title = view1.findViewById(R.id.titleRow);
            ImageView poster = view1.findViewById(R.id.posterRow);

            title.setText(titles.get(i));

            // If there is no poster, we load a placeholder image
            if (posters.get(i).equals("N/A")) {
                poster.setImageResource(R.drawable.movie_poster);
            }
            else {
                Picasso.get().load(posters.get(i)).into(poster);
            }

            return view1;
        }
    }
}
