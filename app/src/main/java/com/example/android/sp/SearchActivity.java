package com.example.android.sp;

//All the required imports
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;
import com.example.android.sp.SpotFinder;
import org.w3c.dom.Comment;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {
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
    String UID="";
    private static final String TAG = "Debugger ";
    int i=0;
    ArrayList<String> array = new ArrayList<String>();


    //the onCreate Method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Intent intentfromoptions = getIntent();                       //Receive intent from Options Activity
        UID     = intentfromoptions.getStringExtra(OptionsActivity.ID); //Receive user's unique ID

        SupportMapFragment mapFragment =       //Load the fragment with the google map
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.searchmap);
        mapFragment.getMapAsync(this);

        ApiClient = new GoogleApiClient.Builder(this)   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = new LocationRequest();        //request location updates
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);//set them to periodically update
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart(){
        ApiClient.connect();      //onStart of the activity, connect apiclient
        super.onStart();
    }

    @Override
    protected void onStop() {
        ApiClient.disconnect();  //disconnect apiclient on stop
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();       //stop location updates when activity pauses as defined below
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                ApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ApiClient.isConnected()) {    //start location updates once apiclient is connected
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
                ApiClient, locationRequest, this);

    }

    @Override
    public void onLocationChanged(Location location) {   //triggered after location change
        currentLocation = location;                     //stores current location
        updateUI();                                      //will update UI accordingly
    }

    private void updateUI() {

        latitude = currentLocation.getLatitude();       //get the latitude
        longitude = currentLocation.getLongitude();     //get the longitude
        place = new LatLng(latitude, longitude);  //initiate LatLng object
        //when function is called first time, set a marker at the users location
        if(i==0){
            marker = searchmap.addMarker(new MarkerOptions().position(place).title("You're here").draggable(true));
            searchmap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 16)); //zoom on the location
        }
        i=i+1;

        SpotFinder finder = new SpotFinder(latitude,longitude,searchmap); //declare the SpotFinder and pass it user's location and searchmap
        finder.addListener();   //call its addListener method

    }



    @Override
    public void onConnectionSuspended(int x){
        //notify user of lost connection
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT);  //notify user when connection is suspended
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        searchmap = googleMap;               //when GoogleMap is ready, put it into the existing map object
    }

}
