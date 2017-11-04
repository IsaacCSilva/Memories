package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        button = (Button) findViewById(R.id.debug_userpage_button);
        button.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();

        //added
        polaroids = new ArrayList<Polaroid>();
        rvAdapter = new CardViewAdapter(this, polaroids);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(rvAdapter);

        Uri uri = Uri.parse("http://webm.land/media/Qn8D.webm");
        Polaroid polaroid = new Polaroid(uri, null);

        polaroids.add(polaroid);
        uri = Uri.parse("http://i646.photobucket.com/albums/uu187/jess_roces/animal11.jpg");
        polaroid = new Polaroid(null, uri);
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
}
