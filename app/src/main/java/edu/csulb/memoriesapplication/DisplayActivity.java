package edu.csulb.memoriesapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class DisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);

        String imageReceived = getIntent().getStringExtra("filepath");

        Bitmap bmImg = BitmapFactory.decodeFile(imageReceived);
        imageView.setImageBitmap(bmImg);


    }
}
