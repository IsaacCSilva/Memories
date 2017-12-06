package edu.csulb.memoriesapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Francisco on 10/28/2017.
 * CardViewAdapter.java
 * Binds data to item in RecyclerView
 * this is where each Polaroid's views are instantiated
 */

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.CardViewHolder> implements com.google.android.gms.tasks.OnSuccessListener<FileDownloadTask.TaskSnapshot> {
    private List<Polaroid> polaroids;
    private Context context;
    private CardView cardView;
    private int position;
    private File localFile;
    private final static String MEDIA = "media";

    @Override
    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout rv = new RelativeLayout(context, null);

        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the player
        SimpleExoPlayer simpleExoPlayer =
                ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        SimpleExoPlayerView videoView = new SimpleExoPlayerView(context);
        videoView.setPlayer(simpleExoPlayer);

//            VideoView videoView = new VideoView(context);
//            videoView.setLayoutParams(lp);
        //MediaController mc = new MediaController(context);
        //mc.setAnchorView(videoView);
        //videoView.setMediaController(mc);
//            //videoView.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    mp.setLooping(true);
//                }
//            });
//            videoView.setVideoURI(polaroid.getVideoUri());
//            videoView.setM.create(context, polaroid.getVideoUri());
//            videoView.seekTo(10);


        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "yourApplicationName"));
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse("file://" + localFile.toString()),
                dataSourceFactory, extractorsFactory, null, null);
        // Prepare the player with the source.
        simpleExoPlayer.prepare(videoSource);
        videoView.setUseController(false);
        rv.addView(videoView);
        rv.setBackgroundColor(Color.BLACK);
        cardView.addView(rv);
        simpleExoPlayer.setRepeatMode(SimpleExoPlayer.REPEAT_MODE_ONE);
        if(position == 0) {
            simpleExoPlayer.setPlayWhenReady(true);
        }
    }

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

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        if(polaroid.getImageUri() != null){
            //load image from Uri
            StorageReference imgReference = firebaseStorage.getReferenceFromUrl(polaroid.getImageUri().toString());
            ImageView imageView = new ImageView(context, null);
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(imgReference)
                    .bitmapTransform(new RotateTransformation(context, 90), new CenterCrop(context))
                    .placeholder(R.color.cardview_dark_background)
                    .into(imageView);
            cardView.addView(imageView);

        }
        else if(polaroid.getVideoUri() != null){
            this.position = position;
            this.cardView =  holder.cardView;
            StorageReference videoReference = firebaseStorage.getReferenceFromUrl(polaroid.getVideoUri().toString());

            localFile = null;
            try {
                localFile = File.createTempFile("video", "");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("localfile tostring()", localFile.toString());
            videoReference.getFile(localFile).addOnSuccessListener(this).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

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
