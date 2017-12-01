package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

/**
 * Created by Daniel on 11/30/2017.
 */

public class AddPictureTestActivity extends Activity {
    private final static int RESULT_LOAD_MEDIA = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_add_media_activity);
        Button button = (Button) this.findViewById(R.id.add_picture_test_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Starts the activity to load in the images from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/* video/*");
                startActivityForResult(intent, RESULT_LOAD_MEDIA);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_MEDIA && resultCode == RESULT_OK && data != null) {
            Uri selectedMedia = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedMedia, filePath, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePath[0]);
            String mediaPath = cursor.getString(columnIndex);
            //Media Received, check if it is an image or a video
            if(selectedMedia.toString().contains("image")){
                Bitmap image = BitmapFactory.decodeFile(mediaPath);
                FirebaseMediaStorage firebaseMediaStorage = new FirebaseMediaStorage();
                firebaseMediaStorage.saveImageToFirebaseStorage(image);
            } else if (selectedMedia.toString().contains("video")) {
                Toast.makeText(this, "Video", Toast.LENGTH_SHORT).show();
            }


//            Cursor cursor = getContentResolver().query(selectedMedia, filePath, null, null, null);
//            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePath[0]);
//            String mediaPath = cursor.getString(columnIndex);

        }
    }
}
