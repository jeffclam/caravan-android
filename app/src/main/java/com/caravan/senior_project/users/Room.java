package com.caravan.senior_project.users;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Jeffrey on 6/9/2017.
 */

public class Room {
    @Exclude private String roomKey;
    @Exclude private ArrayList<Roommate> roommates;
    private ArrayList<Double> start;
    private ArrayList<Double> finish;
    private Map<String, ArrayList<Double>> users;

    public Room() {
    }

    public ArrayList<Double> getStart() {
        return start;
    }

    public ArrayList<Double> getFinish() {
        return finish;
    }

    public Map<String, ArrayList<Double>> getUsers() {
        return users;
    }

    /*
    @Exclude
    public String getRoomKey() {
        return roomKey;
    }

    @Exclude
    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }

    @Exclude
    public ArrayList<Roommate> getRoommates() {
        return roommates;
    }

    @Exclude
    public void setRoommates(ArrayList<Roommate> roommates) {
        this.roommates = roommates;
    }

    public void pushToDB() {
        if (roomKey != null) {
            DatabaseReference room = CaravanDB.rooms.child(roomKey);
            room.child("start").setValue(start);
            room.child("finish").setValue(finish);
            room.child("users").setValue(users);
        } else {
            Log.d("Room", "No room to push");
        }
    }
    */
}
