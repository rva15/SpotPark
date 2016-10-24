package com.example.android.sp;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.android.sp.WalkTimeParser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by ruturaj on 8/28/16.
 */
public class WalkTime {
    //Necessary variables
    double carlatitude,carlongitude,userlatitude,userlongitude;
    public final static String TAG="";
    public DatabaseReference database;
    String latlngcode="",key="",UID="";
    int totalmins=0,count=0;

    public WalkTime(){} //empty constructor

    public WalkTime(double carlatitude,double carlongitude,double userlatitude,double userlongitude,String UID){
        this.carlatitude = carlatitude;
        this.carlongitude = carlongitude;
        this.userlatitude = userlatitude;
        this.userlongitude = userlongitude;
        this.UID  = UID;
    }

    public void getWalkTime(){
        LatLng origin = new LatLng(userlatitude,userlongitude);
        LatLng dest = new LatLng(carlatitude,carlongitude);
        String url = getUrl(origin, dest); //fetch url to connect to google maps
        FetchUrl FetchUrl = new FetchUrl();
        FetchUrl.execute(url); //execure this asynctask
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        java.io.InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;


            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                WalkTimeParser parser = new WalkTimeParser(); //initiate walktime parser object
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                totalmins = parser.parse(jObject);  //get mins required to walk to destination

                if(totalmins>10){
                    //do nothing
                }
                else{
                    updatedata(); //update entry in database if mins is less than 10
                }

                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask totalmins",Integer.toString(totalmins));

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        public void updatedata(){
            database = FirebaseDatabase.getInstance().getReference();
            updatewalktime();
        }

        public void updatewalktime() {
            ValueEventListener valuelistener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    if (count == 0) {
                        if (dataSnapshot.exists()) {
                            Log.d(TAG, "exists");
                            CheckInUser user = dataSnapshot.getValue(CheckInUser.class);
                            latlngcode = user.getlatlngcode();
                            key = user.getkey();

                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/CheckInKeys/"+latlngcode+"/"+key+"/minstoleave", totalmins); //update the total mins required
                            database.updateChildren(childUpdates);


                        } else if (!dataSnapshot.exists()) {
                            Log.d(TAG, "not exists");


                        }

                    }
                    count = count + 1;

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

                }
            };
            database.child("CheckInUsers").child(UID).addListenerForSingleValueEvent(valuelistener);
        }


        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

        }
    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        //Api key
        String key = "AIzaSyDKQYvSAVhRH6s8WW-RmtJPAyLnbjA9t8I";

        String mode = "&mode=walking";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + mode+"&key=";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters +key;
        Log.d(TAG,"url is "+url);

        return url;
    }

}
