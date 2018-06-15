package com.dadahasa.movies.model;

import com.google.gson.annotations.SerializedName;

public class Review {

    @SerializedName("author")
    private String author;

    @SerializedName("content")
    private String content;

    @SerializedName("id")
    private String id;

    @SerializedName("url")
    private String url;

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

}