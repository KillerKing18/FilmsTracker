package com.example.filmstracker.webservices;

import com.example.filmstracker.models.Movie;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("/")
    Call<Movie> searchByTitle(@Query("t") String title);

    @GET("/")
    Call<Movie> searchByTitleAndYear(@Query("t") String title, @Query("y") int year);
}
