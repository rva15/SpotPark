package com.example.android.sp;
//All imports
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruturaj on 8/28/16.
 */
public class WalkTimeParser {

    public int parse(JSONObject jObject){

        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;
        int mins=0;
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
                        mins = mins+ Integer.parseInt(output[0]);
                    }

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }


        return mins;
    }


}
