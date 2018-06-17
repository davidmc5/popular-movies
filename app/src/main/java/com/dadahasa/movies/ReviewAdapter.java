package com.dadahasa.movies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dadahasa.movies.model.Review;
import com.dadahasa.movies.model.Trailer;

import java.util.List;


public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    //We don't need a click listener for reviews!

    //data source for the reviews retrieved from the TMDB API
    private List<Review> reviewList;

    //view for each review in the recyclerview
    //A review item has an author and the actual review notes.
    //private TextView authorTxView;
    //private TextView reviewTxView;


    //Constructor called by detail activity to pass the data retrieved and attach the listener
    ReviewAdapter(List<Review> reviewList){
        this.reviewList = reviewList;
    }


    //A view holder object will display a single item to view
    // (creating new views or reusing hidden ones)
    public class ViewHolder extends RecyclerView.ViewHolder{

        //view for each review in the recyclerview
        //A review item has an author and the actual review notes.
        private TextView authorTxView;
        private TextView reviewTxView;


        public ViewHolder(View itemView) {
            super(itemView);
            authorTxView = itemView.findViewById(R.id.author_txv);
            reviewTxView = itemView.findViewById(R.id.review_txv);
        }
    }

    //Called when recyclerView instantiates a new viewHolder instance which inflates item views from xml.
    // Returns a new ViewHolder object associated with the new view.
    // The inflater takes xml and returns a view for one image cell
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForText = R.layout.review_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForText, viewGroup, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }


    //Sets the data (text) to each view holder (each item to display) from the data array, based on the adapter position
    //Called by recyclerView when it needs to populate a particular view with data from the model.
    //It binds the given view holder to the data source every time the recycler needs to display an item for the given position.
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Review review =  reviewList.get(position);
        viewHolder.authorTxView.setText(review.getAuthor());
        viewHolder.reviewTxView.setText(review.getContent());
    }


    @Override
    public int getItemCount() {
        if (reviewList == null){return 0;}
        return reviewList.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.review_item;
    }


    //method to update the adapter when the movie list changes
    //this is called by the retrofit method onResponse when new API data is fetched
    public void addData(List<Review> reviewList) {
        this.reviewList = reviewList;
        notifyDataSetChanged();
    }
}
