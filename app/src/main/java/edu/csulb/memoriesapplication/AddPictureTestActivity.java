package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

/**
 * Created by Daniel on 11/30/2017.
 */

public class AddPictureTestActivity extends Activity {
    private final static int RESULT_LOAD_PROFILE_PIC = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button button = (Button) this.findViewById(R.id.add_picture_test_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_PROFILE_PIC);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_PROFILE_PIC && resultCode == RESULT_OK && data != null) {
            Uri selectedMedia = data.getData();

//            Cursor cursor = getContentResolver().query(selectedMedia, filePath, null, null, null);
//            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePath[0]);
//            String mediaPath = cursor.getString(columnIndex);

        }
    }
}
