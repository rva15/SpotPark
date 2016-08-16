package com.example.android.sp;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/15/16.
 */
public class Time {
    int hours=0;
    int mins=0;

    public Time(){}

    public Time(int hours,int mins){

        this.hours=hours;
        this.mins=mins;

    }

    public int getHours(){
        return hours;
    }

    public int getMins(){
        return mins;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("hours", hours);
        result.put("mins", mins);

        return result;
    }

}
