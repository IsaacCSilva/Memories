package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Danie on 10/16/2017.
 */

public class TrendingActivity extends Activity{

    private TextView text;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);
        mAuth = FirebaseAuth.getInstance();

    }
}
