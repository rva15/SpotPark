package com.application.android.sp;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 9/8/16.
 */
public class ReportedDetails {
    double latitude,longitude;
    int verification;
    String id;

    public ReportedDetails(){}

    public ReportedDetails(double latitude,double longitude, int verification,String id){
        this.latitude=latitude;
        this.longitude=longitude;
        this.verification=verification;
        this.id= id;
    }

    public double getlongitude(){return this.longitude;}
    public double getlatitude(){return this.latitude;}
    public int getverification(){return this.verification;}
    public String getid(){return this.id;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude",this.latitude);
        result.put("longitude",this.longitude);
        result.put("verification",this.verification);
        result.put("id",this.id);

        return result;
    }

}
