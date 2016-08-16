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
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.Firebase;
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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {

    //Necessary global variable declarations
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    public  double latitude;
    public  double longitude;
    float zoom = 16;
    public String checkinTime;
    public final static String EXTRA_MESSAGE = "com.example.android.";
    public final static String fbl = "fbl";
    Location mCurrentLocation;
    LocationRequest mLocationRequest = LocationRequest.create();
    Marker marker;
    EditText hour,min;
    EditText dollar,cent;
    Calendar calendar;
    LatLng place;
    SimpleDateFormat simpleDateFormat;
    private DatabaseReference database;
    String UID="";







    //The onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent1 = getIntent();           //Receive intent from Login Activity
        UID     = intent1.getStringExtra(FacebookLogin.UID);

        SupportMapFragment mapFragment =       //Load the fragment with the google map
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
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
    public void onLocationChanged(Location location) {   //triggered after location change
        mCurrentLocation = location;                     //stores current location
        updateUI();                                      //will update UI accordingly
    }


    private void updateUI() {
        latitude = mCurrentLocation.getLatitude();       //get the latitude
        longitude = mCurrentLocation.getLongitude();     //get the longitude
        place = new LatLng(latitude, longitude);  //initiate LatLng object
        if (marker != null) {
            marker.remove();                             //remove the previous marker on the map
        }
        marker = map.addMarker(new MarkerOptions().position(place).title("You're here").draggable(true)); //add marker at new location
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, zoom));                                   //zoom on the location

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
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //if yes, request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
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

    //Called on clicking the checkout button
    public void checkIn(View view) {

        //get all values from the textboxes

        hour = (EditText) findViewById(R.id.hour);
        min = (EditText) findViewById(R.id.min);
        dollar = (EditText) findViewById(R.id.dollar);
        cent = (EditText) findViewById(R.id.cent);

        //Time checkoutTime = new Time(Integer.parseInt(hour.getText().toString()),Integer.parseInt(min.getText().toString()));
        int dollars = Integer.parseInt(dollar.getText().toString());
        int cents  = Integer.parseInt(cent.getText().toString());


        database = FirebaseDatabase.getInstance().getReference();
        checkinTime = simpleDateFormat.format(calendar.getTime());
        String key = database.child("CheckIns").push().getKey();
        CheckIn checkIn = new CheckIn(latitude,longitude,Integer.parseInt(hour.getText().toString()),
                Integer.parseInt(min.getText().toString()),dollars,cents,UID,true);
        Map<String, Object> checkInValues = checkIn.toMap();
        HashMap<String, Object> checkInMap = new HashMap<>();
        checkInMap.put("latitude", latitude);
        checkInMap.put("longitude",longitude);
        checkInMap.put("checkInTime", checkinTime );

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/CheckIns/"+key,checkInMap);
        childUpdates.put("/CheckInDetails/"+key,checkInValues);
        database.updateChildren(childUpdates);

        //intent.putExtra(EXTRA_MESSAGE,message);                              //put info in the intent and then start the next activity
        //Intent intent = new Intent(this, CheckedIn.class);
        //startActivity(intent);

    }

    //gets called on pressing the logout button
    public void backtologin(View view){
        String message1 = "1";

        Intent intent3 = new Intent(MainActivity.this, FacebookLogin.class);  //pass intent to login activity
        intent3.putExtra(fbl,message1);                                       //put the boolean string into it
        startActivity(intent3);                                               //start login activity and kill itself
        finish();

    }
}