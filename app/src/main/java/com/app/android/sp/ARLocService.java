package com.app.android.sp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

import static com.app.android.sp.SPApplication.getContext;

/**
 * Created by ruturaj on 6/30/17.
 */

public class ARLocService extends android.app.Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private LocationManager mLocationManager = null;
    public GoogleApiClient mApiClient;
    private double userlat1=0, userlon1=0,userlat2=0,userlon2=0,userlat3=0,userlon3=0,candlat=0,candlon=0;
    private String TAG = "ActivityRecognition";
    protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    private boolean T1=false,T2=false,T3=false,trackAct=false;
    private boolean l1taken=false,l2taken=false,l3taken=false,inprocess=false;
    private long t1,t2,t3,diff1,diff2,t4,t5;
    private double drivingc,footc,runningc,stillc,tiltingc,walkingc,unknownc;

    //---------------------------Service LifeCycle Methods------------------------//

    //onCreate method
    @Override
    public void onCreate(){
        Log.d(TAG," created");
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
        mApiClient = new GoogleApiClient.Builder(this)  //construct googleapiclient for AR
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect(); //connect it
        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver(); //declare broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, //register the receiver
                new IntentFilter("com.app.android.sp.BROADCAST_ACTION"));

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestActivityUpdates(8000);
    } //request for AR on connection success

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
        removeActivityUpdates();
    }

    //----------Request and remove AR updates--------------//

    public void requestActivityUpdates(int milliseconds) {
        if (mApiClient.isConnected()) {
            Intent intent = new Intent( this, ARService.class );
            PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mApiClient,
                    milliseconds,
                    pendingIntent
            );

        }
    }

    public void removeActivityUpdates() {
        if (mApiClient.isConnected()) {
            Intent intent = new Intent( this, ARService.class );
            PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mApiClient,
                    pendingIntent
            );
        }
    }

    //----------------------------------------------------//

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        drivingc = 0;
        footc=0;
        runningc=0;
        stillc=0;
        tiltingc=0;
        walkingc=0;
        unknownc=0;
        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    if(activity.getConfidence()>50) {
                        triggerOn();
                        inprocess = false;
                        l1taken = false;
                        l2taken = false;
                        l3taken = false;
                        drivingc = activity.getConfidence();
                        Log.d("ActivityRecogition", "In Vehicle: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    if(activity.getConfidence()>50) {
                        Log.d("ActivityRecogition", "On Bicycle: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    if(activity.getConfidence()>50) {
                        footc = activity.getConfidence();
                        Log.d("ActivityRecogition", "On Foot: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.RUNNING: {
                    if(activity.getConfidence()>50) {
                        runningc = activity.getConfidence();
                        Log.d("ActivityRecogition", "Running: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.STILL: {
                    if(activity.getConfidence()>50) {
                        stillc = activity.getConfidence();
                        Log.d("ActivityRecogition", "Still: " + activity.getConfidence());
                    }

                    break;
                }
                case DetectedActivity.TILTING: {
                    if(activity.getConfidence()>50) {
                        tiltingc = activity.getConfidence();
                        Log.d("ActivityRecogition", "Tilting: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.WALKING: {
                    if(activity.getConfidence()>50) {
                        walkingc = activity.getConfidence();
                        Log.d("ActivityRecogition", "Walking: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    if(activity.getConfidence()>50) {
                        unknownc = activity.getConfidence();
                        Log.d("ActivityRecogition", "Unknown: " + activity.getConfidence());
                    }
                    break;
                }
            }
        }

        if(trackAct) {
            if ((footc > 50) || (runningc > 50) || (stillc > 50) || (tiltingc > 50) || (walkingc > 50) || (unknownc > 50) && (drivingc<30)){
                if(!inprocess){
                    initializeLocationManager(2000,0);   //declare location manager
                }
                t4 = System.currentTimeMillis();
                t5 = (t4-t3)/1000;
                if(t5>120){
                    scheduleNotification(getARTestNotification(false), 1000, 411019);//notify user immediately
                    T1 = T2 = T3 =false;
                    trackAct = false;
                }
            }
        }
    }

    private void triggerOn(){
        if(!T1){
            T1 = true;
            t1 = System.currentTimeMillis();
            Log.d(TAG,"t121 "+t1);
            return;
        }
        if(T1 && (!T2) && (!T3)){
            t2 = System.currentTimeMillis();
            Log.d(TAG,"t122 "+t2);
            diff1 = (t2-t1)/1000;
            if(diff1<30){
                T2 = true;
            }
            else{
                T1 = false;
            }
            Log.d(TAG,"t12 diff1 "+diff1);
            return;
        }
        if(T1 && T2 && (!T3)){
            t3 = System.currentTimeMillis();
            Log.d(TAG,"t123 "+t3);
            diff2 = (t3-t2)/1000;
            if(diff2<30){
                T3 =true;
                scheduleNotification(getARTestNotification(true), 1000, 199019);//notify user immediately
                trackAct = true;
            }
            else{
                T1 = false;
                T2 = false;
            }
            Log.d(TAG,"t12 diff2 "+diff2);
            return;

        }
        if(T1 && T2 && T3){
            t3 = System.currentTimeMillis();
        }
    }

    //-----------------------Notification Related Functions------------------------------//

    private void scheduleNotification(Notification notification, int delay, int unique) { //schedules the inform notification immediately after the right conditions are met

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, unique);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, unique, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }


    private Notification getARTestNotification(boolean starteddrive) {

        //open the app on tapping the notification
        Intent openapp = new Intent(getContext(), HomeScreenActivity.class);
        openapp.addCategory("artestalert");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openapp, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.tab_background_unselected));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle("SpotPark");
        if(starteddrive) {
            builder.setContentText("You are driving!!");
        }
        else{
            builder.setContentText("Candidate location "+Double.toString(candlat)+" "+Double.toString(candlon));
            Log.d(TAG,"candidate loc "+Double.toString(candlat)+" "+Double.toString(candlon));
        }
        builder.setAutoCancel(true);
        builder.setSound(Uri.parse("android.resource://" + getContext().getPackageName() + "/" +R.raw.expiry)); //custom ringtone


        return builder.build();

    }

    //----------Class for Broadcast receiver-------------------//
    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"received broadcast");
            ArrayList<DetectedActivity> updatedActivities =
                    intent.getParcelableArrayListExtra("com.app.android.sp.ACTIVITY_EXTRA");
            handleDetectedActivities(updatedActivities);
        }
    }

    //-----------------------private locationlistener class-------------------------//

    private void initializeLocationManager(int locinterval, int locdistance) {
        inprocess = true;
        l1taken = false;
        l2taken = false;
        l3taken = false;
        if(mLocationManager!=null){
            mLocationManager.removeUpdates(mLocationListeners[0]); //stop getting location updates
            mLocationManager.removeUpdates(mLocationListeners[1]);
        }
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
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

    private class LocationListener implements android.location.LocationListener {

        Location mLastLocation;

        public LocationListener(String provider)       //constructor
        {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            if(!l1taken) {
                userlat1 = location.getLatitude();
                userlon1 = location.getLongitude();
                l1taken = true;
            }
            if(l1taken && (!l2taken)){
                userlat2 = location.getLatitude();
                userlon2 = location.getLongitude();
                l2taken = true;
            }
            if(l1taken && l2taken && (!l3taken))
            {
                userlat3 = location.getLatitude();
                userlon3 = location.getLongitude();
                l3taken = true;
                inprocess = false;
                candlat = (userlat1+userlat2+userlat3)/3;
                candlon = (userlon1+userlon2+userlon3)/3;
                if(mLocationManager!=null){
                    mLocationManager.removeUpdates(mLocationListeners[0]); //stop getting location updates
                    mLocationManager.removeUpdates(mLocationListeners[1]);
                }
            }
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
