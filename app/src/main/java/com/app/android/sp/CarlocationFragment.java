package com.app.android.sp;
//All imports
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.android.gms.location.LocationListener;
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

/**
 * Created by ruturaj on 9/16/16.
 */
public class CarlocationFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener,View.OnClickListener,GoogleMap.OnCameraMoveStartedListener{

    //Variable declarations
    //--Utility Variables
    private static String UID="";
    private double latitude,longitude,carlatitude,carlongitude;
    private LatLng place;
    private int i = 0,couthours,coutmins;
    private DatabaseReference database;
    private Boolean isAutoMode=true;
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String TAG = "Debugger ";
    private LinearLayout recenter;
    private String time="",latlngcode,checkinkey;
    private TextView timeview;
    private Button informbutton;
    private com.google.firebase.database.Query getcheckin,getminstoleave;
    private FrameLayout othersknow;
    //--Google API variables
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private Location mCurrentLocation;
    private Marker marker;
    private GoogleMap navigationmap;
    private MapView nMapView;
    private Bitmap carMarker;

    //------------------------------Fragment Lifecycle Related Functions-------------------------//

    public static CarlocationFragment newInstance(int page, String id) {
        UID = id;
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        CarlocationFragment fragment = new CarlocationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //onCreate of fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);


        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = new LocationRequest();                      // Create location request
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS); //periodically update location
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS); //fastest update interval
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //get the small sized car marker
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.car,null);
        Bitmap b=bitmapdraw.getBitmap();
        carMarker = Bitmap.createScaledBitmap(b, dpToPx(32), dpToPx(32), false);

    }

    @Override
    public void onStart(){
        mGoogleApiClient.connect();          //onStart of the activity, connect apiclient
        super.onStart();
    }

    @Override
    public void onStop() {
        i=0;                                //set counter for UpdateUI back to 0
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();  //disconnect apiclient on stop
        }
        if(getminstoleave!=null) {
            getminstoleave.removeEventListener(listener2);
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();       //stop location updates when activity pauses as defined below
        if (null != nMapView){
            nMapView.onPause();}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != nMapView)
            nMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != nMapView)
            nMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (null != nMapView)
            nMapView.onLowMemory();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {    //start location updates once apiclient is connected
            startLocationUpdates();
        }
        if (null != nMapView){
            nMapView.onResume();}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carlocation, container, false); //inflate the view

        nMapView = (MapView) view.findViewById(R.id.nmap);
        nMapView.onCreate(savedInstanceState);
        nMapView.onResume();                                                      //get mapView and initialize it
        MapsInitializer.initialize(getActivity());
        nMapView.getMapAsync(this);

        //get the recenter button and set visibility to gone
        recenter = (LinearLayout) view.findViewById(R.id.recenter);
        recenter.setVisibility(View.GONE);
        recenter.setOnClickListener(this);

        //Get the TextView and inform buttons
        othersknow = (FrameLayout)view.findViewById(R.id.othersknow);
        timeview = (TextView) view.findViewById(R.id.couttime);
        informbutton = (Button) view.findViewById(R.id.informbutton);
        informbutton.setOnClickListener(this);


        // Initialize the Ad unit
        NativeExpressAdView adView = (NativeExpressAdView)view.findViewById(R.id.carlocadView);
        AdRequest request = new AdRequest.Builder()
                .addTestDevice(getResources().getString(R.string.test_device_ID))
                .build();
        adView.loadAd(request);


        return view;
    }

    //----------------------Location Related Functions----------------------------------//

    @Override
    public void onConnected(Bundle connectionHint) {
        //call this function
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int x){
        //notify user of lost connection
        Toast.makeText(this.getActivity(), "Connection suspended", Toast.LENGTH_SHORT); //notify user when connection is suspended
    }

    @Override
    public void onLocationChanged(Location location) {   //triggered after location change
        mCurrentLocation = location;                     //stores current location
        updateUI();                                      //will update UI accordingly
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        navigationmap = googleMap;                    //when GoogleMap is ready, put it into the existing map object
        navigationmap.getUiSettings().setMyLocationButtonEnabled(true);
        navigationmap.setOnCameraMoveStartedListener(this);
    }

    protected void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onCameraMoveStarted(int reason){
        if(reason==REASON_GESTURE) {
            recenter.setVisibility(View.VISIBLE);
            isAutoMode = false;
        }
    }

    protected void startLocationUpdates() {

        //you need to check first if you have permissions from user
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this.getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){

            return;
        }
        //if yes, request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);         //location request requests updates periodically
    }

    //-----------------------------Helper functions----------------------------//

    private void informaction(){
        informbutton.setVisibility(View.GONE);
        Intent servIntent = new Intent(this.getActivity(), DirectionService.class);     //start the DirectionService
        servIntent.putExtra("started_from", "navigation");
        this.getActivity().startService(servIntent);
        HomeScreenActivity homeScreenActivity = (HomeScreenActivity) this.getActivity();
        homeScreenActivity.refreshMainAdapter();
    }

    private void confirminform() {   //show a confirmation dialog before deleting the spot
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("This will notify other users that a legal parking spot is being vacated soon. You will be rewarded 2 more keys.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                informaction();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void updateUI() {

        latitude = mCurrentLocation.getLatitude();       //get the latitude
        longitude = mCurrentLocation.getLongitude();     //get the longitude
        place = new LatLng(latitude, longitude);  //initiate LatLng object


        if(marker!=null){
            marker.remove();    //remove previous marker
        }

        marker = navigationmap.addMarker(new MarkerOptions().position(place).title("You're here").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mylocation))); //and set it at new location

        if(i==0) {
            navigationmap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 16)); //zoom on the location
            getcarLocation(UID);
        }
        if(isAutoMode){
            navigationmap.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 16)); //smoothly animate camera
        }
        i=i+1;

    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void getcarLocation(String id){
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
        getcheckin = database.child("CheckInUsers").orderByKey().equalTo(id);
        getcheckin.addChildEventListener(listener1);
    }

    private void drawroute(double carlatitude,double carlongitude){
        LatLng origin = new LatLng(latitude, longitude);
        LatLng dest = new LatLng(carlatitude, carlongitude);
        navigationmap.addMarker(new MarkerOptions().position(dest).title("Car's here").icon(BitmapDescriptorFactory.fromBitmap(carMarker)));
        String url = getUrl(origin, dest);
        FetchUrl FetchUrl = new FetchUrl();
        FetchUrl.execute(url);

    }

    private void checkInformed(){
        database = FirebaseDatabase.getInstance().getReference();
        getminstoleave = database.child("CheckInKeys").child(latlngcode).orderByKey().equalTo(checkinkey);
        getminstoleave.addChildEventListener(listener2);
    }


    //define the ChildEventListener
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            CheckInUser user = dataSnapshot.getValue(CheckInUser.class);
            carlatitude = user.getcarlatitude();
            carlongitude = user.getcarlongitude();
            couthours = user.getcouthours();
            coutmins  = user.getcoutmins();
            latlngcode = user.getlatlngcode();
            checkinkey = user.getkey();
            // pass information to home screen to make edit checkin active
            HomeScreenActivity homeScreenActivity = (HomeScreenActivity) getActivity();
            homeScreenActivity.setCheckinkey(checkinkey);
            homeScreenActivity.setLatitude(carlatitude);
            homeScreenActivity.setLongitude(carlongitude);
            homeScreenActivity.setLatlngcode(latlngcode);
            homeScreenActivity.setCoutTime(couthours,coutmins);
            timeview.setText(gettime(couthours,coutmins));
            drawroute(carlatitude,carlongitude);
            checkInformed();
            getcheckin.removeEventListener(listener1);
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
            CheckInDetails checkInDetails = dataSnapshot.getValue(CheckInDetails.class);
            Integer minstoleave =  checkInDetails.getminstoleave();
            HomeScreenActivity homeScreenActivity = (HomeScreenActivity) getActivity();
            homeScreenActivity.setRate(checkInDetails.getdollars(),checkInDetails.getcents()); //set default rate for edit checkins
            if(minstoleave!=10031 && minstoleave!=20041){
                informbutton.setVisibility(View.GONE);
            }
            else if(minstoleave==20041){

                othersknow.setVisibility(View.GONE);
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            CheckInDetails checkInDetails = dataSnapshot.getValue(CheckInDetails.class);
            Integer minstoleave =  checkInDetails.getminstoleave();
            if(minstoleave!=10031){
                informbutton.setVisibility(View.GONE);
            }

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
    };


    private String gettime(int hours,int mins){
        if(hours==123 || mins==123){
            time = "---";
            return time;
        }
        if(hours>12){
            if(mins <10) {
                time = Integer.toString(hours - 12) + ":0" + Integer.toString(mins) + " pm";
            }
            else{
                time = Integer.toString(hours - 12) + ":" + Integer.toString(mins) + " pm";
            }
        }
        if(hours<12){
            if(mins<10) {
                time = Integer.toString(hours) + ":0" + Integer.toString(mins) + " am";
            }
            else{
                time = Integer.toString(hours) + ":" + Integer.toString(mins) + " am";
            }
        }
        if(hours==12){
            if(mins <10) {
                time = Integer.toString(hours) + ":0" + Integer.toString(mins) + " pm";
            }
            else{
                time = Integer.toString(hours) + ":" + Integer.toString(mins) + " pm";
            }
        }
        return time;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.informbutton) {
            confirminform();
        }
        if(v.getId()==R.id.recenter){
            recenter.setVisibility(View.GONE);
            navigationmap.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 16));
            isAutoMode=true;
        }
    }




    //--------------------Method that communicates with Directions API----------------------------//


    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
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
            br.close();

        } catch (Exception e) {
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
                DataParser parser = new DataParser();

                // Starts parsing data
                routes = parser.parse(jObject);

            } catch (Exception e) {
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
                lineOptions.width(7);
                lineOptions.color(Color.BLUE);

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                navigationmap.addPolyline(lineOptions);
            }
            else {
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
        String key = getResources().getString(R.string.googleAPI_directionskey);

        //Mode of transport
        String mode = "&mode=walking";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + mode + "&key=";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters +key;


        return url;
    }


}
