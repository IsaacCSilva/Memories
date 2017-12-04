package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.transition.Scene;
import android.transition.Slide;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;

/**
 * Created by Francisco on 11/22/2017.
 */

public class MyCoordinatorLayout extends android.support.design.widget.CoordinatorLayout {
    private float deltaX;
    private Intent leftPage;
    private Intent rightPage;
    private final float SWIPE_THRESHOLD = 50;

    public MyCoordinatorLayout(Context context, AttributeSet attrs){
        super(context, attrs);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e){
        if(e.getAction() == e.ACTION_MOVE) {
            if (e.getHistorySize() > 0) {
                deltaX = e.getX() - e.getHistoricalAxisValue(e.AXIS_X, 0);
                Log.d("Delta X of current swipe", "" + deltaX);
                if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                    return true;
                } else {
                    deltaX = 0;
                }
            }
        }
//        if(e.getAction() == e.ACTION_DOWN){
//            x1 = e.getX();
//        }
//        if(e.getAction() == e.ACTION_UP){
//            x2 = e.getX();
//            deltaX = x2-x1;
//            Log.d("Delta X of last swipe", "" + deltaX);
//            if(Math.abs(deltaX) > SWIPE_THRESHOLD){
//
//                return true;
//            }
//            else{
//                x1 = 0;
//                x2 = 0;
//                deltaX = 0;
//            }
//        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        if(e.getAction() == e.ACTION_MOVE && deltaX == 0) {
            if (e.getHistorySize() > 0) {
                deltaX = e.getX() - e.getHistoricalAxisValue(e.AXIS_X, 0);
                Log.d("Delta X of current swipe 2", "" + deltaX);
            }
        }

        Scene scene = new Scene(this);
        Intent intent = null;
        Slide slide = new Slide();
        if (deltaX > SWIPE_THRESHOLD) {
            if(leftPage != null) {
                intent = leftPage;
                intent.putExtra("slide edge", 1);
                slide.setSlideEdge(Gravity.RIGHT);
            }
        }
        else if(deltaX < SWIPE_THRESHOLD * -1){
            if(rightPage != null) {
                intent = rightPage;
                intent.putExtra("slide edge", 0);
                slide.setSlideEdge(Gravity.LEFT);
            }
        }
        if(deltaX > SWIPE_THRESHOLD || deltaX < SWIPE_THRESHOLD * -1) {
            if(intent != null) {
                Activity currentActivity = (Activity) getContext();
                currentActivity.getWindow().setExitTransition(slide);

                getContext().startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) getContext()).toBundle());
            }
        }
        deltaX = 0;
        return true;
    }

    public void setLeftPage(Intent leftPage){
        this.leftPage = leftPage;
    }

    public void setRightPage(Intent rightPage){
        this.rightPage = rightPage;
    }
}
