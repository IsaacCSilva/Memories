package edu.csulb.memoriesapplication;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;
/**
 * Created by Danie on 10/25/2017.
 */

@IgnoreExtraProperties
public class User implements Parcelable{
    public String email;
    public String name;
    public String userIntro;
    public String userID;
    public int userPostsCount;

    public User() {
        //Default Constructor for specific write cases
    }

    public User(String userEmail, String userName) {
        email = userEmail;
        name = userName;
        userID = userEmail;
        userIntro = "";
        userPostsCount = 0;
    }

    public User(Parcel in) {
        this.email = in.readString();
        this.name = in.readString();
        userIntro = in.readString();
        userID = in.readString();
        userPostsCount = in.readInt();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userPostsCount);
        dest.writeString(this.email);
        dest.writeString(this.name);
        dest.writeString(this.userIntro);
        dest.writeString(this.userID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
