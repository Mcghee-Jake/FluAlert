package com.example.jmcghee.flualert;

import android.Manifest;
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
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.jmcghee.flualert.data.FluTweet;

import org.json.JSONArray;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String URL = "http://api.flutrack.org/?time=7";
    private static final String TV_TEST_TAG = "results"; // Used to save and restore the results of the API call

    private TextView tvTest, tvLatitude, tvLongitude;
    private List<FluTweet> fluTweets;
    private RequestQueue requestQueue;
    private BroadcastReceiver broadcastReceiver;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTest = findViewById(R.id.tv_test);
        tvLatitude = findViewById(R.id.tv_latitude);
        tvLongitude = findViewById(R.id.tv_longitude);

        if (savedInstanceState != null) {
            String rawJsonSearchResults = savedInstanceState.getString(TV_TEST_TAG);
            tvTest.setText(rawJsonSearchResults);
        } else {
            vollyRequest();
        }

        // Make sure location permissions are enabled
        while (!runtime_permissions()) {
            runtime_permissions();
        }

        // Start location service
        if (location == null) {
            location = new Location("");
        }

        Intent intent = new Intent(getApplicationContext(), UserLocationService.class);
        startService(intent);
    }

    private void vollyRequest() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                fluTweets = ApiCallService.buildFluTweetsFromRawJson(response.toString());
                FluTweet fluTweet = fluTweets.get(0);
                tvTest.setText(fluTweet.getTweetText());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                tvTest.setText("Unable to make http request");
            }
        });

        requestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Bundle bundle = intent.getExtras();
                    Double latitude = bundle.getDouble(UserLocationService.LATITUDE_TAG);
                    Double longitude = bundle.getDouble(UserLocationService.LONGITUDE_TAG);
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);


                    tvLatitude.setText(String.format(Locale.ENGLISH, "%f", latitude));
                    tvLongitude.setText(String.format(Locale.ENGLISH, "%f", longitude));

                    if (fluTweets != null) {
                        String msg = "";
                        for (FluTweet fluTweet : fluTweets) {
                            if ((int) fluTweet.getDistanceInMiles(location) < 25) {
                                msg += "Tweet: " + fluTweet.getTweetText() +
                                        "\nDistance: " + fluTweet.getDistanceInMiles(location) + " miles\n\n";
                            }
                        }
                        tvTest.setText(msg);
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter(UserLocationService.INTENT_FILTER));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String tvTestContent = tvTest.getText().toString();
        outState.putString(TV_TEST_TAG, tvTestContent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean permissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) permissionsGranted = false;
            }
            if (!permissionsGranted) runtime_permissions();
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
