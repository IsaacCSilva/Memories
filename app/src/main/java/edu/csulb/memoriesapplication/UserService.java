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

    public final static String LOAD_USER_PROFILE_PICTURES_FROM_DATABASE_ACTION = "LUPPFD";
    public final static String REFRESH_USER_PRIMITIVE_DATA_ACTION = "RUPD";
    public final static String STORE_USER_PICTURE_TO_DATABASE_AND_INTERNAL_STORAGE_ACTION = "SUPTDAIS";
    public final static String USER_IMAGE_TYPE_PROFILE = "profileImage";
    public final static String USER_IMAGE_TYPE_BACKGROUND = "backgroundImage";
    private final String TAG = "UserService";

    private class StorageListener implements OnSuccessListener<byte[]>, OnFailureListener {

        private Bitmap image = null;

        @Override
        public void onSuccess(byte[] bytes) {
            image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            int errorCode = ((StorageException) exception).getErrorCode();
            switch (errorCode) {
                case StorageException.ERROR_OBJECT_NOT_FOUND: {
                    Log.d(TAG, "User File not found in storage.");
                }
                break;
                case StorageException.ERROR_CANCELED: {
                    Log.d(TAG, "User has canceled.");
                }
                break;
            }
            image = null;
        }

        private Bitmap getImage() {
            return image;
        }
    }

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
                //initialize connections
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                String userId = firebaseAuth.getCurrentUser().getUid();
                String userProfilePicPath = StorageReferenceKeys.USER_PROFILE_PIC_PATH + userId + ".png";
                String userBackgroundPicPath = StorageReferenceKeys.USER_BACKGROUND_PIC_PATH + userId + ".png";

                Bitmap userProfileImage = retrieveUserImageFromStorage(userProfilePicPath);
                Bitmap userBackgroundImage = retrieveUserImageFromStorage(userBackgroundPicPath);

                InternalStorage.saveImageFile(this, InternalStorage.ImageType.PROFILE, userId, userProfileImage);
                InternalStorage.saveImageFile(this, InternalStorage.ImageType.BACKGROUND, userId, userBackgroundImage);
            }
            break;
            //----------------------------------------------------------------------------------------------------------------------------------------------------
            //Stores the pictures provided in the intent to the database and to the internal storage of the device
            case STORE_USER_PICTURE_TO_DATABASE_AND_INTERNAL_STORAGE_ACTION: {

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


    private Bitmap retrieveUserImageFromStorage(String userImagePath) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final long MAX_IMAGE_SIZE = 1024 * 1024 * 10;

        StorageListener storageListener = new StorageListener();
        storageReference.child(userImagePath).getBytes(MAX_IMAGE_SIZE)
                .addOnSuccessListener(storageListener).addOnFailureListener(storageListener);
        return storageListener.getImage();
    }

}
