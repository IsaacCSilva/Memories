package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Danie on 10/24/2017.
 */

public class UserPageActivity extends Activity implements View.OnClickListener{

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    private final int RESULT_LOAD_IMAGE = 1;
    DatabaseReference databaseReference;

    CircleImageView userImage;

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        userImage = (CircleImageView) this.findViewById(R.id.user_profile_picture);
        userImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.user_profile_picture:{
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }break;
        }
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String [] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePath, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePath[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            userImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }
}
