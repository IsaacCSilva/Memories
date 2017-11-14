package edu.csulb.memoriesapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/**
 * Created by Daniel on 11/7/2017.
 */

public class UserFirebaseStorage {
    public final static String USER_PROFILE_PIC_PATH = "user_images/user_profile_";
    public final static String USER_BACKGROUND_PIC_PATH = "user_images/user_background_";
    public final static String IMAGE_EXTENSION_TYPE = ".png";
    private final String TAG = "UserFirebaseStorage";

    public enum ImageType{
        PROFILE,
        BACKGROUND
    }

    private class StorageListener implements OnSuccessListener<byte[]>, OnFailureListener {

        private Bitmap image;

        public StorageListener() {
            image = null;
        }

        @Override
        public void onSuccess(byte[] bytes) {
            image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            int errorCode = ((StorageException) exception).getErrorCode();
            switch (errorCode) {
                case StorageException.ERROR_OBJECT_NOT_FOUND: {
                    Log.d(TAG, "User File not found in storage.");
                }
                break;
                case StorageException.ERROR_CANCELED: {
                    Log.d(TAG, "User has canceled.");
                }
                break;
            }
            image = null;
        }

        private Bitmap getImage() {
            return image;
        }
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

    public Bitmap getUserImage(String userId, ImageType imageType) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final long MAX_IMAGE_SIZE = 1024 * 1024 * 3;

        String imageFilePath = null;
        if(imageType == ImageType.PROFILE) {
            imageFilePath = USER_PROFILE_PIC_PATH + userId + IMAGE_EXTENSION_TYPE;
        } else {
            imageFilePath = USER_BACKGROUND_PIC_PATH + userId + IMAGE_EXTENSION_TYPE;
        }

        StorageListener storageListener = new StorageListener();

        storageReference.child(imageFilePath).getBytes(MAX_IMAGE_SIZE)
                .addOnSuccessListener(storageListener)
                .addOnFailureListener(storageListener);

        return storageListener.getImage();
    }


}
