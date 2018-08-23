package com.example.jmcghee.flualert.utils;

import android.location.Location;
import android.net.Uri;

import com.example.jmcghee.flualert.data.FluTweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NetworkUtils {

    final static String BASE_URL = "http://api.flutrack.org";
    final static String PARAM_QUERY = "time";

    /**
     * Builds the URL
     *
     * @param days The number of days included in the api call
     * @return The built URL
     */
    public static URL buildUrl(String days) {
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_QUERY, days)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response
     *
     * @param url The URL to fetch the HTTP response from
     * @return The contents of the HTTP response
     * @throws IOException
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static List<FluTweet> getFluTweetsFromRawJson(String rawJson) {
        List<FluTweet> fluTweets = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(rawJson);
            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String username = jsonObject.getString("user_name");
                String tweetText = jsonObject.getString("tweet_text");
                Location location = new Location("");
                location.setLatitude(jsonObject.getDouble("latitude"));
                location.setLongitude(jsonObject.getDouble("longitude"));
                Long tweetDate = jsonObject.getLong("tweet_date");

                FluTweet fluTweet = new FluTweet(username, tweetText, location, tweetDate);
                fluTweets.add(fluTweet);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return fluTweets;
    }

}
