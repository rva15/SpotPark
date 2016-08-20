package com.example.android.sp;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/15/16.
 */
public class CheckInUser {

    public String LatLngCode;
    public String key;

    public CheckInUser() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.android.sp.CheckInUser.class)
    }

    public CheckInUser(String LatLngCode, String key) {
        this.LatLngCode     = LatLngCode;
        this.key    = key;

    }

    public String getLatLngCode(){
        return this.LatLngCode;
    }
    public String getKey(){
        return this.key;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("LatLngCode", LatLngCode);
        result.put("key", key);

        return result;
    }

}
