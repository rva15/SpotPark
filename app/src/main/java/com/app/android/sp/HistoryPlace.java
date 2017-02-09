package com.app.android.sp;
//All imports
import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 10/2/16.
 */
public class HistoryPlace {
    private double platitude;
    private double plongitude;
    private String date;
    private String time;
    private int isfavorite;

    public HistoryPlace() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.android.sp.CheckInUser.class)
    }

    public HistoryPlace(double platitude, double plongitude,String date,String time,int isfavorite) {
        this.platitude     = platitude;
        this.plongitude    = plongitude;
        this.date          = date;
        this.time          = time;
        this.isfavorite    = isfavorite;
    }

    public double getplatitude(){
        return this.platitude;
    }
    public double getplongitude(){
        return this.plongitude;
    }
    public String getdate(){return this.date;}
    public String gettime(){return this.time;}
    public int    getisfavorite(){return this.isfavorite;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("platitude", platitude);
        result.put("plongitude", plongitude);
        result.put("date",date);
        result.put("time",time);
        result.put("isfavorite",isfavorite);
        return result;
    }
}
