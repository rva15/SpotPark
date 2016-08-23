package com.example.android.sp;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/18/16.
 */
public class CheckInDetails {
    double checkouthours=0,checkoutmins=0;
    double dollars=0,cents=0;
    double longitude=0.0,latitude=0.0;
    String id = "";
    boolean readytoleave=false;



    public CheckInDetails(){}


    public CheckInDetails(double longitude,double latitude, double checkouthours,double checkoutmins,double dollars,double cents,String id,boolean readytoleave){

        this.longitude=longitude;
        this.latitude = latitude;
        this.checkouthours=checkouthours;
        this.checkoutmins=checkoutmins;
        this.dollars=dollars;
        this.cents=cents;
        this.id=id;
        this.readytoleave=readytoleave;
    }


    public double   getlongitude(){return this.longitude;}
    public double   getlatitude(){return this.latitude;}
    public double      getcheckouthours(){return this.checkouthours;}
    public double      getcheckoutmins(){return this.checkoutmins;}
    public double      getdollars(){return this.dollars;}
    public double      getcents(){return this.cents;}
    public String   getid(){return this.id;}
    public boolean  getreadytoleave(){return this.readytoleave;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("longitude",this.longitude);
        result.put("latitude",this.latitude);
        result.put("checkouthours",this.checkouthours);
        result.put("checkoutmins",this.checkoutmins);
        result.put("dollars",this.dollars);
        result.put("cents",this.cents);
        result.put("id",this.id);
        result.put("readytoleave",this.readytoleave);

        return result;
    }
}
