package com.app.android.sp;

import android.app.AlarmManager;
import android.app.Notification;
import android.os.Handler;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static android.R.attr.data;
import static com.app.android.sp.SPApplication.getContext;
import static com.app.android.sp.SearchHelperDB.key;

/**
 * Created by ruturaj on 6/30/17.
 */

public class ARLocService extends android.app.Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private LocationManager mLocationManager = null;
    public GoogleApiClient mApiClient;
    private double userlat1=0, userlon1=0,userlat2=0,userlon2=0,candlat=0,candlon=0,curlat=0,curlon=0;
    private boolean forPlaces=false;
    private String TAG = "ActivityRecognition",UID="",latlngcode,key,gplacename="";
    protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    private boolean T1=false,T2=false,T3=false,trackAct=false,trackUser=false,activeSpot=false,notesent=false,stactive=false;
    private boolean l1taken=false,l2taken=false,type0updated=false,trackactHP=false,tracklocHP=false,sleepMode=false;
    private long t1,t2,t3,diff1,diff2,t4,t5;
    private double drivingc,footc,runningc,stillc,tiltingc,walkingc,unknownc,curdist=0,activemillis=0;
    private DatabaseReference database;
    private double gplacelat=0,gplacelng=0,minplacedis=30,userplacelat=0,userplacelon=0,notesentlat=0,notesentlon=0;
    private Places closestPlace;
    private int tempcounter=0;
    private ArrayList<Places> placesArrayList = new ArrayList<Places>();
    private ArrayList<Places> vetoesArrayList = new ArrayList<Places>();
    private Handler handler;

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
        UID = readUID();
        if(TextUtils.isEmpty(UID)){
            stopSelf();
        }
        checkST();
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
        requestActivityUpdates(60000);
        handler = new Handler();
        handler.post(runnableCode);
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

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            Log.d("Handlers", "Called on main thread");
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if((hour>0) && (hour<7)){
                if(!sleepMode) {
                    removeActivityUpdates();
                    if (mLocationManager != null) {
                        mLocationManager.removeUpdates(mLocationListeners[0]); //stop getting location updates
                        mLocationManager.removeUpdates(mLocationListeners[1]);
                    }
                    trackactHP = false;
                    tracklocHP = false;
                    forPlaces = false;
                    T1 = false;
                    T2 = false;
                    T3 = false;
                    trackAct = false;
                    trackUser = false;
                    activeSpot = false;
                    notesent = false;
                    stactive = false;
                    l1taken = false;
                    l2taken = false;
                    type0updated = false;
                    sleepMode = true;
                    candlon = 0.0;
                    candlat = 0.0;
                }
            }
            else{
                if(sleepMode){
                    requestActivityUpdates(60000);
                    trackactHP = false;
                    sleepMode = false;
                }
            }
            handler.postDelayed(this, 60000);
        }
    };

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
        Log.d(TAG,"activity detection " +trackactHP);
        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    if(activity.getConfidence()>50) {
                        if(!trackactHP){
                            removeActivityUpdates();
                            requestActivityUpdates(8000);
                            trackactHP = true;
                        }
                        triggerOn();
                        l1taken = false;
                        l2taken = false;
                        drivingc = activity.getConfidence();
                        Log.d("ActivityRecogition", "In Vehicle: " + activity.getConfidence());
                    }
                    if(stactive) {
                        if (activity.getConfidence() < 30) {
                            if (trackAct) {
                                Log.d(TAG,"initial st condition");
                                forPlaces = true;
                                initializeLocationManager(2000, 0);
                            }
                        }
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
                        if(activeSpot && (curdist<0.1)){
                            if(!tracklocHP){
                                Log.d(TAG,"switching to locHP");
                                initializeLocationManager(2000,0);
                                tracklocHP = true;
                            }
                        }
                        footc = activity.getConfidence();
                        Log.d("ActivityRecogition", "On Foot: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.RUNNING: {
                    if(activity.getConfidence()>50) {
                        if(activeSpot && (curdist<0.1)){
                            if(!tracklocHP){
                                Log.d(TAG,"switching to locHP");
                                initializeLocationManager(2000,0);
                                tracklocHP = true;
                            }
                        }
                        runningc = activity.getConfidence();
                        Log.d("ActivityRecogition", "Running: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.STILL: {
                    if(activity.getConfidence()>50) {
                        if(activeSpot && (curdist>0.08))
                        if(tracklocHP) {
                            initializeLocationManager(20000, 10);
                            tracklocHP = false;
                        }
                    }
                    stillc = activity.getConfidence();
                    Log.d("ActivityRecogition", "Still: " + activity.getConfidence());
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
                        if(activeSpot && (curdist<0.1)){
                            if(!tracklocHP){
                                Log.d(TAG,"switching to locHP");
                                initializeLocationManager(2000,0);
                                tracklocHP = true;
                            }
                        }
                        walkingc = activity.getConfidence();
                        Log.d("ActivityRecogition", "Walking: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    if(activity.getConfidence()>50) {
                        if(activeSpot && (curdist<0.1)){
                            if(!tracklocHP){
                                Log.d(TAG,"switching to locHP");
                                initializeLocationManager(2000,0);
                                tracklocHP = true;
                            }
                        }
                        unknownc = activity.getConfidence();
                        Log.d("ActivityRecogition", "Unknown: " + activity.getConfidence());
                    }
                    break;
                }
            }
        }

        if(trackAct) {
            if (((footc > 50) || (runningc > 50) || (stillc > 50) || (tiltingc > 50) || (walkingc > 50)) && (drivingc==0)){
                if(!tracklocHP){
                    initializeLocationManager(2000,0);   //declare location manager
                    tracklocHP = true;
                }
                t4 = System.currentTimeMillis();
                t5 = (t4-t3)/1000;
                if(t5>180){
                    trackUser = true;
                    T1 = T2 = T3 =false;
                    trackAct = false;
                    scheduleNotification(getARTestNotification("Candidate "+Double.toString(candlat)+" "+Double.toString(candlon)), 1000, 411019);//notify user immediately
                    if(tracklocHP) {
                        initializeLocationManager(20000, 10);
                        tracklocHP = false;
                    }
                    if(trackactHP) {
                        removeActivityUpdates();
                        requestActivityUpdates(60000);
                        trackactHP = false;
                    }
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
                if(trackactHP) {
                    removeActivityUpdates();
                    requestActivityUpdates(60000);
                    trackactHP = false;
                }
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
                scheduleNotification(getARTestNotification("You're driving!!"), 1000, 599019);//notify user immediately
                trackAct = true;
                trackUser = false;
                type0updated = false;
                if(!trackactHP) {
                    removeActivityUpdates();
                    requestActivityUpdates(8000);
                    trackactHP = true;
                }
                if(mLocationManager!=null){
                    mLocationManager.removeUpdates(mLocationListeners[0]); //stop getting location updates
                    mLocationManager.removeUpdates(mLocationListeners[1]);
                    tracklocHP = false;
                }
                if(activeSpot){
                    Log.d(TAG,"spot is active");
                    double timediff = (t3-activemillis)/1000;
                    Log.d(TAG,"time diff active "+timediff);
                    if(timediff<120){
                        Log.d(TAG,"timediff is less than 120");
                        scheduleNotification(getARTestNotification("Certainly left a spot!!"), 1000, 359019);//notify user immediately
                        updateType();
                    }
                }
                activeSpot = false;
            }
            else{
                if(trackactHP) {
                    removeActivityUpdates();
                    requestActivityUpdates(60000);
                    trackactHP = false;
                }
                T1 = false;
                T2 = false;
            }
            Log.d(TAG,"t12 diff2 "+diff2);
            return;

        }
        if(T1 && T2 && T3){
            if(mLocationManager!=null){
                mLocationManager.removeUpdates(mLocationListeners[0]); //stop getting location updates
                mLocationManager.removeUpdates(mLocationListeners[1]);
                tracklocHP = false;
            }
            t3 = System.currentTimeMillis();
        }
    }

    // ----------- Functions to calculate distance to the place in kms ------------------//
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

    // --------------------------------------------------------------------------//

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


    private Notification getARTestNotification(String message) {

        //open the app on tapping the notification
        Intent openapp = new Intent(getContext(), HomeScreenActivity.class);
        openapp.addCategory("artestalert");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openapp, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.tab_background_unselected));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle("SpotPark");
        builder.setContentText(message);
        builder.setAutoCancel(true);
        builder.setSound(Uri.parse("android.resource://" + getContext().getPackageName() + "/" +R.raw.expiry)); //custom ringtone


        return builder.build();

    }

    private void checkST(){
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        database.child("UserInformation").child(UID).child("singletouch").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                { //check if singletouch child exists in database
                    stactive = dataSnapshot.getValue(Boolean.class);
                }
                else{ //create a single touch branch and make it active
                    dataSnapshot.getRef().setValue(true);
                    stactive = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Check for existing active spot
    private void checkActive(){
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        database.child("ARUsers").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ARUsers arUsers = dataSnapshot.getValue(ARUsers.class);
                    String userkey = arUsers.getkey();
                    String usercode = arUsers.getlatlngcode();
                    database.child("ARSpots").child(usercode).child(userkey).setValue(null);
                    database.child("ARUsers").child(UID).setValue(null);
                    addSpot();
                }
                else{
                    addSpot();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Add active spot
    private void addSpot(){
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        key = database.child("ARSpots").child(latlngcode).push().getKey();

        ARUsers arUsers = new ARUsers(latlngcode,key);
        Map<String, Object> arUsersMap = arUsers.toMap(); //call its toMap method
        ARSpots arSpots = new ARSpots(candlat,candlon,activemillis,0);
        Map<String, Object> arSpotsMap = arSpots.toMap(); //call its toMap method

        // Make the entries
        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/ARSpots/"+latlngcode+"/"+key, arSpotsMap);
        childUpdates.put("/ARUsers/"+UID,arUsersMap);

        database.updateChildren(childUpdates);
    }

    private void updateType(){
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        double curmillis = System.currentTimeMillis();
        database.child("ARSpots").child(latlngcode).child(key).child("millis").setValue(curmillis);
        database.child("ARSpots").child(latlngcode).child(key).child("type").setValue(1);
    }

    //function to generate the LatLngCode
    private String getLatLngCode(double lat, double lon){

        lat = lat*100;                     //get the centi latitudes and centi longitudes
        lon = lon*100;
        int lat1 = (int)Math.round(lat);   //round them off
        int lon1 = (int)Math.round(lon);
        String lons = Integer.toString(lon1);   //convert them to strings
        String lats = Integer.toString(lat1);

        if(lon1>=0){                //concatenate those strings to form the code
            lons = "+"+lons;
        }
        if(lat1>=0){
            lats = "+"+lats;
        }
        return (lons+lats);
    }

    //function to read the UID
    private String readUID(){
        String line="";
        StringBuffer buffer= new StringBuffer();
        BufferedReader input = null;
        File file = null;
        try {
            file = new File(getCacheDir(), "UIDFile"); // Pass getFilesDir() and "MyFile" to read file
            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            stopSelf();
            e.printStackTrace();
        }
        if(buffer.length()!=0) {
            return buffer.toString();
        }
        else{
            return "";
        }
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
        if(!forPlaces) {
            l1taken = false;
            l2taken = false;
        }
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
            Log.d(TAG,"on location changed "+tracklocHP);
            if(notesentlon!=0 && notesentlon!=0){
                double notesentdist = distance(notesentlat,notesentlon,location.getLatitude(),location.getLongitude());
                if(notesentdist>1){
                    notesentlon =0;
                    notesentlat=0;
                    notesent = false;
                }
            }
            if(!forPlaces) {
                if (!trackUser) {
                    if (!l1taken) {
                        Log.d(TAG, "l1taken " + location.getLatitude() + " " + location.getLongitude());
                        userlat1 = location.getLatitude();
                        userlon1 = location.getLongitude();
                        l1taken = true;
                    }
                    if (l1taken && (!l2taken)) {
                        Log.d(TAG, "l2taken " + location.getLatitude() + " " + location.getLongitude());
                        userlat2 = location.getLatitude();
                        userlon2 = location.getLongitude();
                        l2taken = true;
                        candlat = (userlat1);
                        candlon = (userlon1);
                        if (mLocationManager != null) {
                            mLocationManager.removeUpdates(mLocationListeners[0]); //stop getting location updates
                            mLocationManager.removeUpdates(mLocationListeners[1]);
                        }
                    }
                } else {
                    curlat = location.getLatitude();
                    curlon = location.getLongitude();
                    curdist = distance(curlat, curlon, candlat, candlon);
                    Log.d(TAG,"current distance "+curdist + " "+activeSpot);
                    if (!activeSpot) {
                        if (curdist > 0.05) {
                            if (trackactHP) {
                                removeActivityUpdates();
                                requestActivityUpdates(60000);
                                trackactHP = false;
                            }
                            activeSpot = true;
                        }
                    }
                    if (activeSpot) {
                        if (curdist < 0.02) {
                            if (!trackactHP) {
                                removeActivityUpdates();
                                requestActivityUpdates(8000);
                                trackactHP = true;
                            }
                            activemillis = System.currentTimeMillis();
                            if (!type0updated) {
                                scheduleNotification(getARTestNotification("Car lene aaye ho aap?"), 1000, 211019);//notify user immediately
                                latlngcode = getLatLngCode(candlat, candlon);
                                type0updated = true;
                                checkActive();
                            }
                        } else {
                            if (trackactHP) {
                                removeActivityUpdates();
                                requestActivityUpdates(60000);
                                trackactHP = false;
                            }
                        }
                    }
                }
            }
            else{
                forPlaces = false;
                userplacelat = location.getLatitude();
                userplacelon = location.getLongitude();
                String url = getUrl(userplacelat, userplacelon); //get url for google places query
                Object[] DataTransfer = new Object[1];
                DataTransfer[0] = url;  //put this url in an Object[]
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                getNearbyPlacesData.execute(DataTransfer);
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


    //------------Single Touch Algorithm---------------------//
    private String getUrl(double latitude, double longitude){
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+Double.toString(latitude)+","+Double.toString(longitude)+"&opennow&rankby=distance&keyword=AMC|Regal&key="+getResources().getString(R.string.googleAPI_serverkey);
        return url;
    }

    private class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

        String googlePlacesData="";
        String url="";

        @Override
        protected String doInBackground(Object... params) {
            try {
                url = (String) params[0];
                DownloadUrl downloadUrl = new DownloadUrl();
                googlePlacesData = downloadUrl.readUrl(url);
            } catch (Exception e) {
            }
            return googlePlacesData;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result!=null) {
                List<HashMap<String, String>> nearbyPlacesList;
                PlacesDataParser dataParser = new PlacesDataParser();
                nearbyPlacesList = dataParser.parse(result);   //pass the result of the query to a dataparser
                if(nearbyPlacesList.isEmpty()) {
                    return;
                }
                else{
                    CheckVicinity(nearbyPlacesList);               //pass the parsed result to check vicinity function
                }
            }
            else{
                return;
            }
        }

        private void CheckVicinity(List<HashMap<String, String>> nearbyPlacesList) {
            if(nearbyPlacesList.isEmpty()){
                return;
            }
            HashMap<String, String> googlePlace = nearbyPlacesList.get(0);
            gplacelat = Double.parseDouble(googlePlace.get("lat"));  //get the lat and lon of the nearest place
            gplacelng = Double.parseDouble(googlePlace.get("lng"));
            gplacename = googlePlace.get("place_name");              //get name of the place
            Log.d(TAG,"google place name "+gplacename);
            getPlaceCount();   //get custom places
        }
    }

    private void getPlaceCount(){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("STPlaces").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    int count = (int) dataSnapshot.getChildrenCount(); //get the number of custom places
                    getPlaces(count); //get these places
                }
                else{
                    getVetoCount();  //if there are no custom places, proceed to get veto places
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPlaces(final int count){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("STPlaces").child(UID).orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                tempcounter = tempcounter+1;
                Places places = dataSnapshot.getValue(Places.class);
                placesArrayList.add(places);
                if(tempcounter==count){
                    tempcounter=0;
                    getVetoCount(); //finished getting places, now get vetoes
                    database.child("STPlaces").child(UID).orderByKey().removeEventListener(this);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getVetoCount(){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("STVetoes").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    int count = (int) dataSnapshot.getChildrenCount();
                    getVetoes(count);
                }
                else{
                    getClosestPlace(); //if no veto places go ahead
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getVetoes(final int count){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("STVetoes").child(UID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                tempcounter = tempcounter + 1;
                Places places = dataSnapshot.getValue(Places.class);
                vetoesArrayList.add(places);
                if (tempcounter == count) {
                    tempcounter = 0;
                    getClosestPlace();
                    database.child("STVetoes").child(UID).removeEventListener(this);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getClosestPlace(){
        for(int i=0;i<placesArrayList.size();i++){
            double tempdis = distance(placesArrayList.get(i).getplacelat(),placesArrayList.get(i).getplacelon(),userplacelat,userplacelon);
            if(tempdis<minplacedis){

                closestPlace = placesArrayList.get(i); //this is the closest custom place
                minplacedis = tempdis;
            }
        }
        initSTAlgo();
    }

    private void initSTAlgo(){
        double distancegp = 30;
        double distancecp = 30;
        double distance   = 30;
        if(closestPlace!=null) { //there is at least one custom place
            distancegp = distance(gplacelat,gplacelng,userplacelat,userplacelon); //distance of closest google place
            distancecp = distance(closestPlace.getplacelat(), closestPlace.getplacelon(), userplacelat, userplacelon); //distance of closest custom place

            if(distancegp<distancecp){
                distance = distancegp;
                closestPlace = new Places(gplacename,gplacelat,gplacelng); //google place is closest
            }
            else{
                distance = distancecp;  //custom place is closest
            }
            Log.d(TAG,"st algo "+closestPlace.getplacename());

        }
        else{ //there are no custom places
            distancegp = distance(gplacelat,gplacelng,userplacelat,userplacelon); //calculate distance from the user's location
            distance = distancegp;
            closestPlace = new Places(gplacename,gplacelat,gplacelng); //google place is closest
            Log.d(TAG,"st algo "+closestPlace.getplacename());
        }


        if(passVeto(closestPlace)) { //execute function only if the closest place is not vetoed
            Log.d(TAG,"st distance "+distance);
            if (distance < 0.2) {   //if distance is less than 300m, you are in the zone
                if(!notesent) {
                    notesentlat = closestPlace.getplacelat();
                    notesentlon = closestPlace.getplacelon();
                    notesent = true;
                    scheduleNotification(getCheckinNotification(), 1000, 13);  // if not notify user immediately
                }
            }
        }

    }

    private boolean passVeto(Places place){
        for(int i=0;i<vetoesArrayList.size();i++){
            double d = distance(vetoesArrayList.get(i).getplacelat(),vetoesArrayList.get(i).getplacelon(),place.getplacelat(),place.getplacelon());
            if(d<0.1){
                return false; //place does not pass veto if it is less than 100m from veto place
            }
        }
        return true;
    }

    // --------------------------------------------------------------------------//


    private Notification getCheckinNotification() {
        //open the app on tapping the notification
        Intent openapp = new Intent(getContext(), HomeScreenActivity.class);
        openapp.putExtra("startedfrom","notification");
        openapp.putExtra("userid",UID);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openapp, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.tab_background_unselected));
        builder.setContentTitle("SpotPark");
        builder.setContentText("Check-in to remember your car location");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setSound(Uri.parse("android.resource://" + getContext().getPackageName() + "/" +R.raw.expiry)); //custom ringtone

        return builder.build();
    }

    // Get the data from the url
    public class DownloadUrl {

        public String readUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                br.close();

            } catch (Exception e) {
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }




}
