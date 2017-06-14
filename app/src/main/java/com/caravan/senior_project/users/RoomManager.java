package com.caravan.senior_project.users;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;

/**
 * Created by Jeffrey on 6/10/2017.
 */

public class RoomManager {
    private String TAG = "RoomManager";

    private Room room;
    private DatabaseReference db_room;

    public RoomManager() {
    }

    public interface waitTilReady {
        void roomExists(boolean exists);
    }

    public void readRoom(final String roomKey, final String user) {
        Log.d(TAG, "readRoom(): trying");
        if (db_room == null) {
            db_room = CaravanDB.rooms.child(roomKey);
        }
        db_room.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        room = dataSnapshot.getValue(Room.class);
                        room.setRoomKey(roomKey);
                        room.addRoommate(user);

                        Log.d(TAG, "readRoom(): Successfully pulled a room for: " +
                                room.getFinish().get(0) + ", " + room.getFinish().get(1));
                    } else {
                        room = null;
                    }
                } catch (DatabaseException d) {
                    Log.e(TAG, "Error in readRoom():" + d.getMessage());
                    d.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void readRoom(final String roomKey, final String user, waitTilReady callback) {
        Log.d(TAG, "readRoom(): trying");
        final waitTilReady x = callback;
        if (db_room == null) {
            db_room = CaravanDB.rooms.child(roomKey);
        }
        db_room.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        room = dataSnapshot.getValue(Room.class);
                        room.setRoomKey(roomKey);
                        room.addRoommate(user);

                        Log.d(TAG, "readRoom(): Successfully pulled a room for: " +
                                room.getFinish().get(0) + ", " + room.getFinish().get(1));

                        if (room != null)
                            x.roomExists(true);
                    } else {
                        room = null;
                        x.roomExists(false);
                    }
                } catch (DatabaseException d) {
                    Log.e(TAG, "Error in readRoom():" + d.getMessage());
                    d.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showRoommates(MapboxMap map, Activity activity) {
        if (room != null) {
            room.getRoommates(activity, map);
        } else {
            Log.e(TAG,"Room is null");
        }
    }

    public Room getRoom() {
        return room;
    }

    public void getRoommates(Activity activity, MapboxMap map) {
        if (room != null) {
            room.getRoommates(activity, map);
        } else {
            Log.d(TAG, "getRoommates(): no room reference");
        }
    }

    public double getLatitude() {
        double lat = 0;
        if (room != null) {
            lat = room.getFinish().get(0);
        }
        return lat;
    }

    public double getLongitude() {
        double lon = 0;
        if (room != null) {
            lon = room.getFinish().get(1);
        }
        return lon;
    }
}
