package com.example.jmcghee.flualert;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.jmcghee.flualert.data.FluTweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ApiCallService extends Service {

        public static final String INTENT_FILTER = "api_data_update";
        public static final String FLU_TWEETS_TAG = "flu_tweets";
        private static final String TAG = ApiCallService.class.getSimpleName();
        private static final String URL = "http://api.flutrack.org/?time=7";

        private RequestQueue requestQueue;

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
           Timer timer = new Timer();
           timer.scheduleAtFixedRate(new TimerTask() {
               @Override
               public void run() {
                   volleyRequest();
               }
           }, 0, 10*1000); // 10 seconds
        }

        private void volleyRequest() {
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(this);
            }

            final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    List<FluTweet> fluTweets = buildFluTweetsFromJsonArray(response);
                    Intent intent = new Intent(INTENT_FILTER);
                    intent.putExtra(FLU_TWEETS_TAG, (Serializable) fluTweets);
                    sendBroadcast(intent);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG,"Unable to make http request");
                }
            });

            requestQueue.add(request);
        }

        public static List<FluTweet> buildFluTweetsFromJsonArray(JSONArray jsonArray) {
            Set<FluTweet> fluTweetsSet = new HashSet<>();
            List<FluTweet> fluTweetsList = new ArrayList<>();


            try {
                for (int i=0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String username = jsonObject.getString("user_name");
                    String tweetText = jsonObject.getString("tweet_text");
                    Location location = new Location("");
                    location.setLatitude(jsonObject.getDouble("latitude"));
                    location.setLongitude(jsonObject.getDouble("longitude"));
                    Long tweetDate = jsonObject.getLong("tweet_date");

                    FluTweet fluTweet = new FluTweet(username, tweetText, location, tweetDate);
                    fluTweetsSet.add(fluTweet);
                }
                fluTweetsList.addAll(fluTweetsSet);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return fluTweetsList;
        }

}
