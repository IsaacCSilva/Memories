package edu.csulb.memoriesapplication;

import android.app.IntentService;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

    public final static String LOAD_USER_DATA = "LOAD_USER_DATA";
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
            case LOAD_USER_DATA:{
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
        }
    }
}
