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
import com.google.firebase.database.ValueEventListener;
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
    public final static String UPDATE_USER_INTRO = "UUI";
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
                Log.d(TAG, "Retrieving user profile picture from database");
                final String userId = getUserId();
                UserFirebaseStorage userFirebaseStorage = new UserFirebaseStorage();

                //Retrieves the user profile image from Storage and saves it into internal storage
                StorageReference storageReference = userFirebaseStorage.getRootReference();
                final long MAX_IMAGE_SIZE = 1024 * 1024 * 3;
                String profilePicPath = userFirebaseStorage.getUserProfilePicPath(userId);
                final Intent userImageLoadFinishedIntent = new Intent(BroadcastKey.USER_PROFILE_IMAGE_LOAD_FINISH_ACTION);

                storageReference.child(profilePicPath).getBytes(MAX_IMAGE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "User profile image received from storage successfully");
                        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        InternalStorage.saveImageFile(UserService.this, InternalStorage.ImageType.PROFILE, userId, image);
                        userImageLoadFinishedIntent.putExtra(BroadcastKey.RECEIVED_IMAGE,true);

                        //Tell the UI Thread that the profile image has been received successfully
                        LocalBroadcastManager.getInstance(UserService.this).sendBroadcast(userImageLoadFinishedIntent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        userImageLoadFinishedIntent.putExtra(BroadcastKey.RECEIVED_IMAGE, false);
                        int errorCode = ((StorageException) e).getErrorCode();
                        switch(errorCode) {
                            case StorageException.ERROR_OBJECT_NOT_FOUND: {
                                Log.d(TAG, "User File not found in storage.");
                            }
                            break;
                            case StorageException.ERROR_CANCELED: {
                                Log.d(TAG, "User has canceled.");
                            }
                            break;
                        }
                        LocalBroadcastManager.getInstance(UserService.this).sendBroadcast(userImageLoadFinishedIntent);
                    }
                });

            }
            break;
            case LOAD_USER_BACKGROUND_PICTURES_FROM_DATABASE_ACTION: {
                Log.d(TAG, "Retrieving user profile picture from database");
                final String userId = getUserId();
                UserFirebaseStorage userFirebaseStorage = new UserFirebaseStorage();

                //Retrieves the user background image from Storage and saves it into internal storage
                StorageReference storageReference = userFirebaseStorage.getRootReference();
                final long MAX_IMAGE_SIZE = 1024 * 1024 * 3;
                String backgroundPicPath = userFirebaseStorage.getUserBackgroundPicPath(userId);
                final Intent userImageLoadFinishedIntent = new Intent(BroadcastKey.USER_BACKGROUND_IMAGE_LOAD_FINISH_ACTION);

                storageReference.child(backgroundPicPath).getBytes(MAX_IMAGE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "User background image received from storage successfully");
                        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        InternalStorage.saveImageFile(UserService.this, InternalStorage.ImageType.BACKGROUND, userId, image);
                        userImageLoadFinishedIntent.putExtra(BroadcastKey.RECEIVED_IMAGE,true);

                        //Tell the UI Thread that the background image has been received successfully
                        LocalBroadcastManager.getInstance(UserService.this).sendBroadcast(userImageLoadFinishedIntent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        userImageLoadFinishedIntent.putExtra(BroadcastKey.RECEIVED_IMAGE, false);
                        int errorCode = ((StorageException) e).getErrorCode();
                        switch(errorCode) {
                            case StorageException.ERROR_OBJECT_NOT_FOUND: {
                                Log.d(TAG, "User File not found in storage.");
                            }
                            break;
                            case StorageException.ERROR_CANCELED: {
                                Log.d(TAG, "User has canceled.");
                            }
                            break;
                        }
                        LocalBroadcastManager.getInstance(UserService.this).sendBroadcast(userImageLoadFinishedIntent);
                    }
                });
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
                com.google.firebase.database.FirebaseDatabase firebaseDatabase = com.google.firebase.database.FirebaseDatabase.getInstance();
                firebaseDatabase.getReference().child(UserDatabase.USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
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
            case UPDATE_USER_INTRO: {
                String userIntro = intent.getStringExtra(UserDatabase.USER_INTRODUCTION);
                String userId = getUserId();
                UserDatabase userDatabase = new UserDatabase();
                userDatabase.updateUserIntroduction(userId, userIntro);
            }
            break;
        }
    }

    private String getUserId() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        return firebaseAuth.getCurrentUser().getUid();
    }


}
