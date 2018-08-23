package com.example.jmcghee.flualert;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

public class LocationService extends Service {

    public static String INTENT_FILTER = "location_update";
    public static String LATITUDE_TAG = "latitude";
    public static String LONGITUDE_TAG = "longitude";

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        buildLocationRequest();
        buildLocationCallback();

        fusedLocationProviderClient = new FusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(5*1000);
        locationRequest.setSmallestDisplacement(10);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Intent intent = new Intent(INTENT_FILTER);
                    intent.putExtra(LATITUDE_TAG, location.getLatitude());
                    intent.putExtra(LONGITUDE_TAG, location.getLongitude());
                    Toast.makeText(LocationService.this, "Test", Toast.LENGTH_SHORT).show();
                    sendBroadcast(intent);
                }
            }
        };
    }

}
