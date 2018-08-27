package com.example.jmcghee.flualert.data;

import android.location.Location;

import java.io.Serializable;

public class FluTweet implements Serializable {
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

    public double getDistanceInMiles(Location location) {
        return this.location.distanceTo(location) * 0.000621371;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FluTweet) {
            FluTweet input = (FluTweet) obj;
            if (this.username.equals(input.username) && this.tweet_date == input.tweet_date)
                return true;
        }
        return false;
    }
}
