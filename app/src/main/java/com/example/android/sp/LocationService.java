package com.example.android.sp;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import java.lang.Math;
import java.util.HashMap;
import java.util.Map;

import com.example.android.sp.WalkTime;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by ruturaj on 8/21/16.
 */
public class LocationService extends android.app.Service{

    public final static int MINUTE = 1000 * 60;
    private static final String TAG = "Debugger ";
    public NotificationManager mNM;
    private int NOTIFICATION = 1;
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 15000;
    private static final float LOCATION_DISTANCE = 0;
    int count = 0,i=0;
    String UID ="",key="";
    public DatabaseReference database;
    private ExampleDBHelper dbHelper;
    Double carlat,carlon;


    //onCreate method
    @Override
    public void onCreate(){
        Log.d(TAG, "running service");

        initializeLocationManager();
        try {

            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        /*showNotification();

        stopSelf();*/
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        }
    }



    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public LocationService getService() {
            // Return this instance of LocalService so clients can call public
            // methods
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        dbHelper = new ExampleDBHelper(this);
        Cursor res = dbHelper.getInfo();
        res.moveToFirst();
        UID = res.getString(res.getColumnIndex("_id"));
        carlat = res.getDouble(res.getColumnIndex("Carlatitude"));
        carlon = res.getDouble(res.getColumnIndex("Carlongitude"));

        Log.d(TAG,"Location Service params " + key);
        Log.d(TAG,"Location Service params " + carlat.toString());
        Log.d(TAG,"Location Service params" + carlon.toString());
        return START_STICKY;
    }


    private void showNotification() {
        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
               // new Intent(this, LoginActivity.class), 0);

        /*Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon)  // the status icon
                .setTicker("you are in the zone!")  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.app_name))  // the label of the entry
                .setContentText("you are in the zone!")  // the contents of the entry
                .build();

        mNM.notify(NOTIFICATION, notification);*/


    }

    private class LocationListener implements android.location.LocationListener{

        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.d(TAG,"this shit fired");

            double lat = location.getLatitude();
            double lon = location.getLongitude();

            if (count < 10) {
                WalkTime walkTime = new WalkTime(carlat.doubleValue(), carlon.doubleValue(), lat, lon, UID);
                walkTime.getWalkTime();
            }
            count = count + 1;
            Log.e(TAG, "dinesh: " + Double.toString(location.getLatitude()) + " " + Double.toString(location.getLongitude()));
            mLastLocation.set(location);


        }


        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }

    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };


}
