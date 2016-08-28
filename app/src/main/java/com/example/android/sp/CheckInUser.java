package com.example.android.sp;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/15/16.
 */
public class CheckInUser {

    public String latlngcode;
    public String key;

    public CheckInUser() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.android.sp.CheckInUser.class)
    }

    public CheckInUser(String latlngcode, String key) {
        this.latlngcode     = latlngcode;
        this.key    = key;

    }

    public String getlatlngcode(){
        return this.latlngcode;
    }
    public String getkey(){
        return this.key;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latlngcode", latlngcode);
        result.put("key", key);

        return result;
    }

}
