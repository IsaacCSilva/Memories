package edu.csulb.memoriesapplication;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Danie on 10/24/2017.
 */

public class UserPageActivity extends Activity implements View.OnClickListener {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    private final int RESULT_LOAD_PROFILE_PIC = 1;
    DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    final String TAG = "UserPageActivity";
    String userProfileFileName;
    File userImagePath;
    final String USER_IMAGES_FOLDER  = "user_images";
    final int REQUEST_CODE = 1052;
    private final String USER_SETTINGS = "User Settings";
    private final String READ_PERMISSION = "Read_external_storage_permission_granted";
    private boolean readPermissionGranted;
    private CircleImageView userImage;

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        userImagePath = contextWrapper.getDir(USER_IMAGES_FOLDER, Context.MODE_PRIVATE);
        userProfileFileName = "profile_pic_" + mAuth.getCurrentUser().getUid() + ".png";

        userImage = (CircleImageView) this.findViewById(R.id.user_profile_picture);
        userImage.setOnClickListener(this);
        //Check if user image profile exists
        File userProfileFile = new File(userImagePath, userProfileFileName);
        if(userProfileFile.exists()) {
            try {
                Bitmap userProfilePicture = BitmapFactory.decodeStream(new FileInputStream(userProfileFile));
                userImage.setImageBitmap(userProfilePicture);
            }catch(FileNotFoundException exception) {
                exception.printStackTrace();
            }
        }

        SharedPreferences sharedPreferences = getSharedPreferences(USER_SETTINGS, 0);
        readPermissionGranted = sharedPreferences.getBoolean(READ_PERMISSION, false);
    }

    //Ask the user if we are able to store a picture with their permission during runtime
    private void checkPermissions() {
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
                if (grantResults.length > 0) {
                    readPermissionGranted = true;
                    SharedPreferences sharedPreferences = getSharedPreferences(USER_SETTINGS, 0);
                    SharedPreferences.Editor userSettings = sharedPreferences.edit();
                    userSettings.putBoolean(READ_PERMISSION, true);
                    userSettings.commit();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_profile_picture: {
                if (!readPermissionGranted) {
                    checkPermissions();
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RESULT_LOAD_PROFILE_PIC);
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_PROFILE_PIC && resultCode == RESULT_OK && data != null) {
            Log.d(TAG, "Inside on Activity Result");
            Uri selectedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePath, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePath[0]);
            String picturePath = cursor.getString(columnIndex);
            Bitmap userProfileImage = BitmapFactory.decodeFile(picturePath);
            cursor.close();

            String userId = mAuth.getCurrentUser().getUid();
            File imagePath = new File(userImagePath, userProfileFileName);

            try{
                FileOutputStream fileOutputStream = new FileOutputStream(imagePath);
                userProfileImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                userImage.setImageBitmap(userProfileImage);
                fileOutputStream.close();
            }catch(FileNotFoundException exception) {
                exception.printStackTrace();
            }catch(IOException exception) {
                exception.printStackTrace();
            }
            //TODO: store the image you just received into firebase storage

        }
    }
}
