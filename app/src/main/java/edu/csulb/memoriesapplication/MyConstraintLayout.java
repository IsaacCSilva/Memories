package edu.csulb.memoriesapplication;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by Francisco on 11/8/2017.
 */

public class MyConstraintLayout extends ConstraintLayout {
    private GestureDetectorCompat gestureDetectorCompat;
    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onDown(MotionEvent e){
            return true;
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Intent intent;
            if (e2.getX() < e1.getX()) {
                intent = new Intent(getContext(), UserPageActivity.class);
            }
            else{
                intent = new Intent(getContext(), LatestMemoriesActivity.class);
            }
            getContext().startActivity(intent);
            return true;
        }
    };
    public MyConstraintLayout(Context context, AttributeSet attrs){
        super(context, attrs);

        gestureDetectorCompat = new GestureDetectorCompat(getContext(), gestureListener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e){
        this.gestureDetectorCompat.onTouchEvent(e);
        return false;
    }
}
