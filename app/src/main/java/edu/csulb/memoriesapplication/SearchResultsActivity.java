package edu.csulb.memoriesapplication;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class SearchResultsActivity extends AppCompatActivity {

    private String stateString;
    private String cityString;
    private boolean queryFinished;
    private ArrayList<String> urlList;
    private final String TAG = "SearchResultsActivity";
    private ProgressBar progressBar;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private ArrayList<Polaroid> polaroids;
    private CardViewAdapter rvAdapter;
    private MyConstraintLayout constraintLayout;

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot mediaSnapshot : dataSnapshot.getChildren()) {
                addToUrlList(mediaSnapshot);
            }
            //Data has finished loading
            queryFinished = true;
            //Todo: Call method to populate the views here
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            loadPolaroids();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener childEventListener = new ChildEventListener() {
        //A new item has been added to the database
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            //Only starts adding to the url list if the query is finished for the new ones
            if (queryFinished) {
                addToUrlList(dataSnapshot);
            }
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        //Initialize the progress bar
        progressBar = (ProgressBar) this.findViewById(R.id.search_progress_bar);

        //instantiate objects
        constraintLayout = (MyConstraintLayout) findViewById(R.id.constraintLayout);
        Intent startLeftNeighborActivity = new Intent(this, UserPageActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        constraintLayout.setLeftPage(startLeftNeighborActivity);
        Intent startRightNeighborActivity = new Intent(this, LatestMemoriesActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        constraintLayout.setRightPage(startRightNeighborActivity);
        polaroids = new ArrayList<Polaroid>();
        rvAdapter = new CardViewAdapter(this, polaroids);
        recyclerView = (RecyclerView) findViewById(R.id.search_recyclerView);
        linearLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int position1 = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                int position2 = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                Log.d("Latest Memories/first completely visible item position", "" + position1);
                if (position1 != -1) {
                    View view = (View) ((LinearLayoutManager) recyclerView.getLayoutManager()).findViewByPosition(position1);
                    if (view instanceof CardView) {
                        Log.d("View is of type", "CardView");
                        View childView = (View) ((CardView) view).getChildAt(0);
                        if (childView instanceof RelativeLayout) {
                            SimpleExoPlayerView videoView = (SimpleExoPlayerView) ((RelativeLayout) childView).getChildAt(0);
                            SimpleExoPlayer simpleExoPlayer = videoView.getPlayer();
                            simpleExoPlayer.setPlayWhenReady(true);
                        }
                    }
                    if (position2 != -1 && position2 != position1) {
                        Log.d("first visible item postion", "" + position2);
                        View view2 = (View) ((LinearLayoutManager) recyclerView.getLayoutManager()).findViewByPosition(position2);
                        View childView = (View) ((CardView) view2).getChildAt(0);
                        if (childView instanceof RelativeLayout) {
                            SimpleExoPlayerView videoView = (SimpleExoPlayerView) ((RelativeLayout) childView).getChildAt(0);
                            SimpleExoPlayer simpleExoPlayer = videoView.getPlayer();
                            simpleExoPlayer.setPlayWhenReady(true);
                        }
                    }
                }
            }
        });
        //Initialize activity variables
        stateString = "";
        cityString = "";
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        //Receives the string
        stateString = intent.getStringExtra("state");
        //Checks if the first letter is upper case, if not it sets it to upper case
        char checkUpper = isUpper(stateString.charAt(0));
        if(checkUpper != ' ') {
            stateString = stateString.replaceFirst(Character.toString(stateString.charAt(0)), Character.toString(checkUpper));
        }
        cityString = intent.getStringExtra("city");
        //Checks if the first letter is upper case, if not it sets it to upper case
        checkUpper = isUpper(cityString.charAt(0));
        if(checkUpper != ' ') {
            cityString = cityString.replaceFirst(Character.toString(cityString.charAt(0)), Character.toString(checkUpper));
        }
    }

    //Checks if a character is uppercase
    private char isUpper(char character) {
        int characterValue = (int) character;
        if(characterValue >= 97) {
            characterValue = characterValue - 32;
            return (char) characterValue;
        }
        return ' ';
    }

    private void initializeQuery() {
        //Initialize the progress bar to appear in the activity while the activity is in the process of querying

        //Query just started, initialize the query to change behavior of child listener
        queryFinished = false;
        //Creates a reference for the location where every media link is stored ordered by time
        DatabaseReference databaseReference = GlobalDatabase.getMediaListReference(stateString);
        //Maximum amount of querries
        final int maxQuerryCount = 700;
        //Initialize the query
        Query urlQuery = databaseReference.equalTo(cityString, GlobalDatabase.CITY_KEY)
                .limitToLast(maxQuerryCount).orderByChild(GlobalDatabase.LIKES_COUNT_KEY);
        /*
        Attach a listener so that if any more media links are added to the database,
        they will be added to the top of the array list stack*/
        urlQuery.addChildEventListener(childEventListener);
        //Add listener so that we know the update finished
        urlQuery.addValueEventListener(valueEventListener);
        //Initialize the ArrayList to hold the url strings
        urlList = new ArrayList<>();
    }

    //Method that adds the url to the urlList ArrayList from a DataSnapshot object
    private void addToUrlList(DataSnapshot mediaSnapshot) {
        String urlString = (String) mediaSnapshot.child(GlobalDatabase.URL_KEY).getValue();
        String mediaType = (String) mediaSnapshot.child(GlobalDatabase.MEDIA_TYPE_KEY).getValue();
        if (mediaType.charAt(0) == 'i') {
            urlString = urlString + 'i';
        } else if (mediaType.charAt(0) == 'v') {
            urlString = urlString + 'v';
        }
        urlList.add(urlString);
    }

    private void loadPolaroids(){
        polaroids.clear();
        String combinedString;
        String uriString;
        char uriType;
        int size = urlList.size();
        Log.d("urlList size", "" + size);

        for(int i = 0; i < size; i++){
            combinedString = urlList.get(i);
            Log.d("Combined String", combinedString);
            uriString = combinedString.substring(0, combinedString.length() - 2);
            Log.d("uriString", uriString);
            uriType = combinedString.charAt(combinedString.length() -1);
            Log.d("uriType", ""+ uriType);
            if(uriType == 'i'){
                polaroids.add(new Polaroid(null, Uri.parse(uriString)));
            }
            else if(uriType == 'v'){
                Log.d("Latest loadPolaroid()", "loading video");
                polaroids.add(new Polaroid(Uri.parse(uriString), null));
            }
            rvAdapter.notifyDataSetChanged();
        }
    }
}