package com.example.android.sp;
// All imports
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.Math;


/**
 * Created by ruturaj on 8/21/16.
 */
public class DirectionService extends android.app.Service{

    //Variable Declaration
    private static final String TAG = "Debugger ";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 30000; //obtain new location every 30secs
    private static final float LOCATION_DISTANCE = 5;   //but only if user has moved 5m
    private int count = 0;
    private String UID ="",key="",origin="";
    private CheckInHelperDB dbHelper;
    private Double carlat,carlon;
    private DatabaseReference database;

    //---------------------------Service Lifecycle Methods---------------------------------//


    //onCreate method
    @Override
    public void onCreate(){
        Log.d(TAG, "running direction service");
        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        manager.cancel(1);   //remove the alert notification from Checkin Fragment
        manager.cancel(23);  //remove the inform notification from Checkin Fragment
        manager.cancel(29);  //remove notification from Location Service


        initializeLocationManager();
        try {

            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, //ask network for location
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, //ask GPS for location
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }


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
        public DirectionService getService() {
            // Return this instance of LocalService so clients can call public
            // methods
            return DirectionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        dbHelper = new CheckInHelperDB(this);
        Cursor res = dbHelper.getInfo();
        res.moveToFirst();
        UID = res.getString(res.getColumnIndex("_id"));             //get location of the car, userID
        carlat = res.getDouble(res.getColumnIndex("Carlatitude"));
        carlon = res.getDouble(res.getColumnIndex("Carlongitude"));

        Log.d(TAG,"direction service params " + key);
        Log.d(TAG,"direction params " + carlat.toString());
        Log.d(TAG,"direction params" + carlon.toString());

        if(intent!=null) {
            //possible duplication happening here but still harmless
            incrementKeys();   //award user with 2 keys
            Log.d(TAG,"running direction service with intent");
            origin = (String) intent.getExtras().get("started_from");
            if(origin.equals("LS") || origin.equals("navigation")){ //this was started from location service
                Log.d(TAG,"started from LS or navigation");
                Intent cancelaction = new Intent(this, NotificationPublisher.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, cancelaction, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                Intent cancelaction2 = new Intent(this, NotificationPublisher.class);
                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 23, cancelaction2, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager2 = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager2.cancel(pendingIntent2); //cancel the inform and alert notifications that user would have got
            }
            if(origin.equals("checkin")){ //this was started from the inform notification
                Log.d(TAG,"entered checkin");
                stopService(new Intent(DirectionService.this,LocationService.class)); //stop the location service
            }
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"on destroy");
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.removeUpdates(mLocationListeners[0]); //remove location listeners on service destroy
        mLocationManager.removeUpdates(mLocationListeners[1]);
    }

    //increment user's keys by 2
    private void incrementKeys(){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("UserInformation").child(UID).child("numberofkeys").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long keys = (long) dataSnapshot.getValue();
                keys = keys+2;
                dataSnapshot.getRef().setValue(keys);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //-------------------private location listener class------------------------------//


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

            double lat = location.getLatitude();    //get current location
            double lon = location.getLongitude();

            if (count < 30) {       //send a maximum of 30 calls to the Directions API
                Log.d(TAG,"direction service getting walktime");
                WalkTime walkTime = new WalkTime(carlat.doubleValue(), carlon.doubleValue(), lat, lon, UID);
                walkTime.getWalkTime(); //get estimated time of walk to the car
            }
            count = count + 1;

            mLastLocation.set(location);
            double deltalat = Math.abs((lat*10000)-(carlat.doubleValue()*10000));
            double deltalon = Math.abs((lon*10000)-(carlon.doubleValue()*10000));
            if((deltalat<2)&&(deltalon<2)){
                Log.d(TAG,"stopping directionservice");  //stop direction service once user is near car
                stopSelf();
            }

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
