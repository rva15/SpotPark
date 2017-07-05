package com.app.android.sp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by ruturaj on 6/30/17.
 */

public class ARLocService extends android.app.Service {


    private LocationManager mLocationManager = null;
    private double userlat, userlon;
    private String TAG = "ActivityRecognition";

    //---------------------------Service LifeCycle Methods------------------------//

    //onCreate method
    @Override
    public void onCreate(){
        initializeLocationManager(15000,1000); //declare location manager
        /*mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();*/


    }

    private void initializeLocationManager(int locinterval, int locdistance) {
        if(mLocationManager!=null){
            mLocationManager.removeUpdates(mLocationListeners[0]); //stop getting location updates
            mLocationManager.removeUpdates(mLocationListeners[1]);
        }
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        /*try {

            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return; //return if you dont have permission
            }
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, locinterval, locdistance, //request location updates through network
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        }*/

        try {
            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return; //return if you dont have permission
            }
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, locinterval, locdistance,  //request location updates through GPS
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        }
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public ARLocService getService() {
            return ARLocService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {  //on Destroy is called when stopSelf() executes
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(mLocationManager!=null) {
            mLocationManager.removeUpdates(mLocationListeners[0]); //stop getting location updates
            mLocationManager.removeUpdates(mLocationListeners[1]);
        }
    }

    //-----------------------private locationlistener class-------------------------//

    private class LocationListener implements android.location.LocationListener {

        Location mLastLocation;

        public LocationListener(String provider)       //constructor
        {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            userlat = location.getLatitude();
            userlon = location.getLongitude();
            Log.d(TAG,userlat + " "+ userlon);
            stopSelf();
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }


    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),             //setup location listeners
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
}
