package com.example.android.sp;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ruturaj on 9/15/16.
 */
public class SearchFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {
    //declare required variables
    private GoogleMap searchmap;
    GoogleApiClient ApiClient;
    LocationRequest locationRequest;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    Location currentLocation;
    double latitude,longitude;
    LatLng place;
    Marker marker;
    float zoom = 16;
    private int mPage;
    static String UID="";
    private static final String TAG = "Debugger ";
    int i=0;
    Timer t;
    public static final String ARG_PAGE = "ARG_PAGE";
    public DatabaseReference database;
    SpotFinder finder;
    MapView gMapView;

    //---------------------------------Fragment Lifecycle Functions---------------------------//

    public static SearchFragment newInstance(int page,String id) {
        UID = id;
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //mPage = getArguments().getInt(ARG_PAGE);

        ApiClient = new GoogleApiClient.Builder(this.getActivity())   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        locationRequest = new LocationRequest();            // Create location request
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS); //periodically update location
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS); //fastest update interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onStart(){
        ApiClient.connect();      //onStart of the activity, connect apiclient
        super.onStart();
    }

    @Override
    public void onStop() {
        i=0;                            //set counter for UpdateUI back to 0
        ApiClient.disconnect();         //disconnect apiclient on stop
        t.cancel();                     //cancel timer for SpotFinder
        String LatLngCode = getLatLngCode(latitude,longitude);
        database = FirebaseDatabase.getInstance().getReference();
        String key = database.child("CheckInKeys/"+LatLngCode).push().getKey(); //delete entry from Searcher database
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Searchers/"+LatLngCode+"/"+UID, null);
        database.updateChildren(childUpdates);
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();       //stop location updates when activity pauses as defined below
        if (null != gMapView){
            gMapView.onPause();}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != gMapView)
            gMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != gMapView)
            gMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (null != gMapView)
            gMapView.onLowMemory();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (ApiClient.isConnected()) {    //start location updates once apiclient is connected
            startLocationUpdates();
        }
        if (null != gMapView){
            gMapView.onResume();}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false); //inflate the view
        gMapView = (MapView) view.findViewById(R.id.smap);
        gMapView.onCreate(savedInstanceState);
        gMapView.onResume();                                                      //get mapView and initialize it
        MapsInitializer.initialize(getActivity());
        gMapView.getMapAsync(this);

        return view;
    }

    //-----------------------------Location Related Functions-------------------//

    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int x){
        //notify user of lost connection
        Toast.makeText(this.getActivity(), "Connection suspended", Toast.LENGTH_SHORT); //notify user when connection is suspended
    }

    @Override
    public void onLocationChanged(Location location) {   //triggered after location change
        currentLocation = location;                     //stores current location
        updateUI();                                      //will update UI accordingly
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        searchmap = googleMap;                    //when GoogleMap is ready, put it into the existing map object
        searchmap.getUiSettings().setMyLocationButtonEnabled(true);
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                ApiClient, this);
    }

    protected void startLocationUpdates() {

        //you need to check first if you have permissions from user
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this.getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){

            return;
        }
        //if yes, request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(
                ApiClient, locationRequest, this);         //location request requests updates periodically
    }

    //--------------------------------Other helper functions-------------------------//

    private void updateUI() {

        latitude = currentLocation.getLatitude();       //get the latitude
        longitude = currentLocation.getLongitude();     //get the longitude
        sendLocation();
        place = new LatLng(latitude, longitude);  //initiate LatLng object
        if(marker!=null){
            marker.remove();                      //remove previous marker
        }
        marker = searchmap.addMarker(new MarkerOptions().position(place).title("You're here"));  //set marker at current location
        if(i==0){
            searchmap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 16)); //zoom on the location
            t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    finder = new SpotFinder(latitude,longitude,searchmap,UID); //declare the SpotFinder and pass it user's location and searchmap
                    finder.addListener();   //call its addListener method
                    Log.d(TAG,"timer called");
                }

            },0, 60000);   //new spotfinder declared every minute
        }
        i=i+1;

    }

    public void sendLocation(){
        latitude = currentLocation.getLatitude();       //get the latitude
        longitude = currentLocation.getLongitude();     //get the longitude
        String LatLngCode = getLatLngCode(latitude,longitude);
        database = FirebaseDatabase.getInstance().getReference();
        String key = database.child("Searchers/"+LatLngCode).push().getKey();  //add new entry to searcher database


        com.example.android.sp.Searcher searcher = new com.example.android.sp.Searcher(latitude,longitude);
        Map<String, Object> searcherMap = searcher.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Searchers/"+LatLngCode+"/"+UID, searcherMap);
        database.updateChildren(childUpdates);
        Log.d(TAG,"reached sendLocation");
    }

    public String getLatLngCode(double lat, double lon){

        lat = lat*100;     //get the centi latitudes and centi longitudes
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

    /*public void found(android.view.View v){
        String code = getLatLngCode(latitude,longitude);       //delete nearest checkin when user says found parking
        finder.deletenearest(latitude,longitude,code);

    }*/




}
