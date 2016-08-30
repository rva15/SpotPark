package com.example.android.sp;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ruturaj on 8/28/16.
 */
public class WalkTimeParser {
    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public int parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<>() ;
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;
        int seconds=0;
        String textsec="";
        final String TAG="";

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){

                        textsec = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("duration")).get("text");
                        String[] output = textsec.split(" ");
                        seconds = seconds+ Integer.parseInt(output[0]);
                        Log.d(TAG,"seconds : "+seconds);
                    }

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }


        return seconds;
    }


}
