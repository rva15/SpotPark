package com.example.android.sp;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 9/4/16.
 */
public class Searcher {
    double latitude,longitude;

    public Searcher(){}

    public Searcher(double latitude,double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getlatitude(){return this.latitude;}
    public double getlongitude(){return this.longitude;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude",this.latitude);
        result.put("longitude",this.longitude);

        return result;
    }

}
