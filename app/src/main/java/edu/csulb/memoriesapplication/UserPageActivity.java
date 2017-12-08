package edu.csulb.memoriesapplication;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayDeque;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Danie on 10/24/2017.
 */

public class UserPageActivity extends Activity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private final int RESULT_LOAD_PROFILE_PIC = 1;
    private final int RESULT_LOAD_BACKGROUND_PIC = 2;
    private final String TAG = "UserPageActivity";
    private final int REQUEST_CODE = 1052;
    private boolean readPermissionGranted;
    private CircleImageView userProfileImageView;
    private ImageView backgroundImageView;
    private ImageButton editIntroductionButton;
    private ImageButton saveEditButton;
    private TextView postsCountText;
    private EditText userProfileIntroEditView;
    private TextView userNameTextView;
    private TextView userIdTextView;
    private View viewContainer;
    private View progressBar;
    private String userId;
    private int activityToStart;
    private boolean userPrimitiveDataLoaded;
    private boolean userProfileImageLoaded;
    private boolean userBackgroundImageLoaded;
    private boolean userEditingProfileIntro;
    private String prevUserIntro;
    private MyCoordinatorLayout coordinatorLayout;
    private GridView gridView;
    private ImageAdapter imageAdapter;
    private Query urlQuery;

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayDeque<String> urlList = new ArrayDeque<>();
            //Get the list
            String urlString = "";
            String mediaType = "";
            for (DataSnapshot mediaSnapshot : dataSnapshot.getChildren()) {
                mediaType = (String) mediaSnapshot.child(UserDatabase.USER_MEDIA_TYPE_KEY).getValue();
                urlString = (String) mediaSnapshot.child(UserDatabase.USER_URL_KEY).getValue();
                if (mediaType.charAt(0) == 'i') {
                    urlString = urlString + 'i';
                } else if (mediaType.charAt(0) == 'v') {
                    urlString = urlString + 'v';
                    urlString = urlString + 'v';
                }
                urlList.addFirst(urlString);
            }
            loadGrid(urlList);
            //TODO; Daniel needs to add the progress bar to the grid layout and make it dissapear everything is loaded
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //set transitions
        setTransitions();

        //Initialize Layout
        coordinatorLayout = (MyCoordinatorLayout) findViewById(R.id.coordinatorLayout);
        Intent startNeighborActivity = new Intent(this, TrendingActivity.class);
        startNeighborActivity.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        coordinatorLayout.setRightPage(new Intent(this, TrendingActivity.class));

        //Instantiate all of the primitive views
        postsCountText = (TextView) this.findViewById(R.id.posts_count_text);
        userProfileIntroEditView = (EditText) this.findViewById(R.id.profile_information_text);
        userNameTextView = (TextView) this.findViewById(R.id.user_name_text);
        userIdTextView = (TextView) this.findViewById(R.id.user_id_text);
        viewContainer = (AppBarLayout) this.findViewById(R.id.view_container);
        progressBar = (ProgressBar) this.findViewById(R.id.progress_bar);
        viewContainer.setVisibility(View.GONE);
        gridView = (GridView) this.findViewById(R.id.gridview);
        imageAdapter = new ImageAdapter(this);
        gridView.setAdapter(imageAdapter);

        //Instantiate all of the database variables
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        //Initialize ImageViews and their listeners
        userProfileImageView = (CircleImageView) this.findViewById(R.id.user_profile_picture);
        userProfileImageView.setOnClickListener(this);
        backgroundImageView = (ImageView) this.findViewById(R.id.background_image);
        backgroundImageView.setOnClickListener(this);
        editIntroductionButton = (ImageButton) this.findViewById(R.id.edit_introduction_button);
        editIntroductionButton.setOnClickListener(this);
        saveEditButton = (ImageButton) this.findViewById(R.id.save_edit_button);
        saveEditButton.setOnClickListener(this);


        //Set the boolean values to false to let the activity only appear once everything is loaded
        userPrimitiveDataLoaded = false;
        userProfileImageLoaded = false;
        userBackgroundImageLoaded = false;

        //Set the boolean value for userEditingProfileIntro to false
        //Used to control the flow and use of the back button
        userEditingProfileIntro = false;


        //Check if user profile image exists in internal storage, if so, set it as the profile image
        Bitmap userProfilePic = InternalStorage.getProfilePic(this, userId);
        if (userProfilePic != null) {
            userProfileImageView.setImageBitmap(userProfilePic);
            userProfileImageLoaded = true;
        } else {
            //User profile image is not contained in internal storage, check if the FirebaseStorage contains it
            Intent getUserProfilePicFromStorage = new Intent(this, UserService.class);
            getUserProfilePicFromStorage.setAction(UserService.LOAD_USER_PROFILE_PICTURES_FROM_DATABASE_ACTION);
            startService(getUserProfilePicFromStorage);

            IntentFilter intentFilter = new IntentFilter(BroadcastKey.USER_PROFILE_IMAGE_LOAD_FINISH_ACTION);
            LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Check if we were able to find a profile image
                    if (intent.getBooleanExtra(BroadcastKey.RECEIVED_IMAGE, false)) {
                        //Image has been received from the database
                        Bitmap userProfilePic = InternalStorage.getProfilePic(UserPageActivity.this, userId);
                        userProfileImageView.setImageBitmap(userProfilePic);
                        userProfileImageLoaded = true;
                        makeViewsVisible();
                    } else {
                        //Image does not exist in both the internal storage nor the database
                        //Use default values
                        userProfileImageLoaded = true;
                        makeViewsVisible();
                    }
                }
            }, intentFilter);
        }

        //Check if user background image exists, if so, set it as the background image
        Bitmap userBackgroundPic = InternalStorage.getBackgroundPic(this, userId);
        if (userBackgroundPic != null) {
            backgroundImageView.setImageBitmap(userBackgroundPic);
            userBackgroundImageLoaded = true;
        } else {
            //User background image is not contained in internal storage, check if FirebaseStorage contains it
            Intent getUserBackgroundPicFromStorage = new Intent(this, UserService.class);
            getUserBackgroundPicFromStorage.setAction(UserService.LOAD_USER_BACKGROUND_PICTURES_FROM_DATABASE_ACTION);
            startService(getUserBackgroundPicFromStorage);

            IntentFilter intentFilter = new IntentFilter(BroadcastKey.USER_BACKGROUND_IMAGE_LOAD_FINISH_ACTION);
            LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getBooleanExtra(BroadcastKey.RECEIVED_IMAGE, false)) {
                        //Image has been received from the database
                        Bitmap userBackgroundPic = InternalStorage.getBackgroundPic(UserPageActivity.this, userId);
                        backgroundImageView.setImageBitmap(userBackgroundPic);
                        userBackgroundImageLoaded = true;
                        makeViewsVisible();
                    } else {
                        //Image does not exist in both the internal storage nor the database
                        //Use default values
                        userBackgroundImageLoaded = true;
                        makeViewsVisible();
                    }
                }
            }, intentFilter);
        }

        readPermissionGranted = UserPermission.checkUserPermission(this, UserPermission.Permission.READ_PERMISSION);

        //Register receiver to check if UserService is finished refreshing the user data
        IntentFilter intentFilter = new IntentFilter(BroadcastKey.USER_INFO_REFRESH_FINISH_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Refresh in all of the user's information and set the various text views to the values as necessary
                User user = (User) intent.getParcelableExtra(BroadcastKey.USER_OBJECT);
                loadUser(user);
            }
        }, intentFilter);

        //Soft input mode
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //Load in the user's images
        getUserImagesUrl();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        urlQuery.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Call to refresh the user's primitive data everytime this activity is revisited
        refreshUserInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Set that the user's primitive data is no longer valid and to refresh
        userPrimitiveDataLoaded = false;
        urlQuery.removeEventListener(valueEventListener);
    }

    private void loadUser(User user) {
        //Load in info from internal storage after UserService finishes refreshing all of the information
        postsCountText.setText(String.valueOf(user.userPostsCount));
        userIdTextView.setText(user.userID);
        userNameTextView.setText(user.name);
        userProfileIntroEditView.setText(user.userIntro);
        userPrimitiveDataLoaded = true;

        //Makes sure that everything is loaded before making the views visible
        makeViewsVisible();
    }

    //Ask the user if we are able to store a picture with their permission during runtime
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission has been granted
                    readPermissionGranted = true;
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, activityToStart);
                } else {
                    Toast.makeText(this, "Read Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_profile_picture: {
                if (!userEditingProfileIntro) {
                    if (!readPermissionGranted) {
                        activityToStart = RESULT_LOAD_PROFILE_PIC;
                        requestPermissions();
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, RESULT_LOAD_PROFILE_PIC);
                    }
                } else {
                    hideSoftKeyboard();
                }
            }
            break;
            case R.id.background_image: {
                if (!userEditingProfileIntro) {
                    if (!readPermissionGranted) {
                        activityToStart = RESULT_LOAD_BACKGROUND_PIC;
                        requestPermissions();
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, RESULT_LOAD_BACKGROUND_PIC);
                    }
                } else {
                    hideSoftKeyboard();
                }
            }
            break;
            case R.id.edit_introduction_button: {
                editIntroMode(true);
            }
            break;
            case R.id.save_edit_button: {
                editIntroMode(false);
                //Create new intent to start service to save the new intro to the database
                String newUserIntro = userProfileIntroEditView.getText().toString();
                Intent intent = new Intent(this, UserService.class);
                intent.setAction(UserService.UPDATE_USER_INTRO);
                intent.putExtra(UserDatabase.USER_INTRODUCTION, newUserIntro);
                startService(intent);
            }
            break;
        }
    }

    @Override
    public void onBackPressed() {
        if (userEditingProfileIntro) {
            editIntroMode(false);
            userProfileIntroEditView.setText(prevUserIntro);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Receives the user image or background image from gallery as the "OnReceive"
        if ((requestCode == RESULT_LOAD_PROFILE_PIC || requestCode == RESULT_LOAD_BACKGROUND_PIC)
                && resultCode == RESULT_OK && data != null) {
            Log.d(TAG, "Inside on Activity Result");
            Uri selectedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePath, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePath[0]);
            String picturePath = cursor.getString(columnIndex);
            Bitmap userImage = BitmapFactory.decodeFile(picturePath);
            cursor.close();

            //Start intent to store the image into database and internal storage
            Intent storeImageIntent = new Intent(UserService.STORE_USER_PICTURE_TO_DATABASE_ACTION);
            storeImageIntent.setClass(this, UserService.class);

            if (requestCode == RESULT_LOAD_PROFILE_PIC) {
                //Set the image received as the current user image of the UserPageActivity
                userProfileImageView.setImageBitmap(userImage);
                //Store the image received in the internal storage
                InternalStorage.saveImageFile(this, InternalStorage.ImageType.PROFILE, userId, userImage);
                //Note that the image type is of profile picture
                storeImageIntent.putExtra(UserService.IMAGE_TYPE, UserService.USER_IMAGE_TYPE_PROFILE);
            } else {
                //Set the image received as the current user background image of the UserPageActivity
                backgroundImageView.setImageBitmap(userImage);
                //Store the image received in the internal storage
                InternalStorage.saveImageFile(this, InternalStorage.ImageType.BACKGROUND, userId, userImage);
                //Note that the image type is of background
                storeImageIntent.putExtra(UserService.IMAGE_TYPE, UserService.USER_IMAGE_TYPE_BACKGROUND);
            }
            //Start the service to store the images in both the database and the internal storage of the device
            startService(storeImageIntent);
        }
    }

    private void refreshUserInfo() {
        Intent intent = new Intent(UserPageActivity.this, UserService.class);
        intent.setAction(UserService.REFRESH_USER_PRIMITIVE_DATA_ACTION);
        startService(intent);
    }

    private void makeViewsVisible() {
        //First checks to see if all of the user informations are loaded before updating the views
        if (userProfileImageLoaded && userBackgroundImageLoaded && userPrimitiveDataLoaded) {
            progressBar.setVisibility(View.GONE);
            viewContainer.setVisibility(View.VISIBLE);
        }
    }

    private void editIntroMode(boolean onOrOff) {
        if (onOrOff) {
            //Save the current user intro in case they cancel
            prevUserIntro = userProfileIntroEditView.getText().toString();

            //Set the Edit Text to editable
            userProfileIntroEditView.setFocusable(true);
            userProfileIntroEditView.setFocusableInTouchMode(true);
            userProfileIntroEditView.setClickable(true);
            userProfileIntroEditView.requestFocus();

            //Make the soft-keyboard appear
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(userProfileIntroEditView, InputMethodManager.SHOW_IMPLICIT);

            //Make the edit button unclickable and also make it dissapear
            editIntroductionButton.setClickable(false);
            editIntroductionButton.setVisibility(View.GONE);

            //Make the save icon appear instead
            saveEditButton.setClickable(true);
            saveEditButton.setVisibility(View.VISIBLE);

            //Set that the user is currently editing their intro to modify onBackPressed
            userEditingProfileIntro = true;
        } else {
            //Make the save icon dissappear
            saveEditButton.setClickable(false);
            saveEditButton.setVisibility(View.GONE);

            //Make the edit button appear now
            editIntroductionButton.setClickable(true);
            editIntroductionButton.setVisibility(View.VISIBLE);

            //Set the Edit Text to not be able to edit
            userProfileIntroEditView.clearFocus();
            userProfileIntroEditView.setFocusable(false);
            userProfileIntroEditView.setFocusableInTouchMode(false);
            userProfileIntroEditView.setClickable(false);

            //Set that the user is no longer editing their intro to modify onBackPressed
            userEditingProfileIntro = false;
        }
    }

    /**
     * define the actiity's transitions
     */
    public void setTransitions() {
        Slide enterSlide = new Slide();
        Slide exitSlide = new Slide();
        enterSlide.setDuration(500);
        enterSlide.setSlideEdge(Gravity.START);
        exitSlide.setDuration(500);
        exitSlide.setSlideEdge(Gravity.RIGHT);
        getWindow().setExitTransition(exitSlide);
        getWindow().setEnterTransition(enterSlide);
        getWindow().setReenterTransition(enterSlide);
        getWindow().setReturnTransition(enterSlide);
    }

    public void getUserImagesUrl() {
        //Creates a reference for the location where the user media link is stored ordered by time
        DatabaseReference databaseReference = UserDatabase.getUserMediaListReference(userId);
        //Query all of the urls ordered by their keys
        urlQuery = databaseReference.orderByKey();
        urlQuery.addListenerForSingleValueEvent(valueEventListener);
    }

    //load pictures from the UrlList to the grid
    public void loadGrid(ArrayDeque<String> urlList) {
        String combinedString;
        String uriString;
        char uriType;
        int size = urlList.size();
        Log.d("urlList size", "" + size);

        //parse each url in UrlLIst
        //urls are appended with "i" or "v' for image or video
        for (int i = 0; i < size; i++) {
            combinedString = urlList.poll();
            Log.d("Combined String", combinedString);
            uriString = combinedString.substring(0, combinedString.length() - 2);
            Log.d("uriString", uriString);
            uriType = combinedString.charAt(combinedString.length() - 1);
            Log.d("uriType", "" + uriType);
            if (uriType == 'i') {
                imageAdapter.addImageUrlString(uriString);
            } else if (uriType == 'v') {
            }

            //prompt gridview to refresh views
            gridView.invalidateViews();
        }
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
