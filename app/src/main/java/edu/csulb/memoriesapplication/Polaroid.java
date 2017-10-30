package edu.csulb.memoriesapplication;

import android.net.Uri;

/**
 * Created by Francisco on 10/28/2017.
 * Polaroid
 * This class is just a wrapper for
 * the video/image the polaroid will contain
 */

public class Polaroid {
    private Uri videoUri;
    private Uri imageUri;

    /**
     * Polaroid()
     * a polaroid contains either a video or an image, exclusively
     * only one of its parameters should be set, the other should be null
     * @param vidUri    The URI of the video
     * @param imgUri    The URI of the image
     */
    public Polaroid(Uri vidUri, Uri imgUri){
        if(vidUri != null){
            videoUri = vidUri;
        }
        else if(imgUri != null){
            imageUri = imgUri;
        }
    }

    public Uri getVideoUri(){
        return videoUri;
    }

    public Uri getImageUri(){
        return imageUri;
    }

}
