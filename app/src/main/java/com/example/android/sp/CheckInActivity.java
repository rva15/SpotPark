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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class CheckInActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {

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
    //LocationRequest mLocationRequest = LocationRequest.create();
    Marker marker;
    EditText hour,min;
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







    //The onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        Intent intent1 = getIntent();           //Receive intent from Login Activity
        UID     = intent1.getStringExtra(OptionsActivity.ID);


        SupportMapFragment mapFragment =       //Load the fragment with the google map
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        calendar = Calendar.getInstance();                  //get current time
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");


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
                mGoogleApiClient, mLocationRequest, this);

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
        if (marker != null) {
            marker.remove();                             //remove the previous marker on the map
        }
        marker = map.addMarker(new MarkerOptions().position(place).title("You're here").draggable(true)); //add marker at new location
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, zoom));                                   //zoom on the location

    }


    @Override
    public void onConnectionSuspended(int x){
        //notify user of lost connection
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;  //when GoogleMap is ready, put it into the existing map object

    }

    public int toInt(String var){
        try{
            int i = Integer.parseInt(var.trim());
            return i;
        }

        catch (NumberFormatException nfe){
            inputerror = true;
            return 0;
        }

    }

    public String getLatLngCode(double lat, double lon){

        lat = lat*100;
        lon = lon*100;
        int lat1 = (int)Math.round(lat);
        int lon1 = (int)Math.round(lon);
        String lons = Integer.toString(lon1);
        String lats = Integer.toString(lat1);

        if(lon1>=0){
            lons = "+"+lons;
        }
        if(lat1>=0){
            lats = "+"+lats;
        }
        return (lons+lats);
    }

    //Called on clicking the checkout button
    public void checkIn(View view) {

        //get all values from the textboxes
        timePicker = (TimePicker) findViewById(R.id.timePicker);

        dollar = (EditText) findViewById(R.id.dollar);
        cent = (EditText) findViewById(R.id.cent);
        LatLng markerposition = marker.getPosition();
        markerlatitude = markerposition.latitude;
        markerlongitude = markerposition.longitude;

        //Time checkoutTime = new Time(Integer.parseInt(hour.getText().toString()),Integer.parseInt(min.getText().toString()));
        int dollars = toInt(dollar.getText().toString());
        int cents  = toInt(cent.getText().toString());
        int hours = timePicker.getCurrentHour();
        int mins = timePicker.getCurrentMinute();

        if(inputerror){
            Toast.makeText(this,"Please enter Integer values",Toast.LENGTH_LONG).show();
            inputerror=false;
        }
        else {

            database = FirebaseDatabase.getInstance().getReference();
            checkinTime = simpleDateFormat.format(calendar.getTime());
            String LatLngCode = getLatLngCode(markerlatitude,markerlongitude);
            //String LatLngCode = getLatLngCode(12.234,11.54232);
            Log.d(TAG, "LatLngCode : " + LatLngCode);

            String key = database.child("CheckInKeys/"+LatLngCode).push().getKey();

            CheckInDetails checkInDetails = new CheckInDetails(markerlongitude,markerlatitude,hours,mins,dollars,cents,UID,false);
            Map<String, Object> checkInDetailsMap = checkInDetails.toMap();

            /*String lon = Double.toString(markerlongitude);
            lon = lon.replace(".","&");
            Toast.makeText(this,lon,Toast.LENGTH_LONG).show();*/

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/CheckInKeys/"+LatLngCode, key);
            childUpdates.put("/CheckInDetails/" + key, checkInDetailsMap);
            childUpdates.put("/CheckInUsers/"+UID,key);
            database.updateChildren(childUpdates);
            inputerror=false;

            //intent.putExtra(EXTRA_MESSAGE,message);                              //put info in the intent and then start the next activity
            Intent intent = new Intent(this, CheckedIn.class);
            startActivity(intent);
        }

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