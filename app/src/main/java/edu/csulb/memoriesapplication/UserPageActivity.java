package edu.csulb.memoriesapplication;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Danie on 10/24/2017.
 */

public class UserPageActivity extends Activity implements View.OnClickListener {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    private final int RESULT_LOAD_IMAGE = 1;
    DatabaseReference databaseReference;
    final String TAG = "UserPageActivity";
    final int REQUEST_CODE = 1052;
    private final String USER_SETTINGS = "User Settings";
    private final String READ_PERMISSION = "Read_external_storage_permission_granted";
    private boolean readPermissionGranted;
    private CircleImageView userImage;

    //TODO: store the picture in internal storage and also in the database in case they log in through a different computer
    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        userImage = (CircleImageView) this.findViewById(R.id.user_profile_picture);
        userImage.setOnClickListener(this);

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
                    startActivityForResult(intent, RESULT_LOAD_IMAGE);
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Log.d(TAG, "Inside on Activity Result");
            Uri selectedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePath, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePath[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            userImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }
}
