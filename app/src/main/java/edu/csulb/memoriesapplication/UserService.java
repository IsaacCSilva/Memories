package edu.csulb.memoriesapplication;

import android.app.IntentService;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Daniel on 11/1/2017.
 */

public class UserService extends IntentService {

    public final static String LOAD_USER_DATA_FROM_DATABASE = "LUDFD";
    public final static String REFRESH_USER_PRIMITIVE_DATA = "RUPD";
    public final static int USER_NAME = 0;
    public final static int USER_INTRO = 1;
    public final static int USER_ID = 2;
    public final static int USER_POSTS_COUNT = 3;
    private final String TAG = "UserService";
    final String USER_IMAGES_FOLDER  = "user_images";

    public UserService(){
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
        switch(intent.getAction()) {
            case LOAD_USER_DATA_FROM_DATABASE:{
                //initialize connections
                final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference storageReference = firebaseStorage.getReference();

                storageReference = storageReference.child("user_images/user_profile_" +
                        firebaseAuth.getCurrentUser().getUid() + ".png");
                final long TEN_MEGABYTES = 1024*1024*10;
                storageReference.getBytes(TEN_MEGABYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
                        File userImagePath = contextWrapper.getDir(USER_IMAGES_FOLDER, Context.MODE_PRIVATE);
                        String userProfileFileName = "profile_pic_" + firebaseAuth.getCurrentUser().getUid() + ".png";
                        File userProfileFile = new File(userImagePath, userProfileFileName);
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(userProfileFile);
                            Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                            fileOutputStream.close();
                        }catch(FileNotFoundException exception) {
                            exception.printStackTrace();
                        }catch(IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        int errorCode = ((StorageException) exception).getErrorCode();
                        switch(errorCode){
                            case StorageException.ERROR_OBJECT_NOT_FOUND:{
                                Log.d(TAG, "User File not found in storage.");
                            }
                            break;
                            case StorageException.ERROR_CANCELED:{
                                Log.d(TAG, "User has canceled.");
                            }
                            break;
                        }
                    }
                });
            }
            break;
            case REFRESH_USER_PRIMITIVE_DATA: {
                final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                final String userId = firebaseAuth.getCurrentUser().getUid();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                firebaseDatabase.getReference().child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
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
}
