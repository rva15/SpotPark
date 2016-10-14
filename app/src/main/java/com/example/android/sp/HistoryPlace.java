package com.example.android.sp;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 10/2/16.
 */
public class HistoryPlace {
    public double platitude;
    public double plongitude;
    public String date;
    public String time;

    public HistoryPlace() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.android.sp.CheckInUser.class)
    }

    public HistoryPlace(double platitude, double plongitude,String date,String time) {
        this.platitude     = platitude;
        this.plongitude    = plongitude;
        this.date          = date;
        this.time          = time;
    }

    public double getplatitude(){
        return this.platitude;
    }
    public double getplongitude(){
        return this.plongitude;
    }
    public String getdate(){return this.date;}
    public String gettime(){return this.time;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("platitude", platitude);
        result.put("plongitude", plongitude);
        result.put("date",date);
        result.put("time",time);
        return result;
    }
}
