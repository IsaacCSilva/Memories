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
 * Created by Daniel on 11/7/2017.
 */

public class UserFirebaseStorage {
    private final static String USER_PROFILE_PIC_PATH = "user_images/user_profile_";
    private final static String USER_BACKGROUND_PIC_PATH = "user_images/user_background_";
    private final static String IMAGE_EXTENSION_TYPE = ".png";
    private final String TAG = "UserFirebaseStorage";

    public enum ImageType{
        PROFILE,
        BACKGROUND
    }

    public void saveImageFile(String userId, Bitmap image, ImageType imageType){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = null;
        if(imageType == ImageType.PROFILE) {
            storageReference = firebaseStorage.getReference().child(USER_PROFILE_PIC_PATH +
                    userId + IMAGE_EXTENSION_TYPE);
        } else {
            storageReference = firebaseStorage.getReference().child(USER_BACKGROUND_PIC_PATH +
                    userId + IMAGE_EXTENSION_TYPE);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
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


    public StorageReference getRootReference() {
        return FirebaseStorage.getInstance().getReference();
    }

    public String getUserProfilePicPath(String userId) {
        return USER_PROFILE_PIC_PATH + userId + IMAGE_EXTENSION_TYPE;
    }

    public String getUserBackgroundPicPath(String userId) {
        return USER_BACKGROUND_PIC_PATH + userId + IMAGE_EXTENSION_TYPE;
    }

}
