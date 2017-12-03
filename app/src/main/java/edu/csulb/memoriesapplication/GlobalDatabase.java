package edu.csulb.memoriesapplication;

import android.net.Uri;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Daniel on 12/2/2017.
 */

public class GlobalDatabase {
    private final static String GLOBAL_MEDIA_REFERENCE = "globalMedia";
    private final static String URL_KEY = "url";
    private final static String MEDIA_TYPE_KEY = "mediaType";

    public enum MediaType{
        IMAGE,
        VIDEO
    }

    public static void addMediaUrl(Uri uri, MediaType mediaType) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(GLOBAL_MEDIA_REFERENCE);
        databaseReference = databaseReference.push();
        databaseReference.child(URL_KEY).setValue(uri.toString());
        if(mediaType == MediaType.IMAGE) {
            databaseReference.child(MEDIA_TYPE_KEY).setValue("image");
        } else {
            databaseReference.child(MEDIA_TYPE_KEY).setValue("video");
        }
    }

    public static DatabaseReference getMediaListReference() {
        return FirebaseDatabase.getInstance().getReference(GLOBAL_MEDIA_REFERENCE);
    }

    public static String getUrlKey() {
        return URL_KEY;
    }

    public static String getMediaTypeKey() { return MEDIA_TYPE_KEY; }

}
