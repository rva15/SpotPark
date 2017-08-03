package com.application.android.sp;
//All imports
import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 10/12/16.
 */
public class FavoritePlace {
    private double flatitude;
    private double flongitude;
    private String spotname;

    public FavoritePlace() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.android.sp.CheckInUser.class)
    }

    public FavoritePlace(double flatitude, double flongitude,String spotname) {
        this.flatitude     = flatitude;
        this.flongitude    = flongitude;
        this.spotname      = spotname;

    }

    public double getflatitude(){
        return this.flatitude;
    }
    public double getflongitude(){
        return this.flongitude;
    }
    public String getspotname(){return this.spotname;}


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("flatitude", flatitude);
        result.put("flongitude", flongitude);
        result.put("spotname",spotname);
        return result;
    }
}
