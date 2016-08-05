package com.example.android.sp;

import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * Created by ruturaj on 8/4/16.
 */
public class CheckOut {
    double latitude,longitude;
    int hour,min;
    int dollar,cent;


    public CheckOut(){}


    public CheckOut(double latitudes, double longitudes, int hours, int mins, int dollars, int cents){


        this.latitude = latitudes;
        this.longitude = longitudes;
        this.hour = hours;
        this.min = mins;
        this.dollar = dollars;
        this.cent = cents;

    }

    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }
    public int getHour(){
        return hour;
    }
    public int getMin(){
        return min;
    }
    public int getDollar(){
        return dollar;
    }
    public int getCent(){
        return cent;
    }




}


