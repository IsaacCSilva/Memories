package edu.csulb.memoriesapplication;

import android.*;
import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Danie on 10/16/2017.
 */

public class TrendingActivity extends AppCompatActivity{

    //added
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private ArrayList<Polaroid> polaroids;
    private CardViewAdapter rvAdapter;
    private MyConstraintLayout constraintLayout;
    private boolean accessLocationPermission;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int PERMISSION_REQUEST_CODE = 1052;
    private boolean cameraRequest;
    private String mCurrentPhotoPath;
    private String TAG = "TrendingActivity";
    private String city;
    private String state;
    private ArrayList<String> urlList;
    private boolean queryFinished;
    private ProgressBar progressBar;

    //Todo: There is a method at the bottom of the activity called userHasRefreshed, urlList is always updated so no need to worry about that

    //Value event listener for the query
    ValueEventListener valueEventListener = new ValueEventListener() {
        /*Adds the last *amount* of queries and adds them in to the head of the ArrayDeque each time to show
        most recent ones first*/
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot mediaSnapshot : dataSnapshot.getChildren()) {
                addToUrlList(mediaSnapshot);
            }
            //Data has finished loading
            queryFinished = true;
            //Todo: Call method to populate the views here
            
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //Child event listener for the query
    ChildEventListener childEventListener = new ChildEventListener() {
        //A new item has been added to the database
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            //Only starts adding to the url list if the query is finished for the new ones
            if(queryFinished) {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        //Initialize progress bar
        progressBar = (ProgressBar) this.findViewById(R.id.trending_progress_bar);

        //get calling activity
        Intent intent = getIntent();
        int slideDirection = 0;
        if (intent != null) {
            slideDirection = intent.getIntExtra("slide edge", 0);
        }

        //set transitions
        setTransitions(slideDirection);

        //Variable to check if the user wants a camera request to change the behavior of onRequestPermissionsResult
        cameraRequest = false;

        //Check if we are able to access the user's location
        accessLocationPermission = UserPermission.checkUserPermission(this, UserPermission.Permission.LOCATION_PERMISSION);

        //If accessLocationPermission variable is false, cannot display anything... ask for user's permission
        if (!accessLocationPermission) {
            requestPermission();
        } else {
            //Get user location and initialize the query, param true to initialize query
            getUserLocationAndInitializeQuery(true);
        }

        //instantiate objects
        constraintLayout = (MyConstraintLayout) findViewById(R.id.constraintLayout);
        Intent startLeftNeighborActivity = new Intent(this, UserPageActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        constraintLayout.setLeftPage(startLeftNeighborActivity);
        Intent startRightNeighborActivity = new Intent(this, LatestMemoriesActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        constraintLayout.setRightPage(startRightNeighborActivity);
        polaroids = new ArrayList<Polaroid>();
        rvAdapter = new CardViewAdapter(this, polaroids);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
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
                Intent intent = new Intent(TrendingActivity.this, Setting.class);
                startActivity(intent);
                Toast.makeText(getBaseContext(), "Information: ", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.action_cam: {
                if (accessLocationPermission) {
                    dispatchTakePictureIntent();
                    return true;
                } else {
                    cameraRequest = true;
                    requestPermission();
                }
            }
        }
        return super.onOptionsItemSelected(items);
    }

    //Asks for the user's permission, double check just in case, don't want to ask the user a second time
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
    }

    //Permission result received, act accordingly
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission has been granted
                    accessLocationPermission = true;
                    if (cameraRequest) {
                        dispatchTakePictureIntent();
                        cameraRequest = false;
                    }
                } else {
                    //Permission Denied
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "edu.csulb.memoriesapplication.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, "tempImage.jpg");
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Intent sendIntent = new Intent(TrendingActivity.this, DisplayActivity.class);

            sendIntent.putExtra("filepath", mCurrentPhotoPath);

            startActivity(sendIntent);
        }
    }


    public void setTransitions(int slideDirection) {
        Log.d("Slide Direction", "" + slideDirection);
        Slide enterSlide = new Slide();
        Slide exitSlide = new Slide();

        enterSlide.setDuration(500);
        exitSlide.setDuration(500);

        if (slideDirection == 0) {
            enterSlide.setSlideEdge(Gravity.RIGHT);
            exitSlide.setSlideEdge(Gravity.RIGHT);
        }

        if (slideDirection == 1) {
            enterSlide.setSlideEdge(Gravity.START);
            exitSlide.setSlideEdge(Gravity.START);
        }
        getWindow().setExitTransition(exitSlide);
        getWindow().setEnterTransition(enterSlide);
        getWindow().setReenterTransition(enterSlide);
        getWindow().setReturnTransition(enterSlide);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("Activity", "BACK PRESSED");
    }

    //Initialize Query parameter is there to see if the method should just update user location or initialize the query also
    private void getUserLocationAndInitializeQuery(final boolean initializeQuery) {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();
                        Geocoder geocoder = new Geocoder(TrendingActivity.this, Locale.getDefault());
                        try {
                            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                            city = addressList.get(0).getLocality();
                            state = addressList.get(0).getAdminArea();
                            if (initializeQuery) {
                                initializeQuery();
                            }
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            });
        } catch (SecurityException exception) {
            Log.d(TAG, "Security Exception : " + exception.toString());
        }
    }

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

    //Retrieves a list of url links and returns null for an empty list
    private void initializeQuery() {
        //Initialize the progress bar to appear in the activity while the activity is in the process of querying

        //Query just started, initialize the query to change behavior of child listener
        queryFinished = false;
        //Creates a reference for the location where every media link is stored ordered by time
        DatabaseReference databaseReference = GlobalDatabase.getMediaListReference(state);
        //Maximum amount of querries
        final int maxQuerryCount = 700;
        //Initialize the query
        Query urlQuery = databaseReference.equalTo(city, GlobalDatabase.CITY_KEY)
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

    private void userHasRefreshed() {

    }

}