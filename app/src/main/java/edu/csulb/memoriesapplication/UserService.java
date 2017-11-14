package edu.csulb.memoriesapplication;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

/**
 * Created by Daniel on 11/1/2017.
 */

public class UserService extends IntentService {

    public final static String LOAD_USER_PROFILE_PICTURES_FROM_DATABASE_ACTION = "LUPPFDA";
    public final static String LOAD_USER_BACKGROUND_PICTURES_FROM_DATABASE_ACTION = "LUBPFDA";
    public final static String REFRESH_USER_PRIMITIVE_DATA_ACTION = "RUPD";
    public final static String STORE_USER_PICTURE_TO_DATABASE = "SUPTD";
    public final static String IMAGE_TYPE = "image_type";
    public final static String USER_IMAGE_TYPE_PROFILE = "profileImage";
    public final static String USER_IMAGE_TYPE_BACKGROUND = "backgroundImage";
    private final String TAG = "UserService";


    public UserService() {
        super("UserService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        switch (intent.getAction()) {
            //Retrieves user background picture and user profile picture from the database to be displayed in the user page
            //And then stores them in the internal storage for easy and quick access
            case LOAD_USER_PROFILE_PICTURES_FROM_DATABASE_ACTION: {
                String userId = getUserId();

                UserFirebaseStorage userFirebaseStorage = new UserFirebaseStorage();
                Bitmap userProfileImage = userFirebaseStorage.getUserImage(userId, UserFirebaseStorage.ImageType.PROFILE);

                Intent userImageLoadFinishedIntent = new Intent(BroadcastKey.USER_PROFILE_IMAGE_LOAD_FINISH_ACTION);
                if (userProfileImage != null) {
                    InternalStorage.saveImageFile(this, InternalStorage.ImageType.PROFILE, userId, userProfileImage);
                    //Record in the intent that we were able to find a profile picture file
                    userImageLoadFinishedIntent.putExtra(BroadcastKey.RECEIVED_IMAGE, true);
                }

                LocalBroadcastManager.getInstance(UserService.this).sendBroadcast(userImageLoadFinishedIntent);
            }
            break;
            case LOAD_USER_BACKGROUND_PICTURES_FROM_DATABASE_ACTION: {
                String userId = getUserId();

                UserFirebaseStorage userFirebaseStorage = new UserFirebaseStorage();
                Bitmap userBackgroundImage = userFirebaseStorage.getUserImage(userId, UserFirebaseStorage.ImageType.BACKGROUND);

                Intent userImageLoadFinishedIntent = new Intent(BroadcastKey.USER_BACKGROUND_IMAGE_LOAD_FINISH_ACTION);
                if (userBackgroundImage != null) {
                    InternalStorage.saveImageFile(this, InternalStorage.ImageType.BACKGROUND, userId, userBackgroundImage);
                    //Record in the intent that we were able to find a background picture file
                    userImageLoadFinishedIntent.putExtra(BroadcastKey.RECEIVED_IMAGE, true);
                }

                LocalBroadcastManager.getInstance(UserService.this).sendBroadcast(userImageLoadFinishedIntent);
            }
            break;
            //----------------------------------------------------------------------------------------------------------------------------------------------------
            //Stores the pictures provided in the intent to the database and to the internal storage of the device
            case STORE_USER_PICTURE_TO_DATABASE: {
                String userId = getUserId();
                UserFirebaseStorage userFirebaseStorage = new UserFirebaseStorage();

                if (intent.getStringExtra(IMAGE_TYPE).equals(USER_IMAGE_TYPE_PROFILE)) {
                    //Image is the user's profile image, save it accordingly to the internal storage and database
                    Log.d(TAG, "Saving user profile image to FirebaseStorage");
                    //Retrieve the image from internal storage
                    Bitmap image = InternalStorage.getProfilePic(this, userId);
                    userFirebaseStorage.saveImageFile(userId, image, UserFirebaseStorage.ImageType.PROFILE);
                } else if (intent.getStringExtra(IMAGE_TYPE).equals(USER_IMAGE_TYPE_BACKGROUND)) {
                    //Image is the user's background image, save it accordingly to the internal storage and database
                    Log.d(TAG, "Saving user background image to FirebaseStorage");
                    //Retrieve the image from internal storage
                    Bitmap image = InternalStorage.getBackgroundPic(this, userId);
                    userFirebaseStorage.saveImageFile(userId, image, UserFirebaseStorage.ImageType.BACKGROUND);
                }
            }
            break;
            //-----------------------------------------------------------------------------------------------------------------------------------------------------
            //Reloads all of the user primitive data such as their name, posts count, and account introduction
            case REFRESH_USER_PRIMITIVE_DATA_ACTION: {
                final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                final String userId = firebaseAuth.getCurrentUser().getUid();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                firebaseDatabase.getReference().child(DatabaseReferenceKeys.USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        Log.d(TAG, "Refreshing user info for " + user.userID);
                        Intent userRefreshFinishedIntent = new Intent(BroadcastKey.USER_INFO_REFRESH_FINISH_ACTION);
                        userRefreshFinishedIntent.putExtra(BroadcastKey.USER_OBJECT, user);
                        LocalBroadcastManager.getInstance(UserService.this).sendBroadcast(userRefreshFinishedIntent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            break;

        }
    }

    private String getUserId() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        return firebaseAuth.getCurrentUser().getUid();
    }


}
