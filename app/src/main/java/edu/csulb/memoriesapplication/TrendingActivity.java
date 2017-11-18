package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * Created by Danie on 10/16/2017.
 */

public class TrendingActivity extends Activity implements View.OnClickListener{

    private TextView text;
    private Button button;
    private FirebaseAuth mAuth;
    private final String USER_INFO = "user_info";

    //added
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private MediaController mediaController;
    private ArrayList<Polaroid> polaroids;
    private CardViewAdapter rvAdapter;
    private MyConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        //set transitions
        setTransitions();

        //instantiate objects
        constraintLayout = (MyConstraintLayout)findViewById(R.id.constraintLayout);
        constraintLayout.setLeftPage(new Intent(this, UserPageActivity.class));
        constraintLayout.setRightPage(new Intent(this, LatestMemoriesActivity.class));
        polaroids = new ArrayList<Polaroid>();
        rvAdapter = new CardViewAdapter(this, polaroids);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int position1 = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                int position2 = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                Log.d("first completely visible item position", ""+position1);
                if(position1 != -1) {
                    View view = (View) ((LinearLayoutManager) recyclerView.getLayoutManager()).findViewByPosition(position1);
                    if (view instanceof CardView) {
                        Log.d("View is of type", "CardView");
                        View childView = (View) ((CardView) view).getChildAt(0);
                        if (childView instanceof RelativeLayout) {
                            VideoView videoView = (VideoView) ((RelativeLayout) childView).getChildAt(0);
                            videoView.start();
                        }
                    }
                    if(position2 != -1 && position2 != position1){
                        Log.d("first visible item postion", ""+position2);
                        View view2 = (View) ((LinearLayoutManager) recyclerView.getLayoutManager()).findViewByPosition(position2);
                        View childView = (View) ((CardView) view2).getChildAt(0);
                        if (childView instanceof RelativeLayout) {
                            VideoView videoView = (VideoView) ((RelativeLayout) childView).getChildAt(0);
                            if(videoView.isPlaying()) {
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
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.debug_userpage_button: {
                Intent intent = new Intent(TrendingActivity.this, UserPageActivity.class);
                startActivity(intent);
            }
            break;
        }
    }

    public void setTransitions(){
        Slide slide = new Slide();
        slide.setDuration(500);
        slide.setSlideEdge(Gravity.BOTTOM);
        getWindow().setExitTransition(null);
        getWindow().setEnterTransition(slide);
        getWindow().setReenterTransition(slide);
    }
}
