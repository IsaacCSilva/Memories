package edu.csulb.memoriesapplication;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Brian on 11/16/2017.
 */

public class LocationService extends IntentService {

    private FusedLocationProviderClient mFusedLocationClient;
    private String mCity;
    private String mState;
    private Double mLatitude;
    private Double mLongitude;

    public LocationService() { super("LocationService"); }

    @Override
    public void onCreate() { super.onCreate(); }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            // Get last known location using Google Play Services
            // Unable to request permissions in service
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null){
                                mLongitude = location.getLongitude();
                                mLatitude = location.getLatitude();
                                // Reverse geocoding to get city and state
                                Geocoder geocoder = new Geocoder(LocationService.this, Locale.getDefault());
                                try{
                                    List<Address> address = geocoder.getFromLocation(mLatitude, mLongitude, 1);
                                    mCity = address.get(0).getLocality();
                                    mState = address.get(0).getAdminArea();
                                    sendMessageToActivity();

                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        } catch (SecurityException e){
            Toast.makeText(getApplicationContext(),"SecurityException:\n" + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendMessageToActivity(){
        Intent intent = new Intent("finishedLocation");
        intent.putExtra("latitude", mLatitude);
        intent.putExtra("longitude", mLongitude);
        intent.putExtra("city", mCity);
        intent.putExtra("state",mState);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
