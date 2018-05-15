package com.dadahasa.movies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dadahasa.movies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;


public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private List<Movie> movieList;
    //private ItemClickListener mClickListener;

    //used for picasso calls
    private Context context;
    public static final String IMAGE_URL_BASE_PATH = "http://image.tmdb.org/t/p/w342//";

    //reference for the listener interface
    final private MovieClickListener mOnClickListener;


    //interface for the click listener
    public interface MovieClickListener {
        void onMovieClick(int clickedMovieIndex);
    }

    //Constructor to pass the image files
    MainAdapter(Context context, List<Movie> movieList, MovieClickListener listener){
        this.context = context;
        this.movieList = movieList;
        this.mOnClickListener = listener;
    }



    //A view holder object will display a single image cell to view
    // (creating new views or reusing hidden ones)
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mImageView;


        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mOnClickListener != null){
                int movieIndex = getAdapterPosition();
                mOnClickListener.onMovieClick(movieIndex);
            }
        }
    }

    //Called when recyclerView instantiates a new viewHolder instance which inflates item views from xml.
    // Returns a new ViewHolder object associated with the new view.
    // The inflater takes xml and returns a view for one image cell
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForImage = R.layout.recyclerview_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForImage, viewGroup, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }



    //binds data to each cell's image based on the adapter position
    //Called when recyclerView needs to populate the view with data from the model.
    //It binds the given view holder to the data source every time the recycler needs to display a view.
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        String image_url = IMAGE_URL_BASE_PATH + movieList.get(position).getPosterPath();

        Picasso.with(context)
                .load(image_url)
                .placeholder(R.drawable.popcorn)
                .error(R.drawable.popcorn)
                //.resize(50, 50).centerCrop()
                .fit().centerInside()
                .into(viewHolder.mImageView);
    }



    @Override
    public int getItemCount() {
        if (movieList == null){return 0;}
        return movieList.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.recyclerview_item;
    }


    //method to update the adapter when the movie list changes
    //this is called by the retrofit method onResponse when new API data is fetched
    public void addData(List<Movie> movieList) {
        this.movieList= movieList;
        notifyDataSetChanged();
    }
}
