package com.caravan.senior_project.users;

import com.mapbox.mapboxsdk.annotations.MarkerView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Jeffrey on 6/10/2017.
 */

public class Roommate {
    private User roommate;
    private MarkerView icon;

    public Roommate() {

    }

    public Roommate(Map.Entry<String, ArrayList<Double>> entry) {
        roommate.setUID(entry.getKey());
        roommate.setLocation(entry.getValue().get(0), entry.getValue().get(1));
    }

    public User getRoommate() {
        return roommate;
    }

    public void setRoommate(User roommate) {
        this.roommate = roommate;
    }

    public MarkerView getIcon() {
        return icon;
    }

    public void setIcon(MarkerView icon) {
        this.icon = icon;
    }

    public double getLatitude() {
        return roommate.getLocation().getLatitude();
    }

    public double getLongtitude() {
        return roommate.getLocation().getLongitude();
    }
}
