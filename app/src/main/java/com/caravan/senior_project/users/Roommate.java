package com.caravan.senior_project.users;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeffrey on 6/10/2017.
 */

public class Roommate {
    private User roommate;
    private MarkerView icon;
    private ValueEventListener locationListener;
    private DatabaseReference db_location;
    private boolean gotData;
    Roommate curRoommate;

    public Roommate() {

    }

    public Roommate(Map.Entry<String, String> entry, final Room room, final MapboxMap map, final Activity activity) {
        roommate = new User(entry.getValue());
        gotData =false;
        db_location = CaravanDB.users.child(roommate.getUID()).child("location");

        Log.d("Roommate", "Creating listener");
        locationListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                roommate.setLocation((Double) dataSnapshot.child("0").getValue(),
                        (Double) dataSnapshot.child("1").getValue());
                if (icon != null) {
                    icon.setPosition(new LatLng(getLatitude(), getLongtitude()));
                }
                Log.d("Roommate", "new Roommate(): " + getUID()
                        + " <" + getLatitude() + ", " + getLongtitude() + ">");
                room.showRoommates(map, activity, curRoommate, roommate.getLatitude(), roommate.getLongitude(), 0);
                gotData = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Roommate", databaseError.getMessage());
            }
        };
        curRoommate = this;
        Log.d("Roommate", "Created listener");
        db_location.addValueEventListener(locationListener);
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

    public String getUID() {
        return roommate.getUID();
    }

    public boolean getGotData() { return gotData; }

    public double getLatitude() {
        if (roommate.getLocation().isEmpty())
            return 0;
        return roommate.getLatitude();
    }

    public double getLongtitude() {
        if (roommate.getLocation().isEmpty())
            return 0;
        return roommate.getLongitude();
    }
}
