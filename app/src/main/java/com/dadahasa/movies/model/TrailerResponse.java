package com.dadahasa.movies.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class TrailerResponse {

    @SerializedName("id")
    private Integer id;

    @SerializedName("results")
    private List<Trailer> results = null;

    public Integer getId() {
        return id;
    }

    public List<Trailer> getResults() {
        return results;
    }
}
