package com.example.android.sp;
//All imports
import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/15/16.
 */
public class CheckInUser {

    private double carlatitude;
    private double carlongitude;
    private String latlngcode;
    private String key;
    private int couthours,coutmins;


    public CheckInUser() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.android.sp.CheckInUser.class)
    }

    public CheckInUser(double carlatitude, double carlongitude,int couthours,int coutmins,String latlngcode,String key) {
        this.carlatitude     = carlatitude;
        this.carlongitude    = carlongitude;
        this.couthours       =couthours;
        this.coutmins        =coutmins;
        this.latlngcode      = latlngcode;
        this.key             = key;

    }

    public double getcarlatitude(){
        return this.carlatitude;
    }
    public double getcarlongitude(){
        return this.carlongitude;
    }
    public int    getcouthours(){return this.couthours;}
    public int    getcoutmins(){return this.coutmins;}
    public String getlatlngcode(){return  this.latlngcode;}
    public String getkey(){return  this.key;}


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("carlatitude", carlatitude);
        result.put("carlongitude", carlongitude);
        result.put("couthours",this.couthours);
        result.put("coutmins",this.coutmins);
        result.put("latlngcode",latlngcode);
        result.put("key",key);

        return result;
    }

}
