package edu.csulb.memoriesapplication;

import android.content.Intent;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

public class DisplayActivity extends AppCompatActivity implements View.OnClickListener{

    private Button submitButton;
    private String TAG  = "DisplayActivity";
    private String imagePath;
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Intent intent = getIntent();
        //Set image value
        imagePath = intent.getStringExtra("filepath");
        // Decode file into Bitmap
        image = BitmapFactory.decodeFile(imagePath);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        initializeImageThumbnail(intent);
        submitButton = (Button) this.findViewById(R.id.submitMediaButton);
        submitButton.setOnClickListener(this);
    }

    private void initializeImageThumbnail(Intent intent) {
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        Glide.with(this)
                .load(imagePath)
                .bitmapTransform(new RotateTransformation(this, 90), new CenterCrop(this))
                .placeholder(R.color.cardview_dark_background)
                .into(imageView);
    }

    @Override
    public void onClick(View view) {
        if(image != null) {
            Intent intent = new Intent(this, UserService.class);
            intent.setAction(UserService.STORE_MEMORY_IMAGE_TO_DATABASE_ACTION);
            intent.putExtra("filepath", imagePath);
            startService(intent);
            finish();
        }
    }
}
