package com.example.filmstracker.webservices;

import com.example.filmstracker.models.Movie;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Repository {

    private final ApiService apiService;

    public static final String BASE_URL = "http://www.omdbapi.com/?";
    public static final String API_KEY = "60fb3241";

    public Repository() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.addInterceptor(new RequestInterceptor());
        OkHttpClient client = okHttpClientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public Call<Movie> searchMovieByTitle(String title) {
        return apiService.searchByTitle(title);
    }

    public Call<Movie> searchMovieByTitleAndYear(String title, int year) {
        return apiService.searchByTitleAndYear(title, year);
    }

}
