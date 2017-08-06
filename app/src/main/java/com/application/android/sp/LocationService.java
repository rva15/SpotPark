package com.application.android.sp;
//All imports
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.lang.Math;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.application.android.sp.SPApplication.getContext;

/**
 * Created by ruturaj on 8/21/16.
 */
public class LocationService extends android.app.Service{

    //-------------------------Necessary variable declarations--------------//
    private static final String TAG = "Debugger ";
    private LocationManager mLocationManager = null;
    private int LOCATION_INTERVAL = 15000;  //request updates every 15secs
    private float LOCATION_DISTANCE = 10;    //but only if user has moved 10meters
    private int count = 0,i=0;
    private String UID ="",key="",currTime="";
    private CheckInHelperDB dbHelper;
    private Double carlat,carlon,checkinhour,checkinmin,lasthour,lastmin,diff,lastentry;
    private SimpleDateFormat simpleDateFormat;
    private Calendar calendar;

    //---------------------------Service LifeCycle Methods------------------------//

    //onCreate method
    @Override
    public void onCreate(){

        initializeLocationManager();
        try {

            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, //request location updates through network
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,  //request location updates through GPS
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        }

    }

    private void initializeLocationManager() {
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
        if(res!=null) {
            res.moveToFirst();                              //get all info from the CheckInHelper db
            UID = res.getString(res.getColumnIndex("_id"));
            carlat = res.getDouble(res.getColumnIndex("Carlatitude"));
            carlon = res.getDouble(res.getColumnIndex("Carlongitude"));
            checkinhour = res.getDouble(res.getColumnIndex("CheckInHour"));
            checkinmin = res.getDouble(res.getColumnIndex("CheckInMin"));
            lasthour = res.getDouble(res.getColumnIndex("LastHour"));
            lastmin = res.getDouble(res.getColumnIndex("LastMin"));
            lastentry = lasthour * 60 + lastmin;
            if(TextUtils.isEmpty(UID)){
                stopSelf();
            }
        }
        else{
            stopSelf();
        }


        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");    //initialize a date format
        return START_STICKY;                                //START_STICKY to keep service going
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


    //-----------------------Notification Related Functions------------------------------//

    private void scheduleNotification(Notification notification, int delay,int unique) { //schedules the inform notification immediately after the right conditions are met

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, unique);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, unique, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }


    private Notification getInformNotification() {

        //open the app on tapping the notification
        Intent openapp = new Intent(getContext(), LoginActivity.class);
        openapp.putExtra("startedfrom","notification");
        openapp.putExtra("userid",UID);
        openapp.addCategory("servicealert");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openapp, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent serviceintent = new Intent(this,DirectionService.class);
        serviceintent.putExtra("started_from","LS");            //inform that it was started from location service
        PendingIntent pIntent = PendingIntent.getService(this, 0, serviceintent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.accept, "Yes", pIntent).build();
        Intent buttonIntent = new Intent(getContext(), CancelNotification.class);
        buttonIntent.putExtra("notificationId",29);
        PendingIntent btPendingIntent = PendingIntent.getBroadcast(getContext(), 0, buttonIntent,0);
        NotificationCompat.Action cancel = new NotificationCompat.Action.Builder(R.drawable.clear, "No", btPendingIntent).build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.tab_background_unselected));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle("SpotPark");
        builder.setContentText("Vacating your parking spot soon?");
        builder.addAction(accept);
        builder.addAction(cancel);
        builder.setAutoCancel(true);
        builder.setSound(Uri.parse("android.resource://" + getContext().getPackageName() + "/" +R.raw.expiry)); //custom ringtone


        return builder.build();

    }

    //-----------------------private locationlistener class-------------------------//

    private class LocationListener implements android.location.LocationListener{

        Location mLastLocation;

        public LocationListener(String provider)       //constructor
        {

            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {

            double lat = location.getLatitude();        //get current location
            double lon = location.getLongitude();

            mLastLocation.set(location);         //set mLastLocation to latest location
            calendar = Calendar.getInstance();   //get the current time
            currTime = simpleDateFormat.format(calendar.getTime());
            String[] timearray = currTime.split(":");
            double diff = gettimediff(Double.parseDouble(timearray[0]),Double.parseDouble(timearray[1]),checkinhour*60+checkinmin);
            if(diff>36000000){
                stopSelf();         //stop this service if it has been running longer than 10 hours
            }

            double distance = distance(lat,lon,carlat,carlon);
            if(distance>0.1){ //distance from car is more than 100m
                LOCATION_DISTANCE = 30;
            }
            else{
                LOCATION_DISTANCE = 5;
            }

            if(distance<0.05){  //if you are within 50m of the car

                double difference = gettimediff(Double.parseDouble(timearray[0]),Double.parseDouble(timearray[1]),lastentry);
                if(difference>15){  //and the difference between right now and last time you were in the zone is >15min
                    if(count==0) {
                        scheduleNotification(getInformNotification(), 1000, 29);//notify user immediately
                        stopSelf(); //and then stopSelf
                    }
                    count=count+1;
                }
                else{
                    lastentry = Double.parseDouble(timearray[0])*60 + Double.parseDouble(timearray[1]);//otherwise update the lastentry field in db
                    dbHelper.updateInfo(UID,carlat,carlon,checkinhour,checkinmin,Double.parseDouble(timearray[0]),Double.parseDouble(timearray[1]));
                }

            }

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

        public double gettimediff(double lh,double lm,double chinmins){ //function returns timedifference between two times
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

        }

        @Override
        public void onProviderEnabled(String provider)
        {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

    }




    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),             //setup location listeners
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };



}

