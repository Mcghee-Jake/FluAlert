package com.example.jmcghee.flualert;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.jmcghee.flualert.data.FluTweet;
import com.example.jmcghee.flualert.utils.NetworkUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<String> {

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String NUM_DAYS = Integer.toString(7);
    private static final String SEARCH_QUERY_URL = "url";
    private static final String SEARCH_RESULTS_RAW_JSON = "results"; // Used to save and restore the results of the API call
    private static final int MY_LOADER = 77;

    private TextView tvTest, tvLatitude, tvLongitude;
    private ProgressBar pbLoadingIndicator;
    private List<FluTweet> fluTweets;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Bundle bundle = intent.getExtras();
                    String latitude = bundle.getString(LocationService.LATITUDE_TAG);
                    String longitude = bundle.getString(LocationService.LATITUDE_TAG);

                    tvLatitude.setText(latitude);
                    tvLongitude.setText(longitude);
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter(LocationService.INTENT_FILTER));
    }

    @SuppressLint("MissingPermission") // TODO remove this
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTest = findViewById(R.id.tv_test);
        tvLatitude = findViewById(R.id.tv_latitude);
        tvLongitude = findViewById(R.id.tv_longitude);
        pbLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        if (savedInstanceState != null) {
            String rawJsonSearchResults = savedInstanceState.getString(SEARCH_RESULTS_RAW_JSON);
            tvTest.setText(rawJsonSearchResults);
        } else {
            makeQuery();
        }

        // Make sure location permissions are enabled
        while (!runtime_permissions()) {
            runtime_permissions();
        }

        /*
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        startService(intent);
        */
        buildLocationRequest();
        buildLocationCallback();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String rawJsonSearchResults = tvTest.getText().toString();
        outState.putString(SEARCH_RESULTS_RAW_JSON, rawJsonSearchResults);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==PERMISSIONS_REQUEST_CODE) {
            boolean permissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) permissionsGranted = false;
            }
            if (!permissionsGranted) runtime_permissions();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }


    @Override
    public android.support.v4.content.Loader<String> onCreateLoader(int id, final Bundle args) {
        return new android.support.v4.content.AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                if (args == null) return;
                pbLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                String searchQueryUrlString = args.getString(SEARCH_QUERY_URL);
                String result = null;
                try {
                    URL url = new URL(searchQueryUrlString);
                    Log.d("URL", url.toString());
                    result = NetworkUtils.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<String> loader, String data) {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);

        if (data != null && !data.equals("")) {
            fluTweets = NetworkUtils.getFluTweetsFromRawJson(data);
            FluTweet fluTweet = fluTweets.get(0);
            tvTest.setText(fluTweet.getTweetText());
        }
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<String> loader) {

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(5*1000);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    tvLatitude.setText(String.format(Locale.ENGLISH, "%f", location.getLatitude()));
                    tvLongitude.setText(String.format(Locale.ENGLISH, "%f", location.getLongitude()));
                }
            }
        };
    }

    private void makeQuery() {
        // Get the loader
        android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
        android.support.v4.content.Loader<String> queryLoader = loaderManager.getLoader(MY_LOADER);

        // Build the URL
        URL url = NetworkUtils.buildUrl(NUM_DAYS);
        // Put the URL into a bundle
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL, url.toString());

        // Get the callbacks
        android.support.v4.app.LoaderManager.LoaderCallbacks<String> callbacks = MainActivity.this;

        // Init or restart the loader
        if (queryLoader == null) {
            loaderManager.initLoader(MY_LOADER, queryBundle, callbacks);
        } else {
            loaderManager.restartLoader(MY_LOADER, queryBundle, callbacks);
        }
    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE);

            return false;
        } else return true;
    }


}
