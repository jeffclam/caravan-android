package com.caravan.senior_project.users;

import android.location.Location;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

/**
 * Created by Jeffrey on 4/10/2017.
 */

public class User {
    @Exclude private String UID;
    private ArrayList<Double> location;

    public User() {
        location = new ArrayList<>();
    }

    public User(String uid) {
        UID = uid;
        location = new ArrayList<>();
    }

    public void setLocation(double lat, double lon) {
        if (location.isEmpty()) {
            location.add(lat);
            location.add(lon);
        } else {
            location.set(0, lat);
            location.set(1, lon);
        }
    }

    public void setLocation(Location loc) {
        if (location.isEmpty()) {
            location.add(loc.getLatitude());
            location.add(loc.getLongitude());
        } else {
            location.set(0, loc.getLatitude());
            location.set(1, loc.getLongitude());
        }
    }

    public ArrayList<Double> getLocation() {
        return location;
    }

    public Double getLatitude() {
        return location.get(0);
    }

    public Double getLongitude() {
        return location.get(1);
    }

    @Exclude
    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
