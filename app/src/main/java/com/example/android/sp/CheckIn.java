//This is the class for CheckIn objects
package com.example.android.sp;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/4/16.
 */
public class CheckIn {
    double latitude;
    double longitude;
    int hours,mins,dollars,cents;
    String userID;
    boolean notify;


    public CheckIn(){}


    public CheckIn( double latitude,double longitude,int hours,int mins, int dollars,int cents, String userID, boolean notify){

        this.latitude=latitude;
        this.longitude=longitude;
        this.hours   = hours;
        this.mins    = mins;
        this.dollars =dollars;
        this.cents   = cents;
        this.userID   = userID;
        this.notify   = notify;

    }


    public double   getLatitude(){return latitude;}
    public double   getLongitude(){return longitude;}
    public int      getHours(){return hours;}
    public int     getMins(){return mins;}
    public int   getDollars(){return dollars;}
    public int   getCents(){return cents;}
    public String  getUserID(){return userID;}
    public boolean getNotify(){return notify;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude",latitude);
        result.put("longitude",longitude);
        result.put("checkoutHours",hours);
        result.put("checkoutMins",mins);
        result.put("dollars", dollars);
        result.put("cents",cents);
        result.put("userID",userID);
        result.put("notify",notify);

        return result;
    }




}


