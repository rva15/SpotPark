package com.application.android.sp;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 4/9/17.
 */

public class Places {

    private String placename="";
    private double placelat;
    private double placelon;


    public Places(){}

    public Places(String placename, double placelat, double placelon){
        this.placename = placename;
        this.placelat  = placelat;
        this.placelon  = placelon;
    }

    public String getplacename(){ return this.placename;}
    public double getplacelat() { return this.placelat;}
    public double getplacelon() { return this.placelon;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("placename", this.placename);
        result.put("placelat", this.placelat);
        result.put("placelon", this.placelon);


        return result;
    }

}
