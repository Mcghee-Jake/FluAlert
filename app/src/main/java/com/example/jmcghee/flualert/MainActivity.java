package com.example.jmcghee.flualert;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.jmcghee.flualert.data.FluTweet;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private BroadcastReceiver broadcastReceiverTweets;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make sure location permissions are enabled
        while (!runtime_permissions()) {
            runtime_permissions();
        }

        // Start alert notification service
         Intent alertNotificationServiceIntent = new Intent(getApplicationContext(), AlertNotificationService.class);
         startService(alertNotificationServiceIntent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initBroadcastReceiverTweets();
    }

    private void initBroadcastReceiverTweets() {
        if (broadcastReceiverTweets == null) {
            broadcastReceiverTweets = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    List<FluTweet> fluTweets = intent.getParcelableArrayListExtra(AlertNotificationService.NEARBY_FLU_TWEETS_TAG);
                    RecyclerView recyclerView = findViewById(R.id.rv_flutweets);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setHasFixedSize(true);
                    AdapterFluTweets adapter = new AdapterFluTweets(fluTweets);
                    recyclerView.setAdapter(adapter);
                }
            };
        }
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(broadcastReceiverTweets, new IntentFilter(AlertNotificationService.INTENT_FILTER));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (broadcastReceiverTweets != null) {
            LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(broadcastReceiverTweets);
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
