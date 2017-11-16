package edu.csulb.memoriesapplication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Daniel on 11/7/2017.
 */

public class UserDatabase {
    public final static String USERS = "Users";
    public final static String USER_INTRODUCTION = "USER_INTRODUCTION";

    public void updateUserIntroduction(String userId, String introduction) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(UserDatabase.USERS);
        DatabaseReference userIntroReference = databaseReference.child(userId).child("userIntro");
        userIntroReference.setValue(introduction);
    }
}
