package com.app.android.sp;
import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 7/15/17.
 */

public class ARSpots {

    private double latitude=0,longitude=0,millis=0;
    private int type=123;

    public ARSpots(){}

    public ARSpots(double latitude,double longitude, double millis, int type){

        this.latitude  = latitude;
        this.longitude = longitude;
        this.millis    = millis;
        this.type      = type;
    }

    public double getlatitude(){return this.latitude;}
    public double getlongitude(){return this.longitude;}
    public double getmillis(){return this.millis;}
    public double gettype(){return this.type;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("longitude",this.longitude);
        result.put("latitude",this.latitude);
        result.put("millis",this.millis);
        result.put("type",this.type);
        return result;
    }

}
