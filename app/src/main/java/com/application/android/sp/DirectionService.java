package com.application.android.sp;
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
import android.text.TextUtils;

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
    private int count = 0;
    private String UID ="",key="",origin="";
    private CheckInHelperDB dbHelper;
    private Double carlat=0.,carlon=0.;
    private DatabaseReference database;
    private DirectionService directionService;
    private boolean initwt=false;

    //---------------------------Service Lifecycle Methods---------------------------------//


    //onCreate method
    @Override
    public void onCreate(){
        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        manager.cancel(1);   //remove the alert notification from Checkin Fragment
        manager.cancel(23);  //remove the inform notification from Checkin Fragment
        manager.cancel(29);  //remove notification from Location Service


        initializeLocationManager(30000,20);



    }

    private void initializeLocationManager(int locinterval, int locdistance) {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        try {

            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, locinterval, locdistance, //ask network for location
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, locinterval, locdistance, //ask GPS for location
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
        } catch (IllegalArgumentException ex) {
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
        if(res!=null) {
            res.moveToFirst();
            UID = res.getString(res.getColumnIndex("_id"));             //get location of the car, userID
            carlat = res.getDouble(res.getColumnIndex("Carlatitude"));
            carlon = res.getDouble(res.getColumnIndex("Carlongitude"));
            if(TextUtils.isEmpty(UID) || carlat==0. || carlon==0.){
                stopSelf();
            }
        }
        else{
            stopSelf();
        }

        if(intent!=null) {
            //possible duplication happening here but still harmless
            incrementKeys();   //award user with 2 keys
            origin = (String) intent.getExtras().get("started_from");
            if(origin.equals("LS") || origin.equals("navigation")){ //this was started from location service/carlocation fragment
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
                stopService(new Intent(DirectionService.this,LocationService.class)); //stop the location service
            }
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(mLocationManager!=null) {
            mLocationManager.removeUpdates(mLocationListeners[0]); //remove location listeners on service destroy
            mLocationManager.removeUpdates(mLocationListeners[1]);
        }
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

            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {

            double lat = location.getLatitude();    //get current location
            double lon = location.getLongitude();
            double distance = distance(lat,lon,carlat,carlon);


            if(distance<0.84) { //user is less than 840m from car
                initializeLocationManager(15000,10);
                if (count < 30) {           //send a maximum of 30 calls to the Directions API
                    WalkTime walkTime = new WalkTime(carlat.doubleValue(), carlon.doubleValue(), lat, lon, UID, getApplicationContext());
                    walkTime.getWalkTime(); //get estimated time of walk to the car
                }
                count = count + 1;

                mLastLocation.set(location);
                if (distance < 0.015) {  //user is 15m from the car
                    stopSelf();
                }
            }
            else{
                initializeLocationManager(30000,30);
                if(!initwt){
                    WalkTime walkTime = new WalkTime(carlat.doubleValue(), carlon.doubleValue(), lat, lon, UID, getApplicationContext());
                    walkTime.getWalkTime(); //get estimated time of walk to the car
                    initwt = true;
                }
            }

        }


        @Override
        public void onProviderDisabled(String provider)
        {

        }

        @Override
        public void onProviderEnabled(String provider)
        {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }



        //----------functions to calculate distance---------------//

        private double distance(double lat1, double lon1, double lat2, double lon2) {
            double theta = lon1 - lon2;
            double dist = Math.sin(deg2rad(lat1))
                    * Math.sin(deg2rad(lat2))
                    + Math.cos(deg2rad(lat1))
                    * Math.cos(deg2rad(lat2))
                    * Math.cos(deg2rad(theta));
            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;
            return (dist);
        }

        private double deg2rad(double deg) {
            return (deg * Math.PI / 180.0);
        }

        private double rad2deg(double rad) {
            return (rad * 180.0 / Math.PI);
        }

        //---------------------------------------------------------//

    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };





}
