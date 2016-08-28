//This is the activity for Check-ins
package com.example.android.sp;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//All the imports
import android.app.AlarmManager;
import android.app.Notification;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.support.v4.app.NotificationCompat;

import org.json.JSONObject;


public class CheckInActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener,CheckInDialog.CheckInDialogListener{

    //Necessary global variable declarations
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    public  double curlatitude;
    public  double curlongitude;
    float zoom = 16;
    public String checkinTime;
    public final static String EXTRA_MESSAGE = "com.example.android.";
    public final static String fbl = "fbl";
    private static final String TAG = "Debugger ";
    Location mCurrentLocation;
    TimePicker timePicker;
    EditText rate;
    Calendar calendar;
    LatLng place;
    SimpleDateFormat simpleDateFormat;
    private DatabaseReference database;
    String UID="",navigate;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    LocationRequest mLocationRequest;
    boolean inputerror= false;
    int i=0;
    double hours,mins,dollars,cents,checkinhour,checkinmin;
    public static String PACKAGE_NAME;
    String startedfrom;




    //The onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        Intent intent1 = getIntent();           //Receive intent from Options Activity
        UID     = intent1.getStringExtra(OptionsActivity.ID); //Receive logged in user's unique ID

        String startedfrom= intent1.getStringExtra("started_from");
        Log.d(TAG,"startedfrom :"+startedfrom);


