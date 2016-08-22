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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class CheckInActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleMap.OnMarkerDragListener{

    //Necessary global variable declarations
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    public  double curlatitude,markerlatitude;
    public  double curlongitude,markerlongitude;
    float zoom = 16;
    public String checkinTime;
    public final static String EXTRA_MESSAGE = "com.example.android.";
    public final static String fbl = "fbl";
    private static final String TAG = "Debugger ";
    Location mCurrentLocation;
    Marker marker;
    TimePicker timePicker;
    EditText dollar,cent;
    Calendar calendar;
    LatLng place;
    SimpleDateFormat simpleDateFormat;
    private DatabaseReference database;
    String UID="";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    LocationRequest mLocationRequest;
    boolean inputerror= false;
    int i=0;
    LatLng markerposition;




    //The onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        Intent intent1 = getIntent();           //Receive intent from Options Activity
        UID     = intent1.getStringExtra(OptionsActivity.ID); //Receive logged in user's unique ID


        SupportMapFragment mapFragment =       //Load the fragment with the google map
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = new LocationRequest();            // Create location request
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS); //periodically update location
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        calendar = Calendar.getInstance();                    //get current time
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");  //and set its format


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
            marker = map.addMarker(new MarkerOptions().position(place).title("You're here").draggable(true)); //enable marker dragging
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, zoom));                                   //zoom on the location
            markerposition = place;                 //default marker position
        }
        i=i+1;

    }


    @Override
    public void onConnectionSuspended(int x){
        //notify user of lost connection
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT); //notify user when connection is suspended
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;                    //when GoogleMap is ready, put it into the existing map object
        map.setOnMarkerDragListener(this);  //set the on marker drag listener to the map

    }

    @Override
    public void onMarkerDrag(Marker marker){
        //Do nothing
    }

    @Override
    public void onMarkerDragEnd(Marker marker){
        markerposition = marker.getPosition();    //update the markerposition to marker's position at the end of marker drag
    }

    @Override
    public void onMarkerDragStart(Marker arg0) {
        // nothing
    }


    //Called on clicking the checkout button
    public void checkIn(View view) {

        //get all values from the textboxes
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        dollar = (EditText) findViewById(R.id.dollar);
        cent = (EditText) findViewById(R.id.cent);
        //get the current postition of marker
        markerlatitude = markerposition.latitude;
        markerlongitude = markerposition.longitude;

        //convert all variables input by user to doubles
        double dollars = Double.parseDouble(dollar.getText().toString());
        double cents  = Double.parseDouble(cent.getText().toString());
        double hours = timePicker.getCurrentHour();
        double mins = timePicker.getCurrentMinute();

        if(inputerror){
            Toast.makeText(this,"Please enter Integer values",Toast.LENGTH_LONG).show();  //not used currently
            inputerror=false;
        }
        else {

            database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
            checkinTime = simpleDateFormat.format(calendar.getTime());  //get the checkin time
            String LatLngCode = getLatLngCode(markerlatitude,markerlongitude);  //convert the checkin location to its LatLngCode
            Log.d(TAG, "LatLngCode : " + LatLngCode);

            String key = database.child("CheckInKeys/"+LatLngCode).push().getKey();  //push an entry into the database and get its key
            //construct the CheckInDetails object
            CheckInDetails checkInDetails = new CheckInDetails(markerlongitude,markerlatitude,hours,mins,dollars,cents,UID,false);
            Map<String, Object> checkInDetailsMap = checkInDetails.toMap(); //call its toMap method
            CheckInUser user = new CheckInUser(LatLngCode,key);            // construct the CheckInUser object
            Map<String, Object> userMap = user.toMap();                    //call its toMap method

            Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
            childUpdates.put("/CheckInKeys/"+LatLngCode+"/"+key, checkInDetailsMap);
            childUpdates.put("/CheckInUsers/"+UID,userMap);
            database.updateChildren(childUpdates);                        //simultaneously update the database at both locations
            inputerror=false;

            //intent.putExtra(EXTRA_MESSAGE,message);                              //put info in the intent and then start the next activity
            Intent servIntent = new Intent(this,LocationService.class);
            startService(servIntent);
            Intent intent = new Intent(this, CheckedIn.class);
            startActivity(intent);
        }

    }

    public int toInt(String var){                                   //currently unused function
        try{
            int i = Integer.parseInt(var.trim());
            return i;
        }

        catch (NumberFormatException nfe){
            inputerror = true;
            return 0;
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

    //gets called on pressing the logout button
    public void backtologin(View view){
        String message1 = "1";

        Intent intent3 = new Intent(CheckInActivity.this, LoginActivity.class);  //pass intent to login activity
        intent3.putExtra(fbl,message1);                                       //put the boolean string into it
        startActivity(intent3);                                               //start login activity and kill itself
        finish();

    }
}