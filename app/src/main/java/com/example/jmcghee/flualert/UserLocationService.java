package com.example.jmcghee.flualert;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

public class UserLocationService extends Service {

    public  static String TAG = UserLocationService.class.getSimpleName();
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
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(5*1000); // 5 seconds
    }


    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Intent intent = new Intent(INTENT_FILTER);
                    Double latitude =  location.getLatitude();
                    Double longitude = location.getLongitude();
                    intent.putExtra(LATITUDE_TAG, latitude);
                    intent.putExtra(LONGITUDE_TAG, longitude);
                    LocalBroadcastManager.getInstance(UserLocationService.this).sendBroadcast(intent);
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if(!locationAvailability.isLocationAvailable()) {
                    Log.d(TAG, "Unable to get location");
                }
            }
        };
    }

}
