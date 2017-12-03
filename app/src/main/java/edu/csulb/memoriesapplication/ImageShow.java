package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by Isaac S on 12/2/2017.
 */

public class ImageShow extends Activity {
    private ImageView ivImage;
    private Button submitImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_image);

        submitImage = (Button) this.findViewById(R.id.progress_bar); // submit button
        ivImage = (ImageView) this.findViewById(R.id.ivImage);   // image set
    }
}
