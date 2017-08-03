package com.application.android.sp;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 7/15/17.
 */

public class ARUsers {

    private String latlngcode="";
    private String key="";

    public ARUsers(){}

    public ARUsers(String latlngcode,String key){
        this.latlngcode = latlngcode;
        this.key        = key;
    }

    public String getlatlngcode(){return this.latlngcode;}
    public String getkey(){return this.key;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latlngcode",this.latlngcode);
        result.put("key",this.key);
        return result;
    }

}
