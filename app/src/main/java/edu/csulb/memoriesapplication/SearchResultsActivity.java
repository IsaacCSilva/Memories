package edu.csulb.memoriesapplication;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot mediaSnapshot : dataSnapshot.getChildren()) {
                addToUrlList(mediaSnapshot);
            }
            //Data has finished loading
            queryFinished = true;
            progressBar.setVisibility(View.GONE);
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
}