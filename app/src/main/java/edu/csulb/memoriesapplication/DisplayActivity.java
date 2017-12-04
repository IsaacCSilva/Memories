package edu.csulb.memoriesapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class DisplayActivity extends AppCompatActivity implements View.OnClickListener{

    Button submitButton;
    private String TAG  = "DisplayActivity";
    Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Intent intent = getIntent();
        //Set image value
        String imageReceived = intent.getStringExtra("filepath");
        image = BitmapFactory.decodeFile(imageReceived);
        initializeImageThumbnail(intent);
        submitButton = (Button) this.findViewById(R.id.submitMediaButton);
        submitButton.setOnClickListener(this);
    }

    private void initializeImageThumbnail(Intent intent) {
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(image);
    }

    @Override
    public void onClick(View view) {
        if(image != null) {
            FirebaseMediaStorage firebaseMediaStorage = new FirebaseMediaStorage();
            firebaseMediaStorage.saveImageToFirebaseStorage(image);
            finish();
        }
    }
}
