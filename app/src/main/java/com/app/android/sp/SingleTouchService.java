package com.app.android.sp;
// All imports
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.app.android.sp.R.drawable.navigate;
import static com.app.android.sp.SPApplication.getContext;

/**
 * Created by ruturaj on 4/2/17.
 */

public class SingleTouchService extends android.app.Service {

    //Variable Declarations
    private LocationManager mLocationManager = null;
    private static int LOCATION_INTERVAL = 15000;  //default request updates every 15secs
    private static float LOCATION_DISTANCE = 100;  //set default distance interval to 100m
    private String TAG = "debugger",UID="",gplacename;
    private double userlat, userlon,gplacelat,gplacelng;
    private ArrayList<Places> placesArrayList = new ArrayList<Places>();
    private ArrayList<Places> vetoesArrayList = new ArrayList<Places>();
    private DatabaseReference database;
    private double minplacedis=30;
    private int tempcounter=0,zonelimit=0;
    private Places closestPlace;
    private boolean inzone=false;


    //---------------------------Service LifeCycle Methods------------------------//

    //onCreate method
    @Override
    public void onCreate(){

        initializeLocationManager(); //declare location manager
        try {

            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return; //return if you dont have permission
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
        public SingleTouchService getService() {
            return SingleTouchService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        UID = readUID();
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

            userlat = location.getLatitude();        //get current location
            userlon = location.getLongitude();
            saveLocation(Double.toString(userlat),Double.toString(userlon)); //save the current location in phone cache

            if(!inzone) { //do these activities only if you are out of a zone
                String url = getUrl(userlat, userlon); //get url for google places query
                Object[] DataTransfer = new Object[1];
                DataTransfer[0] = url;  //put this url in an Object[]
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                getNearbyPlacesData.execute(DataTransfer);  //execute single touch logic
            }
            else{ //you are in the zone, so keep checking distances
                initSTAlgo();
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
                List<HashMap<String, String>> nearbyPlacesList = null;
                PlacesDataParser dataParser = new PlacesDataParser();
                nearbyPlacesList = dataParser.parse(result);   //pass the result of the query to a dataparser
                CheckVicinity(nearbyPlacesList);               //pass the parsed result to check vicinity function
            }
            else{
                return;
            }
        }

        private void CheckVicinity(List<HashMap<String, String>> nearbyPlacesList) {
            HashMap<String, String> googlePlace = nearbyPlacesList.get(0);
            gplacelat = Double.parseDouble(googlePlace.get("lat"));  //get the lat and lon of the nearest place
            gplacelng = Double.parseDouble(googlePlace.get("lng"));
            gplacename = googlePlace.get("place_name");              //get name of the place
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
            double tempdis = distance(placesArrayList.get(i).getplacelat(),placesArrayList.get(i).getplacelon(),userlat,userlon);
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
            distancegp = distance(gplacelat,gplacelng,userlat,userlon); //distance of closest google place
            distancecp = distance(closestPlace.getplacelat(), closestPlace.getplacelon(), userlat, userlon); //distance of closest custom place

            if(distancegp<distancecp){
                distance = distancegp;
                closestPlace = new Places(gplacename,gplacelat,gplacelng); //google place is closest
            }
            else{
                distance = distancecp;  //custom place is closest
            }

        }
        else{ //there are no custom places
            distancegp = distance(gplacelat,gplacelng,userlat,userlon); //calculate distance from the user's location
            distance = distancegp;
            closestPlace = new Places(gplacename,gplacelat,gplacelng); //google place is closest
        }


        if(passVeto(closestPlace)) { //execute function only if the closest place is not vetoed
            if (distance < 0.3) {   //if distance is less than 300m, you are in the zone

                if (readCSStatus().equals("0") || readCSStatus().equals("")) { // if notification is not already sent
                    scheduleNotification(getCheckinNotification(), 1000, 13);  // if not notify user immediately
                }
                inzone = true;            //you are in zone
                LOCATION_DISTANCE = 0;    //location distance is 0
                LOCATION_INTERVAL = 3000; //location interval to 3secs
                saveCSStatus("1");        //and store in phone memory that we have already notified user
            } else if ((distance > 0.3) && (distance < 0.6)) { //in this range, user might enter zone anytime soon
                inzone = false;
                zonelimit = zonelimit+1;
                if(zonelimit>30){ //user has been in near zone more than 30 times
                    zonelimit = 0;
                    LOCATION_DISTANCE = 300;    //now user has to move 300m to next location update
                    LOCATION_INTERVAL = 30000;
                }
                else { //user is below near zone limit, check location more frequently
                    LOCATION_DISTANCE = (int) ((distance - 0.3) * 1000);  //get the distance to the inner circle and set that as location distance
                    LOCATION_INTERVAL = 15000; //location interval is 15secs
                }
            } else {  //user far away from nearest interesting place
                inzone = false;
                LOCATION_DISTANCE = (int) (distance * 1000 * 0.5);   //set this to half of the distance to nearest place
                LOCATION_INTERVAL = 15000;
                if (readCSStatus().equals("1")) {  //destroy already given notification
                    NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
                    manager.cancel(13);
                }
                saveCSStatus("0");
            }
        }
        else{
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

    //function that constructs google places api url
    private String getUrl(double latitude, double longitude){
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+Double.toString(latitude)+","+Double.toString(longitude)+"&rankby=distance&keyword=AMC|Regal|Cinemark|walmart|costco|Stadium&key="+getResources().getString(R.string.googleAPI_serverkey);
        return url;
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


    private Notification getCheckinNotification() {
        //open the app on tapping the notification
        Intent openapp = new Intent(getContext(), HomeScreenActivity.class);
        openapp.putExtra("startedfrom","notification");
        openapp.putExtra("userid",UID);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openapp, PendingIntent.FLAG_CANCEL_CURRENT);

        //checkin at current location on tapping "checkin"
        Intent checkin = new Intent(this,CheckinService.class); //start checkin service
        checkin.putExtra("action",true);
        checkin.addCategory("checking in");
        PendingIntent pIntent = PendingIntent.getService(this, 0, checkin,PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.cinnotif, "Checkin", pIntent).build();

        //cancel and add this place to vetoes
        Intent neverhere = new Intent(this, CheckinService.class);
        neverhere.putExtra("action",false);
        neverhere.putExtra("vplacename",closestPlace.getplacename());
        neverhere.putExtra("vplacelat",closestPlace.getplacelat());
        neverhere.putExtra("vplacelon",closestPlace.getplacelon());
        neverhere.addCategory("never here");
        PendingIntent btPendingIntent = PendingIntent.getService(this, 0, neverhere,PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action cancel = new NotificationCompat.Action.Builder(R.drawable.cancelnotif, "Never here", btPendingIntent).build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.tab_background_unselected));
        builder.setContentTitle("SpotPark");
        builder.setContentText("Checkin to remember your car location");
        builder.setContentIntent(pendingIntent);
        builder.addAction(accept);
        builder.addAction(cancel);
        builder.setAutoCancel(true);
        builder.setSound(Uri.parse("android.resource://" + getContext().getPackageName() + "/" +R.raw.sintouch)); //custom ringtone


        return builder.build();

    }

    private void scheduleNotification(Notification notification, int delay,int unique) { //schedules the checkin notification

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, unique);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, unique, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    //-----------Functions to save and read from phone internal storage -------- //
    private void saveCSStatus(String sent){
        File file;
        FileOutputStream outputStream;
        try {
            file = new File(getCacheDir(), "CSStatus");
            outputStream = new FileOutputStream(file);
            outputStream.write(sent.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveLocation(String lat, String lon){
        File file1;
        FileOutputStream outputStream1;
        try {
            file1 = new File(getCacheDir(), "curLat");
            outputStream1 = new FileOutputStream(file1);
            outputStream1.write(lat.getBytes());
            outputStream1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file2;
        FileOutputStream outputStream2;
        try {
            file2 = new File(getCacheDir(), "curLon");
            outputStream2 = new FileOutputStream(file2);
            outputStream2.write(lon.getBytes());
            outputStream2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readCSStatus(){
        String line="";
        StringBuffer buffer= new StringBuffer();
        BufferedReader input = null;
        File file = null;
        try {
            file = new File(getCacheDir(), "CSStatus"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return buffer.toString();
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

        return buffer.toString();
    }

    //--------------------------------------------------------------------------------//


}
