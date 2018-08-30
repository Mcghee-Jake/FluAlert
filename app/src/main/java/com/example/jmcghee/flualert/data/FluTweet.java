package com.example.jmcghee.flualert.data;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class FluTweet implements Parcelable {

    private String username, tweetText;
    private Location location;
    private long tweetDate;


    public static final Creator<FluTweet> CREATOR = new Creator<FluTweet>() {
        @Override
        public FluTweet createFromParcel(Parcel in) {
            return new FluTweet(in);
        }

        @Override
        public FluTweet[] newArray(int size) {
            return new FluTweet[size];
        }
    };

    public FluTweet(String username, String tweetText, Location location, long tweetDate) {
        this.username = username;
        this.location = location;
        this.tweetText = tweetText;
        this.tweetDate = tweetDate;
    }

    public FluTweet(Parcel in) {
        this.username = in.readString();
        this.tweetText = in.readString();
        this.location = Location.CREATOR.createFromParcel(in);
        this.tweetDate = in.readLong();
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

    public long getTweetDate() {
        return tweetDate;
    }

    public double getDistanceInMiles(Location location) {
        return this.location.distanceTo(location) * 0.000621371;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FluTweet) {
            FluTweet input = (FluTweet) obj;
            return this.username.equals(input.username) && this.tweetDate == input.tweetDate;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (this.username.hashCode() + Long.toString(this.tweetDate).hashCode());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.username);
        dest.writeString(this.tweetText);
        location.writeToParcel(dest, flags);
        dest.writeLong(this.tweetDate);
    }

}
