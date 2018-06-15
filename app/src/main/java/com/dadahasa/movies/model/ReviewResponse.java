package com.dadahasa.movies.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewResponse {

    @SerializedName("id")
    private Integer id;

    @SerializedName("page")
    private Integer page;

    @SerializedName("results")
    private List<Review> results = null;

    @SerializedName("total_pages")
    private Integer totalPages;

    @SerializedName("total_results")
    private Integer totalResults;


    public List<Review> getResults() {
        return results;
    }

}


