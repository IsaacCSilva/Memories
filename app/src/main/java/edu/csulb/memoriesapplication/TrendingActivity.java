package edu.csulb.memoriesapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;


import java.util.ArrayList;

/**
 * Created by Danie on 10/16/2017.
 */

public class TrendingActivity extends AppCompatActivity {

    //added
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private MediaController mediaController;
    private ArrayList<Polaroid> polaroids;
    private CardViewAdapter rvAdapter;
    private MyConstraintLayout constraintLayout;

    static final int CAM_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        //set transitions
        setTransitions();

        //instantiate objects
        constraintLayout = (MyConstraintLayout) findViewById(R.id.constraintLayout);
        constraintLayout.setLeftPage(new Intent(this, UserPageActivity.class));
        constraintLayout.setRightPage(new Intent(this, LatestMemoriesActivity.class));
        polaroids = new ArrayList<Polaroid>();
        rvAdapter = new CardViewAdapter(this, polaroids);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int position1 = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                int position2 = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                Log.d("first completely visible item position", "" + position1);
                if (position1 != -1) {
                    View view = (View) ((LinearLayoutManager) recyclerView.getLayoutManager()).findViewByPosition(position1);
                    if (view instanceof CardView) {
                        Log.d("View is of type", "CardView");
                        View childView = (View) ((CardView) view).getChildAt(0);
                        if (childView instanceof RelativeLayout) {
                            VideoView videoView = (VideoView) ((RelativeLayout) childView).getChildAt(0);
                            videoView.start();
                        }
                    }
                    if (position2 != -1 && position2 != position1) {
                        Log.d("first visible item postion", "" + position2);
                        View view2 = (View) ((LinearLayoutManager) recyclerView.getLayoutManager()).findViewByPosition(position2);
                        View childView = (View) ((CardView) view2).getChildAt(0);
                        if (childView instanceof RelativeLayout) {
                            VideoView videoView = (VideoView) ((RelativeLayout) childView).getChildAt(0);
                            if (videoView.isPlaying()) {
                                videoView.pause();
                            }
                        }
                    }
                }
            }
        });

        Uri uri = Uri.parse("http://webm.land/media/Qn8D.webm");
        Polaroid polaroid = new Polaroid(uri, null);

        polaroids.add(polaroid);

        uri = Uri.parse("http://i646.photobucket.com/albums/uu187/jess_roces/animal11.jpg");
        polaroid = new Polaroid(null, uri);
        polaroids.add(polaroid);
        polaroids.add(polaroid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem items) {
        switch (items.getItemId()) {
            case R.id.action_settings: {
                final Intent intent = new Intent(TrendingActivity.this, Setting.class);
                startActivity(intent);
                Toast.makeText(getBaseContext(), "Information: ", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.action_cam: {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera_intent, CAM_REQUEST);
                return true;
            }
        }
        return super.onOptionsItemSelected(items);
    }


    public void setTransitions() {
        Slide enterSlide = new Slide();
        Slide returnSlide = new Slide();
        enterSlide.setDuration(500);
        enterSlide.setSlideEdge(Gravity.BOTTOM);
        returnSlide.setDuration(500);
        returnSlide.setSlideEdge(Gravity.START);
        getWindow().setExitTransition(null);
        getWindow().setEnterTransition(enterSlide);
        getWindow().setReenterTransition(enterSlide);
        getWindow().setReturnTransition(returnSlide);
    }

//    @Override
//    public void onBackPressed(){
//        super.onBackPressed();
//        Slide slide = new Slide();
//        slide.setDuration(500);
//        slide.setSlideEdge(Gravity.RIGHT);
//        getWindow().setExitTransition(slide);
//    }
}