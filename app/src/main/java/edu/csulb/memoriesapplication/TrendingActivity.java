package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Danie on 10/16/2017.
 */

public class TrendingActivity extends Activity{

    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);
        text = (TextView) this.findViewById(R.id.trending_text);
        Intent intent = getIntent();
        text.setText(intent.getStringExtra("EMAIL"));
    }
}
