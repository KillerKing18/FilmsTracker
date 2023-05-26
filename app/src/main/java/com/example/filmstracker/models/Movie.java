package com.example.filmstracker.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * This Movie class corresponds with the response we get from OMDbAPI
 */
public class Movie {
    @SerializedName("Title")
    @Expose
    private String title;
    @SerializedName("Director")
    @Expose
    private String director;
    @SerializedName("Actors")
    @Expose
    private String actors;
    @SerializedName("Poster")
    @Expose
    private String poster;
    @SerializedName("imdbID")
    @Expose
    private String imdbID;
    @SerializedName("Response")
    @Expose
    private String response;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }


}
