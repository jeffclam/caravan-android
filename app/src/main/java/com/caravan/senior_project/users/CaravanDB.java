package com.caravan.senior_project.users;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Jeffrey on 6/10/2017.
 */

public class CaravanDB {
    public static DatabaseReference caravanDB = FirebaseDatabase.getInstance().getReference();
    public static DatabaseReference roomKeys = caravanDB.child("roomKeys");
    public static DatabaseReference rooms = caravanDB.child("rooms");
    public static DatabaseReference users = caravanDB.child("users");
}
