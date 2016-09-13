package com.example.android.sp;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/18/16.
 */
public class CheckInDetails {
    int dollars=0,cents=0;
    double longitude=0.0,latitude=0.0;
    String id = "";
    int minstoleave = 0;



    public CheckInDetails(){}


    public CheckInDetails(double latitude,double longitude, int dollars,int cents,String id,int minstoleave){

        this.longitude=longitude;
        this.latitude = latitude;
        this.dollars=dollars;
        this.cents=cents;
        this.id=id;
        this.minstoleave=minstoleave;
    }


    public double   getlongitude(){return this.longitude;}
    public double   getlatitude(){return this.latitude;}
    public int      getdollars(){return this.dollars;}
    public int      getcents(){return this.cents;}
    public String   getid(){return this.id;}
    public int  getminstoleave(){return this.minstoleave;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("longitude",this.longitude);
        result.put("latitude",this.latitude);
        result.put("dollars",this.dollars);
        result.put("cents",this.cents);
        result.put("id",this.id);
        result.put("minstoleave",this.minstoleave);

        return result;
    }
}
