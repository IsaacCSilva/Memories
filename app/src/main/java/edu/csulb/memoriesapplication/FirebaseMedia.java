package edu.csulb.memoriesapplication;

import android.support.annotation.NonNull;

/**
 * Created by Daniel on 12/7/2017.
 * Due to firebase realtime database's constraints on their queries, this class was made
 * to by pass and further the filter client side
 */

public class FirebaseMedia implements Comparable<FirebaseMedia> {
    private String url;
    private long likesCount;

    FirebaseMedia(String url, long likesCount) {
        this.url = url;
        this.likesCount = likesCount;
    }

    public void attachMediaType(char mediaTypeCharacter) {
        url = url + mediaTypeCharacter;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int compareTo(@NonNull FirebaseMedia o) {
        if(o.likesCount == this.likesCount) {
            return 0;
        } else if (this.likesCount > o.likesCount) {
            return -1;
        } else {
            return 1;
        }
    }
}
