package com.example.android.sp;

import android.content.Intent;
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
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ruturaj on 9/16/16.
 */
public class ReportFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener,View.OnClickListener{

    //Necessary global variable declarations
    private GoogleMap reportmap;
    private GoogleApiClient mGoogleApiClient;
    public  double curlatitude;
    public  double curlongitude;
    float zoom = 16;
    private static final String TAG = "Debugger ";
    Location mCurrentLocation;
    LatLng place;
    String tid,ID;
    static String UID="";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    LocationRequest mLocationRequest;
    int i=0;
    CameraPosition position;
    LatLng cameracenter;
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    MapView sMapView;
    ImageView rpin;
    //------------------------------Fragment Lifecycle Related Functions-------------------------//

    public static ReportFragment newInstance(int page,String id) {
        UID = id;
        Log.d(TAG,"report passed id : "+UID);
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ReportFragment fragment = new ReportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //onCreate of fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //mPage = getArguments().getInt(ARG_PAGE);

        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = new LocationRequest();            // Create location request
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS); //periodically update location
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS); //fastest update interval
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onStart(){
        mGoogleApiClient.connect();      //onStart of the activity, connect apiclient
        super.onStart();
    }

    @Override
    public void onStop() {
        i=0;                            //set counter for UpdateUI back to 0
        mGoogleApiClient.disconnect();  //disconnect apiclient on stop
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();       //stop location updates when activity pauses as defined below
        if (null != sMapView){
            sMapView.onPause();}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != sMapView)
            sMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != sMapView)
            sMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (null != sMapView)
            sMapView.onLowMemory();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {    //start location updates once apiclient is connected
            startLocationUpdates();
        }
        if (null != sMapView){
            sMapView.onResume();}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false); //inflate the view
        rpin = (ImageView)view.findViewById(R.id.rpinimage);                      //get the marker
        rpin.setOnClickListener(this);                                            //and set its onclicklistener
        sMapView = (MapView) view.findViewById(R.id.rmap);
        sMapView.onCreate(savedInstanceState);
        sMapView.onResume();                                                      //get mapView and initialize it
        MapsInitializer.initialize(getActivity());
        sMapView.getMapAsync(this);

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
        reportmap = googleMap;                    //when GoogleMap is ready, put it into the existing map object
        reportmap.getUiSettings().setMyLocationButtonEnabled(true);

    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
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


    //----------------------Other Helper Functions----------------------------------//

    @Override
    public void onClick(View v) {
        showForm();           //get next activity when user clicks marker
    }

    public void showForm(){
        position = reportmap.getCameraPosition();
        cameracenter = position.target;
        HomeScreenActivity activity = (HomeScreenActivity) getActivity();
        activity.test(UID,Double.toString(cameracenter.latitude),Double.toString(cameracenter.longitude));
        //Intent intent = new Intent(this.getActivity(), ReportForm.class); //send Intent
        //intent.putExtra("user_id",ID);
        //Log.d(TAG,"reportform "+UID);
        //intent.putExtra("lats",Double.toString(cameracenter.latitude));   //attach marker location to the intent
        //intent.putExtra("lons",Double.toString(cameracenter.longitude));
        //startActivity(intent);

    }



    private void updateUI() {
        Log.d(TAG, "yayy location updated !!!");
        curlatitude = mCurrentLocation.getLatitude();       //get the latitude
        curlongitude = mCurrentLocation.getLongitude();     //get the longitude
        place = new LatLng(curlatitude, curlongitude);  //initiate LatLng object
        //first time this method is called, put a marker on user's location and zoom in on it
        if(i==0){
            reportmap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, zoom));//zoom on the location
        }
        i=i+1;
    }


}
