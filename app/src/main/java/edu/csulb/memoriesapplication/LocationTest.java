package edu.csulb.memoriesapplication;

import android.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Brian on 11/16/2017.
 */

public class LocationTest extends AppCompatActivity {

    private double mLatitude;
    private double mLongitude;
    private String mState;
    private String mLocation;
    private int LOCATION_PERMISSON = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationtest);

        // Check for permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSON);
        }

        final Intent intent = new Intent(this, LocationService.class);
        intent.setAction("finshedLocation");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("finishedLocation"));

        Button locationButton = (Button) findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(intent);
                Toast.makeText(getApplicationContext(), mLocation, Toast.LENGTH_LONG).show();
            }
        });
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mLatitude = intent.getDoubleExtra("latitude", 0);
            mLongitude = intent.getDoubleExtra("longitude", 0);
            mState = intent.getStringExtra("state");
            mLocation = "Latitude: " + mLatitude + "\nLongitude: " + mLongitude + "\nState: " + mState;
        }
    };

    @Override
    protected void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}