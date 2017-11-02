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
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
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
    private TextView postsCountText;
    private TextView profileInformationTextView;
    private TextView userNameTextView;
    private TextView userIdTextView;

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

        postsCountText = (TextView) this.findViewById(R.id.posts_count_text);
        profileInformationTextView = (TextView) this.findViewById(R.id.profile_information_text);
        userNameTextView = (TextView) this.findViewById(R.id.user_name_text);
        userIdTextView = (TextView) this.findViewById(R.id.user_id_text);


        loadUser();
    }

    private void loadUser() {
        //Load in info from internal storage after login finishes it's service
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
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
            StorageReference storageReference = firebaseStorage.getReference()
                    .child("user_images/user_profile_" + mAuth.getCurrentUser().getUid() + ".png");
            userImage.setDrawingCacheEnabled(true);
            userImage.buildDrawingCache();
            Bitmap bitmapCache = userImage.getDrawingCache();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapCache.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] outputData = byteArrayOutputStream.toByteArray();

            UploadTask uploadTask = storageReference.putBytes(outputData);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
