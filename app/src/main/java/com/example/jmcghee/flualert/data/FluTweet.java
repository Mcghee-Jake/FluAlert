package com.example.jmcghee.flualert.data;

import android.location.Location;

public class FluTweet {
    private String username, tweetText;
    private Location location;
    private long tweet_date;

    public FluTweet(String username, String tweetText, Location location, long tweet_date) {
        this.username = username;
        this.location = location;
        this.tweetText = tweetText;
        this.tweet_date = tweet_date;
    }

    public String getUsername() {
        return username;
    }

    public String getTweetText() {
        return tweetText;
    }

    public Location getLocation() {
        return location;
    }

    public long getTweet_date() {
        return tweet_date;
    }

    public double getDistance(Location location) {
        return this.location.distanceTo(location);
    }

}