        SupportMapFragment mapFragment =       //Load the fragment with the google map
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GoogleSignInOptions checkingso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("283432722166-icn0f1dke2845so2ag841mpvdklssum7.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, checkingso)
                .build();
        mLocationRequest = new LocationRequest();            // Create location request
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS); //periodically update location
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        calendar = Calendar.getInstance();                    //get current time
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");  //and set its format
        PACKAGE_NAME = getApplicationContext().getPackageName();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart(){
        mGoogleApiClient.connect();      //onStart of the activity, connect apiclient
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();  //disconnect apiclient on stop
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();       //stop location updates when activity pauses as defined below
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {    //start location updates once apiclient is connected
            startLocationUpdates();
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        //call this function
        startLocationUpdates();

    }


    protected void startLocationUpdates() {

        //you need to check first if you have permissions from user
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            //ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //if yes, request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);         //location request requests updates periodically

    }

    @Override
    public void onLocationChanged(Location location) {   //triggered after location change
        mCurrentLocation = location;                     //stores current location
        updateUI();                                      //will update UI accordingly
    }

    private void updateUI() {
        Log.d(TAG, "yayy location updated !!!");
        curlatitude = mCurrentLocation.getLatitude();       //get the latitude
        curlongitude = mCurrentLocation.getLongitude();     //get the longitude
        place = new LatLng(curlatitude, curlongitude);  //initiate LatLng object
        //first time this method is called, put a marker on user's location and zoom in on it
        if(i==0){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, zoom));//zoom on the location
        }
        i=i+1;
        if(false){
            Log.d(TAG,"navigating");
            LatLng origin = new LatLng(curlatitude,curlongitude);
            LatLng dest = new LatLng(40.5279534271587,-74.4646624848246);
            String url = getUrl(origin, dest);
            FetchUrl FetchUrl = new FetchUrl();
            FetchUrl.execute(url);
        }
    }


    @Override
    public void onConnectionSuspended(int x){
        //notify user of lost connection
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT); //notify user when connection is suspended
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;                    //when GoogleMap is ready, put it into the existing map object

    }


    public void showCheckInDialog(View v) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new CheckInDialog();
        dialog.show(getSupportFragmentManager(),"CheckIn fragment");

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Dialog dialogView = dialog.getDialog();
        rate = (EditText) dialogView.findViewById(R.id.rate);
        timePicker = (TimePicker) dialogView.findViewById(R.id.time);
        Double rph = toDouble(rate.getText().toString());
        dollars =  Math.floor(rph);
        cents = (100*(rph - Math.floor(rph)));
        hours = (double)timePicker.getCurrentHour();
        mins = (double) timePicker.getCurrentMinute();
        checkIn();

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }

    //Called on clicking the checkout button
    public void checkIn() {



        if(inputerror){
            Toast.makeText(this,"Please enter correct rate",Toast.LENGTH_LONG).show();  //not used currently
            inputerror=false;
        }
        else {

            CameraPosition position = map.getCameraPosition();
            LatLng cameracenter = position.target;
            database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
            checkinTime = simpleDateFormat.format(calendar.getTime());  //get the checkin time
            String[] timearray = checkinTime.split(":");
            checkinhour = Double.parseDouble(timearray[0]);
            checkinmin = Double.parseDouble(timearray[1]);
            String LatLngCode = getLatLngCode(cameracenter.latitude,cameracenter.longitude);  //convert the checkin location to its LatLngCode
            Log.d(TAG, "LatLngCode : " + LatLngCode);

            String key = database.child("CheckInKeys/"+LatLngCode).push().getKey();  //push an entry into the database and get its key
            //construct the CheckInDetails object
            CheckInDetails checkInDetails = new CheckInDetails(cameracenter.latitude,cameracenter.longitude,hours,mins,dollars,cents,UID,false);
            Map<String, Object> checkInDetailsMap = checkInDetails.toMap(); //call its toMap method
            CheckInUser user = new CheckInUser(LatLngCode,key);            // construct the CheckInUser object
            Map<String, Object> userMap = user.toMap();                    //call its toMap method

            Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
            childUpdates.put("/CheckInKeys/"+LatLngCode+"/"+key, checkInDetailsMap);
            childUpdates.put("/CheckInUsers/"+UID,userMap);
            database.updateChildren(childUpdates);                        //simultaneously update the database at both locations
            inputerror=false;

            //intent.putExtra(EXTRA_MESSAGE,message);                              //put info in the intent and then start the next activity
            //Intent servIntent = new Intent(this,LocationService.class);
            //startService(servIntent);
            int delay = (int)getDelay(checkinhour,checkinmin,hours,mins) - 900000;
            if (delay<0){
                Toast.makeText(CheckInActivity.this,"Cannot set notification",Toast.LENGTH_LONG).show();
                return;
            }
            scheduleNotification(getNotification(),delay);
            Intent intent = new Intent(this, CheckedIn.class);
            startActivity(intent);
        }

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
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(android.graphics.Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                map.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
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

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&key=";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters +key;


        return url;
    }

    public Double toDouble(String var){                                   //currently unused function
        try{
            Double i = Double.parseDouble(var.trim());
            return i;
        }

        catch (NumberFormatException nfe){
            inputerror = true;
            return 0.;
        }

    }

    //function to generate the LatLngCode
    public String getLatLngCode(double lat, double lon){

        lat = lat*100;     //get the centi latitudes and centi longitudes
        lon = lon*100;
        int lat1 = (int)Math.round(lat);   //round them off
        int lon1 = (int)Math.round(lon);
        String lons = Integer.toString(lon1);   //convert them to strings
        String lats = Integer.toString(lat1);

        if(lon1>=0){                //concatenate those strings to form the code
            lons = "+"+lons;
        }
        if(lat1>=0){
            lats = "+"+lats;
        }
        return (lons+lats);
    }

    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification() {

        Intent navigate = new Intent(this, NavigationActivity.class);
        navigate.putExtra("user_id",UID);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, navigate, 0);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.accept, "Navigate to Car", pIntent).build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle("SpotPark");
        builder.setContentText("Parking Ticket expires in 15min !");
        builder.addAction(accept);


        return builder.build();
    }

    public double getDelay(double checkinhour,double checkinmin,double checkouthour,double checkoutmin){
        double comins;
        double cimins;
        double mindelay=0.;
        cimins = checkinhour*60 + checkinmin;
        comins = checkouthour*60 + checkoutmin;
        if(comins>=cimins){
            mindelay = comins - cimins;
        }
        if(comins<cimins){
            mindelay = (24*60-cimins)+comins;
        }
        return (mindelay*60*1000);
    }

    //gets called on pressing the logout button
    public void backtologin(View view){
        String message1 = "1";
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG,"google signed out");
                    }
                });
        Intent intent3 = new Intent(CheckInActivity.this, LoginActivity.class);  //pass intent to login activity
        intent3.putExtra(fbl,message1);                                       //put the boolean string into it
        startActivity(intent3);                                               //start login activity and kill itself
        finish();

    }


}