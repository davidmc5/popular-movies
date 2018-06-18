package com.dadahasa.movies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dadahasa.movies.model.Trailer;


import java.util.List;


public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {

    //data source for the movie data retrieved from the TMDB API
    private List<Trailer> trailerList;
    private TrailerClickListener listener;

    //private Context context;



    //interface for the click listener
    public interface TrailerClickListener {
        void onTrailerClick(int clickedTrailerIndex);
    }


    //Constructor called by detail activity to pass the data retrieved and attach the listener
    TrailerAdapter(List<Trailer> trailerList, TrailerClickListener listener){
        this.trailerList = trailerList;
        this.listener = listener;
    }


    //A view holder object will display a single item to view
    // (creating new views or reusing hidden ones)
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //view for each item in the recyclerview
        private TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.trailerView);
            itemView.setOnClickListener(this);
        }



        @Override
        public void onClick(View view) {
            if (listener != null){
                //if there is a listener, this will call the method onTrailerClick on DetailActivity
                int clickedTrailer = getAdapterPosition();
                listener.onTrailerClick(clickedTrailer);
            }
        }
    }

    //Called when recyclerView instantiates a new viewHolder instance which inflates item views from xml.
    // Returns a new ViewHolder object associated with the new view.
    // The inflater takes xml and returns a view for one image cell
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForText = R.layout.trailer_item;
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
        Trailer trailer =  trailerList.get(position);
        viewHolder.mTextView.setText(trailer.getName());
    }


    @Override
    public int getItemCount() {
        if (trailerList == null){return 0;}
        return trailerList.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.trailer_item;
    }


    //method to update the adapter when the movie list changes
    //this is called by the retrofit method onResponse when new API data is fetched
    public void addData(List<Trailer> trailerList) {
        this.trailerList = trailerList;
        notifyDataSetChanged();
    }
}
