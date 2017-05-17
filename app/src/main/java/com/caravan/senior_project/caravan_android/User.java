package com.caravan.senior_project.caravan_android;

/**
 * Created by Jeffrey on 4/10/2017.
 */

public class User {

    public String email;
    public Coord coord ;

    public User() {
        //empty constructor
    }

    public User(String email) {
        this.email = email;
        coord = new Coord();
    }

    public void setCoords(double lat, double lon) {
        coord.latitude = lat;
        coord.longitude = lon;
    }

    public String getEmail() {
        return email;
    }

    public Coord getCoord() {
        return coord;
    }
}
