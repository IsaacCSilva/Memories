package edu.csulb.memoriesapplication;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Danie on 10/25/2017.
 */

@IgnoreExtraProperties
public class User {
    public String email;
    public String name;
    public String userIntro;
    public String userID;
    public int followersCount;
    public int followingCount;

    public User() {
        //Default Constructor for specific write cases
    }

    public User(String userEmail, String userName) {
        email = userEmail;
        name = userName;
        userID = userEmail;
        userIntro = "";
        followersCount = 0;
        followingCount = 0;
    }
}
