package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayDeque;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by Danie on 10/16/2017.
 */

public class LatestMemoriesActivity extends Activity {
    //added
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private MediaController mediaController;
    private ArrayList<Polaroid> polaroids;
    private CardViewAdapter rvAdapter;
    private MyConstraintLayout constraintLayout;
    private ProgressBar progressBar;
    private Query urlQuery;
    private ArrayDeque<String> urlList;
    static final int CAM_REQUEST = 1;
    private String TAG = "LastestMemoriesActivity";

    //Listener that is attached to the query
    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            //Receive the url string and store it in a list
            String urlString = (String) dataSnapshot.child(GlobalDatabase.getUrlKey()).getValue();
            String mediaType = (String) dataSnapshot.child(GlobalDatabase.getMediaTypeKey()).getValue();
            if(mediaType.charAt(0) == 'i'){
                urlString = urlString + 'i';
            } else if(mediaType.charAt(0) == 'v') {
                urlString = urlString + 'v';
            }
            urlList.add(urlString);
            Log.d(TAG, urlString);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        //set transitions
        setTransitions();

        //Initialize the query
        initializeQuery();

        //instantiate objects
        progressBar = (ProgressBar) this.findViewById(R.id.progress_bar);
        constraintLayout = (MyConstraintLayout) findViewById(R.id.constraintLayout);
        Intent startNeighborActivity = new Intent(this, TrendingActivity.class);
        startNeighborActivity.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        constraintLayout.setLeftPage(startNeighborActivity);
        polaroids = new ArrayList<Polaroid>();
        rvAdapter = new CardViewAdapter(this, polaroids);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setVisibility(View.GONE);
        linearLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int position1 = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                int position2 = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                Log.d("first completely visible item position", "" + position1);
                if (position1 != -1) {
                    View view = (View) ((LinearLayoutManager) recyclerView.getLayoutManager()).findViewByPosition(position1);
                    if (view instanceof CardView) {
                        Log.d("View is of type", "CardView");
                        View childView = (View) ((CardView) view).getChildAt(0);
                        if (childView instanceof RelativeLayout) {
                            VideoView videoView = (VideoView) ((RelativeLayout) childView).getChildAt(0);
                            videoView.start();
                        }
                    }
                    if (position2 != -1 && position2 != position1) {
                        Log.d("first visible item postion", "" + position2);
                        View view2 = (View) ((LinearLayoutManager) recyclerView.getLayoutManager()).findViewByPosition(position2);
                        View childView = (View) ((CardView) view2).getChildAt(0);
                        if (childView instanceof RelativeLayout) {
                            VideoView videoView = (VideoView) ((RelativeLayout) childView).getChildAt(0);
                            if (videoView.isPlaying()) {
                                videoView.pause();
                            }
                        }
                    }
                }
            }
        });

        Uri uri = Uri.parse("http://webm.land/media/Qn8D.webm");
        Polaroid polaroid = new Polaroid(uri, null);

        polaroids.add(polaroid);

        uri = Uri.parse("http://i646.photobucket.com/albums/uu187/jess_roces/animal11.jpg");
        polaroid = new Polaroid(null, uri);
        polaroids.add(polaroid);
        polaroids.add(polaroid);

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Override search hint
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem items) {
        switch (items.getItemId()) {
            case R.id.action_settings: {
                final Intent intent = new Intent(LatestMemoriesActivity.this, Setting.class);
                startActivity(intent);
                Toast.makeText(getBaseContext(), "Information: ", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.action_cam: {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera_intent, CAM_REQUEST);
                return true;
            }
        }
        return super.onOptionsItemSelected(items);
    }


    public void setTransitions() {
        Slide enterSlide = new Slide();
        Slide exitSlide = new Slide();
        enterSlide.setDuration(500);
        enterSlide.setSlideEdge(Gravity.RIGHT);
        exitSlide.setDuration(500);
        exitSlide.setSlideEdge(Gravity.START);
        getWindow().setExitTransition(exitSlide);
        getWindow().setEnterTransition(enterSlide);
        getWindow().setReenterTransition(enterSlide);
        getWindow().setReturnTransition(enterSlide);
    }

    //Retrieves a list of url links and returns null for an empty list
    private void initializeQuery() {
        //Creates a reference for the location where every media link is stored ordered by time
        DatabaseReference databaseReference = GlobalDatabase.getMediaListReference();
        //Set the maximum amount of queries to be received at once
        int maxQuerySize = 30;
        //Initialize the query located as a private class variable
        urlQuery = databaseReference.limitToFirst(maxQuerySize);
        /*
        Attach a listener so that if any more media links are added to the database,
        they will be added to the top of the array list stack*/
        urlQuery.addChildEventListener(childEventListener);
        //Initialize the ArrayList to hold the url strings
        urlList = new ArrayDeque<>(maxQuerySize);
    }

}
