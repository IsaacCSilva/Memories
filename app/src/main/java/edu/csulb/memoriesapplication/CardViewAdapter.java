package edu.csulb.memoriesapplication;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Francisco on 10/28/2017.
 * CardViewAdapter.java
 * Binds data to item in RecyclerView
 * this is where each Polaroid's views are instantiated
 */

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.CardViewHolder>{
    private List<Polaroid> polaroids;
    private Context context;

    /**
     * CardViewHolder
     * this inner class holds on to a polaroid's views and their data
     */
    public class CardViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ImageView imageView;
        public VideoView videoView;


        public CardViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.cardItem);
            imageView = new ImageView(itemView.getContext());
            videoView = new VideoView(itemView.getContext());

        }
    }

    public CardViewAdapter(Context context, List<Polaroid> polaroids){
        this.polaroids = polaroids;
        this.context = context;
    }

    /**
     * OnCreateViewHolder
     * inflates a polaroid
     */
    @Override
    public CardViewAdapter.CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View polaroidView = inflater.inflate(R.layout.recyclerview_item, parent, false);

        CardViewHolder viewHolder = new CardViewHolder(polaroidView);
        return viewHolder;
    }

    @Override
    /**
     * onBindViewHolder
     * This is where the data binding takes place
     * a holder's data is passed on the recyclerview item
     * view layouts are also programmatically defined here
     */
    public void onBindViewHolder(CardViewAdapter.CardViewHolder holder, int position) {
        Polaroid polaroid = polaroids.get(position);

        CardView cardView = holder.cardView;
        cardView.setContentPadding(100,100,100,250);

        if(polaroid.getImageUri() != null){
            //load image from Uri
            ImageView imageView = new ImageView(context, null);
            Glide.with(context)
                    .load(polaroid.getImageUri().toString())
                    .into(imageView);
            cardView.addView(imageView);

        }
        else if(polaroid.getVideoUri() != null){
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            RelativeLayout rv = new RelativeLayout(context, null);
            VideoView videoView = new VideoView(context);
            videoView.setLayoutParams(lp);
            //MediaController mc = new MediaController(context);
            //mc.setAnchorView(videoView);
            //videoView.setMediaController(mc);
//            //videoView.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    mp.setLooping(true);
//                }
//            });
            videoView.setVideoURI(polaroid.getVideoUri());
            rv.addView(videoView);
            rv.setBackgroundColor(Color.BLACK);
            cardView.addView(rv);
            if(position == 0) {
                videoView.start();
            }
        }
    }

    /**
     * getItemCount()
     * returns the amount of polaroids in the recyclerview
     */
    @Override
    public int getItemCount() {
        return polaroids.size();
    }
}
