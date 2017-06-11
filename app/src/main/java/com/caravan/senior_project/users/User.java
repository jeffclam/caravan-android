package com.caravan.senior_project.users;

import android.location.Location;

import com.google.firebase.database.Exclude;

/**
 * Created by Jeffrey on 4/10/2017.
 */

public class User {
    @Exclude private String UID;
    private String email;
    private Coord location;

    public User() {
        //empty constructor
    }

    public User(String email) {
        this.email = email;
        location = new Coord();
    }

    public void setLocation(double lat, double lon) {
        location.setLatitude(lat);
        location.setLongitude(lon);
    }

    public void setLocation(Location loc) {
        location.setLatitude(loc.getLatitude());
        location.setLongitude(loc.getLongitude());
    }

    public String getEmail() {
        return email;
    }

    public Coord getLocation() {
        return location;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
