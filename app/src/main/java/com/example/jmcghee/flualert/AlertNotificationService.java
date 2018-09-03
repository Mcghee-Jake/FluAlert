package com.example.jmcghee.flualert;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.jmcghee.flualert.data.FluTweet;

import java.util.ArrayList;
import java.util.List;


public class AlertNotificationService extends Service {

    private static final String TAG = AlertNotificationService.class.getSimpleName();

    private static final String CHANNEL_NAME = "FluAlert";
    private static final String CHANNEL_ID = "FluAlertChannel";
    private static final int NOTIFICATION_ID = 1;

    public static final String INTENT_FILTER = "nearby_flu_tweets";
    public static final String NEARBY_FLU_TWEETS_TAG = "nearby_flu_tweets";

    private NotificationCompat.Builder notificationBuilder;
    private BroadcastReceiver broadcastReceiverTweets;
    private BroadcastReceiver broadcastReceiverLocation;
    private Location location = new Location("");
    private List<FluTweet> fluTweets;

    private boolean notificationActive = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Start location service
        Intent locationServiceIntent = new Intent(getApplicationContext(), UserLocationService.class);
        startService(locationServiceIntent);


        // Start api call service
        Intent apiCallServiceIntent = new Intent(getApplicationContext(), ApiCallService.class);
        startService(apiCallServiceIntent);

        // Initialize broadcast receivers
        initBroadcastReceiverLocation();
        initBroadcastReceiverTweets();

        createNotificationChannel();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (broadcastReceiverLocation != null) {
            LocalBroadcastManager.getInstance(AlertNotificationService.this).unregisterReceiver(broadcastReceiverLocation);
        }
        if (broadcastReceiverTweets != null) {
            LocalBroadcastManager.getInstance(AlertNotificationService.this).unregisterReceiver(broadcastReceiverTweets);
        }

        if (notificationActive) deactivateNotification();
    }

    private void initBroadcastReceiverTweets() {
        if (broadcastReceiverTweets == null) {
            broadcastReceiverTweets = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    intent.setExtrasClassLoader(FluTweet.class.getClassLoader());
                    fluTweets = intent.getParcelableArrayListExtra(ApiCallService.FLU_TWEETS_TAG);
                }
            };

        }
        LocalBroadcastManager.getInstance(AlertNotificationService.this).registerReceiver(broadcastReceiverTweets, new IntentFilter(ApiCallService.INTENT_FILTER));
    }

    private void initBroadcastReceiverLocation() {
        if (broadcastReceiverLocation == null) {
            broadcastReceiverLocation = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Bundle bundle = intent.getExtras();
                    Double latitude = bundle.getDouble(UserLocationService.LATITUDE_TAG);
                    Double longitude = bundle.getDouble(UserLocationService.LONGITUDE_TAG);
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);


                    if (fluTweets != null) {
                        List<FluTweet> nearbyFluTweets = new ArrayList<>();

                        for (FluTweet fluTweet : fluTweets) {
                            if ((int) fluTweet.getDistanceInMiles(location) < 25) {
                               nearbyFluTweets.add(fluTweet);
                            }
                        }
                        if (nearbyFluTweets.size() > 0 ) {
                            if (!notificationActive) {
                                buildNotification("Flu Warning", nearbyFluTweets.size() + " nearby threats");
                                activateNotification();
                            }
                            broadcastToMainActivity(nearbyFluTweets);
                        } else {
                            if (notificationActive) deactivateNotification();
                        }
                    }
                }
            };
        }
        LocalBroadcastManager.getInstance(AlertNotificationService.this).registerReceiver(broadcastReceiverLocation, new IntentFilter(UserLocationService.INTENT_FILTER));
    }

    private void broadcastToMainActivity(List<FluTweet> nearbyFluTweets) {
        Intent broadcastToMainActivity = new Intent(INTENT_FILTER);
        broadcastToMainActivity.setExtrasClassLoader(FluTweet.class.getClassLoader());
        broadcastToMainActivity.putParcelableArrayListExtra(NEARBY_FLU_TWEETS_TAG, (ArrayList<FluTweet>) nearbyFluTweets);
        LocalBroadcastManager.getInstance(AlertNotificationService.this).sendBroadcast(broadcastToMainActivity);
        Log.d(TAG, "Broadcast sent");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void buildNotification(String title, String content) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
    }

    private void activateNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

       // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        notificationActive = true;
    }

    private void deactivateNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.cancel(NOTIFICATION_ID);
        notificationActive = false;
    }


}
