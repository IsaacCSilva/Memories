package edu.csulb.memoriesapplication;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Daniel on 11/7/2017.
 */

public class InternalStorage {
    private static final String USER_IMAGES_FOLDER = "user_images";
    private static final String IND_USER_PROFILE_PIC = "profile_pic_";
    private static final String IND_USER_BACKGROUND_PIC = "background_pic_";
    private static final String IMAGE_EXTENSION = ".png";

    public enum ImageType {
        PROFILE,
        BACKGROUND
    }

    public static void saveImageFile(Context context, ImageType imageType, String userId, Bitmap image) {
        File imageStoragePath = internalUserImageStoragePath(context);
        String fileName = null;
        if (imageType == ImageType.PROFILE) {
            fileName = IND_USER_PROFILE_PIC + userId + IMAGE_EXTENSION;
        } else if (imageType == ImageType.BACKGROUND) {
            fileName = IND_USER_BACKGROUND_PIC + userId + IMAGE_EXTENSION;
        }
        File toSaveImagePath = new File(imageStoragePath, fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(toSaveImagePath);
            image.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static Bitmap getProfilePic(Context context, String userId) {
        File imageStoragePath = internalUserImageStoragePath(context);
        String imageName = IND_USER_PROFILE_PIC + userId + IMAGE_EXTENSION;
        File userImage = new File(imageStoragePath, imageName);
        if (userImage.exists()) {
            Bitmap userProfilePicture = BitmapFactory.decodeFile(userImage.getAbsolutePath());
            return userProfilePicture;
        }
        return null;
    }

    public static Bitmap getBackgroundPic(Context context, String userId) {
        File imageStoragePath = internalUserImageStoragePath(context);
        String imageName = IND_USER_BACKGROUND_PIC + userId + IMAGE_EXTENSION;
        File userImage = new File(imageStoragePath, imageName);
        if (userImage.exists()) {
            Bitmap userBackgroundPicture = BitmapFactory.decodeFile(userImage.getAbsolutePath());
            return userBackgroundPicture;
        }
        return null;
    }


    private static File internalUserImageStoragePath(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File path = contextWrapper.getDir(USER_IMAGES_FOLDER, Context.MODE_PRIVATE);
        return path;
    }
}
