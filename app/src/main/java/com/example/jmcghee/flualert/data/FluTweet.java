package com.example.jmcghee.flualert.data;

public class FluTweet {
    private String username, tweetText;
    private double latitude, longitude;
    private long tweet_date;

    public FluTweet(String username, String tweetText, double latitude, double longitude, long tweet_date) {
        this.username = username;
        this.tweetText = tweetText;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tweet_date = tweet_date;
    }
}
