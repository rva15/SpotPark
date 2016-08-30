package com.example.android.sp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.example.android.sp.NotificationPublisher;

public class NavigationActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, LocationListener {
    String UID="";
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    Location mCurrentLocation;
    double latitude,longitude,carlatitude,carlongitude;
    LatLng place;
    int i = 0;
    Marker marker;
    GoogleMap navigationmap;
    private DatabaseReference database;
    private static final String TAG = "Debugger ";
    String latlngcode="",key="";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        manager.cancel(1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Intent navigateintent = getIntent();
        UID     = navigateintent.getStringExtra("user_id");

        SupportMapFragment mapFragment =       //Load the fragment with the google map
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.navigationmap);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = new LocationRequest();            // Create location request
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS); //periodically update location
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart(){
        mGoogleApiClient.connect();      //onStart of the activity, connect apiclient
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();  //disconnect apiclient on stop
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();       //stop location updates when activity pauses as defined below
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {    //start location updates once apiclient is connected
            startLocationUpdates();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //call this function
        startLocationUpdates();

    }


    protected void startLocationUpdates() {

        //you need to check first if you have permissions from user
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            //ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //if yes, request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);         //location request requests updates periodically

    }

    @Override
    public void onLocationChanged(Location location) {   //triggered after location change
        mCurrentLocation = location;                     //stores current location
        updateUI();                                      //will update UI accordingly
    }

    private void updateUI() {

        latitude = mCurrentLocation.getLatitude();       //get the latitude
        longitude = mCurrentLocation.getLongitude();     //get the longitude
        place = new LatLng(latitude, longitude);  //initiate LatLng object
        //when function is called first time, set a marker at the users location

        if(marker!=null){
            marker.remove();
        }

        marker = navigationmap.addMarker(new MarkerOptions().position(place).title("You're here"));

        if(i==0) {
            navigationmap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 16)); //zoom on the location
            getcarLocation(UID);
        }
        i=i+1;



    }

    @Override
    public void onConnectionSuspended(int x){
        //notify user of lost connection
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT);  //notify user when connection is suspended
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        navigationmap = googleMap;               //when GoogleMap is ready, put it into the existing map object
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        java.io.InputStream iStream = null;
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
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(android.graphics.Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                navigationmap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        //Api key
        String key = "AIzaSyDKQYvSAVhRH6s8WW-RmtJPAyLnbjA9t8I";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&key=";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters +key;


        return url;
    }

    public void getcarLocation(String id){
        Log.d(TAG,"user id is :"+id);
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
        com.google.firebase.database.Query getcheckin = database.child("CheckInUsers").orderByKey().equalTo(id);
        getcheckin.addChildEventListener(listener1);

    }

    //define the ChildEventListener
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG,"detected something");
            CheckInUser user = dataSnapshot.getValue(CheckInUser.class);
            latlngcode = user.getlatlngcode();
            key = user.getkey();

            com.google.firebase.database.Query getlocation = database.child("CheckInKeys").child(latlngcode).orderByKey().equalTo(key);
            getlocation.addChildEventListener(listener2);

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {                 //currently all these functions have been left empty

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    ChildEventListener listener2 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            CheckInDetails details = dataSnapshot.getValue(CheckInDetails.class);
            carlatitude = details.getlatitude();
            carlongitude = details.getlongitude();
            drawroute(carlatitude,carlongitude);
            Log.d(TAG,"detected location "+Double.toString(carlatitude));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {                 //currently all these functions have been left empty

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void drawroute(double carlatitude,double carlongitude){
        LatLng origin = new LatLng(latitude, longitude);
        LatLng dest = new LatLng(carlatitude, carlongitude);
        navigationmap.addMarker(new MarkerOptions().position(dest).title("Car's here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        String url = getUrl(origin, dest);
        FetchUrl FetchUrl = new FetchUrl();
        FetchUrl.execute(url);

    }

}
