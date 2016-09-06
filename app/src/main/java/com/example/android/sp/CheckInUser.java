package com.example.android.sp;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/15/16.
 */
public class CheckInUser {

    public double carlatitude;
    public double carlongitude;
    public String latlngcode;
    public String key;

    public CheckInUser() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.android.sp.CheckInUser.class)
    }

    public CheckInUser(double carlatitude, double carlongitude,String latlngcode,String key) {
        this.carlatitude     = carlatitude;
        this.carlongitude    = carlongitude;
        this.latlngcode      = latlngcode;
        this.key             = key;

    }

    public double getcarlatitude(){
        return this.carlatitude;
    }
    public double getcarlongitude(){
        return this.carlongitude;
    }
    public String getlatlngcode(){return  this.latlngcode;}
    public String getkey(){return  this.key;}


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("carlatitude", carlatitude);
        result.put("carlongitude", carlongitude);
        result.put("latlngcode",latlngcode);
        result.put("key",key);

        return result;
    }

}
