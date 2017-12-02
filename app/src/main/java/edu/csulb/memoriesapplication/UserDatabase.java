package edu.csulb.memoriesapplication;

import android.net.Uri;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Daniel on 11/7/2017.
 */

public class UserDatabase {
    final static String USERS = "Users";
    final static String USER_INTRODUCTION = "USER_INTRODUCTION";
    private final static String USER_MEDIA_LIST = "mediaList";
    private final String USER_URL_KEY = "url";

    void updateUserIntroduction(String userId, String introduction) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(USERS);
        DatabaseReference userIntroReference = databaseReference.child(userId).child("userIntro");
        userIntroReference.setValue(introduction);
    }

    void addMediaUrl(String userId, Uri uri) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(USERS);
        DatabaseReference userMediaListReference = databaseReference.child(userId).child(USER_MEDIA_LIST).push();
        userMediaListReference.child(USER_URL_KEY).setValue(uri.toString());
    }
}
