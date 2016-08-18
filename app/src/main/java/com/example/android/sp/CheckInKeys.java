//This is the class for CheckIn objects
package com.example.android.sp;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/4/16.
 */
public class CheckInKeys {
    double latitude=0.0;
    String key="";



    public CheckInKeys(){}


    public CheckInKeys(double latitude, String key){

        this.latitude=latitude;
        this.key     = key;
    }


    public double   getLatitude(){return this.latitude;}
    public String   getKey()     {return this.key;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude",this.latitude);
        result.put("key",this.key);

        return result;
    }

}


