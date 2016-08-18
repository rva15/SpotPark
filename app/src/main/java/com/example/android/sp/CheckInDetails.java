package com.example.android.sp;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/18/16.
 */
public class CheckInDetails {
    int checkouthours=0,checkoutmins=0;
    int dollars=0,cents=0;
    double longitude=0.0;
    String ID = "";
    boolean readyToLeave=false;



    public CheckInDetails(){}


    public CheckInDetails(double longitude, int checkouthours,int checkoutmins,int dollars,int cents,String ID,boolean readyToLeave){

        this.longitude=longitude;
        this.checkouthours=checkouthours;
        this.checkoutmins=checkoutmins;
        this.dollars=dollars;
        this.cents=cents;
        this.ID=ID;
        this.readyToLeave=readyToLeave;
    }


    public double   getLongitude(){return this.longitude;}
    public int      getCheckouthours(){return this.checkouthours;}
    public int      getCheckoutmins(){return this.checkoutmins;}
    public int      getDollars(){return this.dollars;}
    public int      getCents(){return this.cents;}
    public String   getID(){return this.ID;}
    public boolean  getreadyToLeave(){return this.readyToLeave;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("longitude",this.longitude);
        result.put("checkOutHours",this.checkouthours);
        result.put("checkOutMins",this.checkoutmins);
        result.put("dollars",this.dollars);
        result.put("cents",this.cents);
        result.put("ID",this.ID);
        result.put("readyToLeave",this.readyToLeave);

        return result;
    }
}
