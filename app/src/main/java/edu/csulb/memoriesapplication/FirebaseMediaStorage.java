package edu.csulb.memoriesapplication;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/**
 * Created by Daniel on 11/30/2017.
 */

public class FirebaseMediaStorage {
    private final static String MEDIA = "media";
    private final String TAG = "FirebaseMediaStorage";

    public void saveImageToFirebaseStorage(Bitmap image){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        //Create a reference to the folder where every media should be saved
        StorageReference storageReference = firebaseStorage.getReference().child(MEDIA);

        //Create the data that is going to be output to Firebase Storage
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteOutput = byteArrayOutputStream.toByteArray();

        //Upload it to Firebase Storage
        UploadTask uploadTask = storageReference.putBytes(byteOutput);
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
