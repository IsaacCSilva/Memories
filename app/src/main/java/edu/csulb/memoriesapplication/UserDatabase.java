package edu.csulb.memoriesapplication;

import android.net.Uri;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Daniel on 11/7/2017.
 */

public class UserDatabase {
    final static String USERS = "Users";
    final static String USER_INTRODUCTION = "USER_INTRODUCTION";
    private final static String USER_MEDIA_LIST = "mediaList";
    private final String USER_URL_KEY = "url";
    private final String USER_MEDIA_TYPE_KEY = "mediaType";

    public enum MediaType{
        IMAGE,
        VIDEO
    }

    void updateUserIntroduction(String userId, String introduction) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(USERS);
        DatabaseReference userIntroReference = databaseReference.child(userId).child("userIntro");
        userIntroReference.setValue(introduction);
    }

    void addMediaUrl(String userId, Uri uri, MediaType mediaType) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(USERS);
        incrementPostCount(userId);
        DatabaseReference userMediaListReference = databaseReference.child(userId).child(USER_MEDIA_LIST).push();
        userMediaListReference.child(USER_URL_KEY).setValue(uri.toString());
        if(mediaType == MediaType.IMAGE) {
            userMediaListReference.child(USER_MEDIA_TYPE_KEY).setValue("image");
        } else {
            userMediaListReference.child(USER_MEDIA_TYPE_KEY).setValue("video");
        }
    }

    private void incrementPostCount(String userId) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference(USERS).child(userId).child("userPostsCount");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long count =(Long) dataSnapshot.getValue();
                count++;
                databaseReference.setValue(count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
