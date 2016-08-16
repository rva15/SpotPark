package com.example.android.sp;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/15/16.
 */
public class User {

    public String userName;
    public int    numberOfKeys;
    public String plateNumber;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.android.sp.User.class)
    }

    public User(String userName,int numberOfKeys, String plateNumber) {
        this.userName     = userName;
        this.numberOfKeys = numberOfKeys;
        this.plateNumber  = plateNumber;

    }

    public String getUserName(){
        return this.userName;
    }

    public int getNumberOfKeys(){
        return this.numberOfKeys;
    }

    public String getPlateNumber(){
        return this.plateNumber;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userName", userName);
        result.put("numkeys", numberOfKeys);
        result.put("plateNumber", plateNumber);

        return result;
    }



}
