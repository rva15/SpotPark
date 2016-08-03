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
import android.database.sqlite.SQLiteDatabase;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.sp.ExampleDBHelper;



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    public  double latitude;
    public  double longitude;
    float zoom = 16;
    public String time="time";
    public String type="type";
    public final static String EXTRA_MESSAGE = "com.example.android.";
    public final static String KEY_EXTRA_CONTACT_ID = "KEY_EXTRA_CONTACT_ID";
    Location mCurrentLocation;
    LocationRequest mLocationRequest = LocationRequest.create();
    Marker marker;
    ExampleDBHelper dbHelper;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent1 = getIntent();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        dbHelper = new ExampleDBHelper(this);


    }

    @Override
    protected void onStart(){
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateUI();
    }

    private void updateUI() {
        latitude = mCurrentLocation.getLatitude();
        longitude = mCurrentLocation.getLongitude();
        LatLng place = new LatLng(latitude, longitude);
        if (marker != null) {
            marker.remove();
        }
        marker = map.addMarker(new MarkerOptions().position(place).title("You're here").draggable(true));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, zoom));

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        startLocationUpdates();

    }


    protected void startLocationUpdates() {

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int x){
        Toast.makeText(this, "Connect suspended", Toast.LENGTH_SHORT);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

    }

    public void onReportTime(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.now:
                if (checked)
                    Toast.makeText(MainActivity.this, "Checkout now", Toast.LENGTH_SHORT).show();
                    time = "now";
                break;
            case R.id.five:
                if (checked)
                    Toast.makeText(MainActivity.this, "Checkout in 5", Toast.LENGTH_SHORT).show();
                    time = "5 mins";
                break;
            case R.id.ten:
                if (checked)
                    Toast.makeText(MainActivity.this, "Checkout in 10", Toast.LENGTH_SHORT).show();
                    time = "now";
                break;
        }
    }

    public void onReportType(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.street:
                if (checked)
                    Toast.makeText(MainActivity.this, "Street Parking", Toast.LENGTH_SHORT).show();
                    type = "Street Parking";
                break;
            case R.id.lot:
                if (checked)
                    Toast.makeText(MainActivity.this, "Parking Lot", Toast.LENGTH_SHORT).show();
                    type = "Parking Lot";
                break;
        }
    }

    public void checkOut(View view) {


        dbHelper.updateKey(0,1);
        Intent intent = new Intent(this, CheckedOut.class);
        String message = time + "  " + type;
        intent.putExtra(EXTRA_MESSAGE,message);
        startActivity(intent);

    }
}
