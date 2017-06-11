package com.caravan.senior_project.users;

import android.app.Activity;
import android.util.Log;

import com.caravan.senior_project.caravan_android.R;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Jeffrey on 6/9/2017.
 */

public class Room {
    @Exclude private String roomKey;
    @Exclude private ArrayList<Roommate> roommates;
    @Exclude private DatabaseReference room;
    private ArrayList<Double> start;
    private ArrayList<Double> finish;
    private Map<String, ArrayList<Double>> users;

    public Room() {
    }

    public ArrayList<Double> getStart() {
        return start;
    }

    public void setStart(double lat, double lon) {
        start.set(0, lat);
        start.set(1, lon);
    }

    public void setFinish(double lat, double lon) {
        finish.set(0, lat);
        finish.set(1, lon);
    }

    public ArrayList<Double> getFinish() {
        return finish;
    }

    public Map<String, ArrayList<Double>> getUsers() {
        return users;
    }

    public String getRoomKey() {
        return roomKey;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }

    public ArrayList<Roommate> getRoommates() {
        if (roommates == null || roommates.isEmpty()) {
            roommates = new ArrayList<>();
            for (Map.Entry<String, ArrayList<Double>> entry : users.entrySet()) {
                roommates.add(new Roommate(entry));
            }
        }

        return roommates;
    }

    public void showRoommates(MapboxMap map, Activity activity) {
        for (int i = 0; i < roommates.size(); i++) {
            Roommate r = roommates.get(i);
            if (r.getIcon() == null) {
                r.setIcon(map.addMarker(new MarkerViewOptions()
                    .position(new LatLng(r.getLatitude(), r.getLongtitude()))));
                IconFactory iconFactory = IconFactory.getInstance(activity);
                Icon icon;
                switch (i % 3) {
                    case 1:
                        icon = iconFactory.fromResource(R.drawable.friend2);
                        break;
                    case 2:
                        icon = iconFactory.fromResource(R.drawable.friend3);
                        break;
                    case 0:
                    default:
                        icon = iconFactory.fromResource(R.drawable.friend1);

                }
                r.getIcon().setIcon(icon);
            }
        }
    }

    public void setRoommates(ArrayList<Roommate> roommates) {
        this.roommates = roommates;
    }

    public void addRoommate(User user) {
        if (roomKey != null) {
            if (room == null)
                room = CaravanDB.rooms.child(roomKey);

            ArrayList<Double> location = new ArrayList<>();
            location.add(user.getLocation().getLatitude());
            location.add(user.getLocation().getLatitude());
            Log.d("Room", "Room: User:" + user.getUID());
            Log.d("Room", "Room: UserLocation:" + user.getLocation().toString());
            room.child("users").child(user.getUID()).child("0")
                .setValue(user.getLocation().getLatitude());
            room.child("users").child(user.getUID()).child("1")
                  .setValue(user.getLocation().getLongitude());
        }
    }

    public void pushToDB() {
        if (roomKey != null) {
            if (room == null)
                room = CaravanDB.rooms.child(roomKey);
            room.child("start").setValue(start);
            room.child("finish").setValue(finish);
            room.child("users").setValue(users);
        } else {
            Log.d("Room", "No room to push");
        }
    }
}
