package com.example.android.sp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.Math;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.firebase.database.DatabaseReference;

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
    String UID ="",key="",checkinTime="";
    public DatabaseReference database;
    private CheckInHelperDB dbHelper;
    Double carlat,carlon,checkinhour,checkinmin,lasthour,lastmin,diff,lastentry;
    SimpleDateFormat simpleDateFormat;
    Calendar calendar;


    //onCreate method
    @Override
    public void onCreate(){
        Log.d(TAG, "running locationservice");

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

        dbHelper = new CheckInHelperDB(this);
        Cursor res = dbHelper.getInfo();
        res.moveToFirst();
        UID = res.getString(res.getColumnIndex("_id"));
        carlat = res.getDouble(res.getColumnIndex("Carlatitude"));
        carlon = res.getDouble(res.getColumnIndex("Carlongitude"));
        checkinhour = res.getDouble(res.getColumnIndex("CheckInHour"));
        checkinmin = res.getDouble(res.getColumnIndex("CheckInMin"));
        lasthour = res.getDouble(res.getColumnIndex("LastHour"));
        lastmin = res.getDouble(res.getColumnIndex("LastMin"));
        lastentry = lasthour*60 + lastmin;


        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Log.d(TAG,"Location Service params " + key);
        Log.d(TAG,"Location Service params " + carlat.toString());
        Log.d(TAG,"Location Service params" + carlon.toString());
        return START_STICKY;
    }

    private void scheduleNotification(Notification notification, int delay,int unique) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, unique);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, unique, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }


    private Notification getInformNotification() {
        Intent serviceintent = new Intent(this,DirectionService.class);
        serviceintent.putExtra("started_from","LS");
        PendingIntent pIntent = PendingIntent.getService(this, 0, serviceintent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.accept, "Yes", pIntent).build();
        NotificationCompat.Action cancel = new NotificationCompat.Action.Builder(R.drawable.cancel, "No", pIntent).build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle("SpotPark");
        builder.setContentText("Inform others that you're leaving?");
        builder.addAction(accept);
        builder.addAction(cancel);
        builder.setAutoCancel(true);


        return builder.build();

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
            //Log.d(TAG,"this shit fired");

            double lat = location.getLatitude();
            double lon = location.getLongitude();


            Log.e(TAG, "dinesh: " + Double.toString(location.getLatitude()) + " " + Double.toString(location.getLongitude()));
            Log.d(TAG,"carpos "+carlat.toString());
            Log.d(TAG,"carpos "+carlon.toString());
            Log.d(TAG,"carpos"+Double.toString(lat));
            Log.d(TAG,"carpos"+Double.toString(lon));
            Log.d(TAG,"carpos"+Double.toString(lastentry));

            mLastLocation.set(location);
            calendar = Calendar.getInstance();
            checkinTime = simpleDateFormat.format(calendar.getTime());
            String[] timearray = checkinTime.split(":");
            Log.d(TAG,"difference :"+timearray[0]);
            Log.d(TAG,"difference :"+timearray[1]);
            double diff = gettimediff(Double.parseDouble(timearray[0]),Double.parseDouble(timearray[1]),checkinhour*60+checkinmin);
            if(diff>36000000){
                stopSelf();
            }

            double deltalat = Math.abs((lat*10000)-(carlat.doubleValue()*10000));
            double deltalon = Math.abs((lon*10000)-(carlon.doubleValue()*10000));
            if((deltalat<3)&&(deltalon<3)){

                double difference = gettimediff(Double.parseDouble(timearray[0]),Double.parseDouble(timearray[1]),lastentry);
                if(difference>15){
                    if(count==0) {
                        scheduleNotification(getInformNotification(), 1000, 29);
                        stopSelf();
                    }
                    count=count+1;
                }
                else{
                    lastentry = Double.parseDouble(timearray[0])*60 + Double.parseDouble(timearray[1]);
                    dbHelper.updateInfo(UID,carlat,carlon,checkinhour,checkinmin,Double.parseDouble(timearray[0]),Double.parseDouble(timearray[1]));
                }

            }

        }

        public double gettimediff(double lh,double lm,double chinmins){
            double currmins = lh*60 + lm;

            if(currmins>=chinmins){
                diff = currmins-chinmins;
                return diff;
            }
            if(chinmins>currmins){
                diff = (24*60 - chinmins)+currmins;
                return diff;
            }
            return 0;
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

    @Override
    public void onDestroy() {
        Log.d(TAG,"on destroy");
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.removeUpdates(mLocationListeners[0]);
        mLocationManager.removeUpdates(mLocationListeners[1]);
    }


}

