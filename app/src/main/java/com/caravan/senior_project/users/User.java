package com.caravan.senior_project.users;

/**
 * Created by Jeffrey on 4/10/2017.
 */

public class User {

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

    public String getEmail() {
        return email;
    }

    public Coord getLocation() {
        return location;
    }
}
