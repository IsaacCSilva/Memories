package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Danie on 10/16/2017.
 */

public class TrendingActivity extends Activity implements View.OnClickListener{

    private TextView text;
    private Button button;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        button = (Button) findViewById(R.id.debug_userpage_button);
        button.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();

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
