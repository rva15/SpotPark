package com.app.android.sp;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;

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
 * Created by ruturaj on 10/10/16.
 */
public class NavutilityFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener{
    //Necessary variable declarations
    static String UID="";
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
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    private static final String TAG = "Debugger ";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    MapView nMapView;

    //------------------------------Fragment Lifecycle Related Functions-------------------------//



    //onCreate of fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);


        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = new LocationRequest();            // Create location request
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS); //periodically update location
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS); //fastest update interval
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Bundle extras = getArguments();
        carlatitude = extras.getDouble("latitude");
        carlongitude = extras.getDouble("longitude");

    }

    @Override
    public void onStart(){
        mGoogleApiClient.connect();      //onStart of the activity, connect apiclient
        super.onStart();
    }

    @Override
    public void onStop() {
        i=0;                            //set counter for UpdateUI back to 0
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();  //disconnect apiclient on stop
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        //stopLocationUpdates();       //stop location updates when activity pauses as defined below
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
        View view = inflater.inflate(R.layout.fragment_navutility, container, false); //inflate the view
        nMapView = (MapView) view.findViewById(R.id.numap);
        nMapView.onCreate(savedInstanceState);
        nMapView.onResume();                                                      //get mapView and initialize it
        MapsInitializer.initialize(getActivity());
        nMapView.getMapAsync(this);

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
    }

    protected void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
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

    private void updateUI() {

        latitude = mCurrentLocation.getLatitude();       //get the latitude
        longitude = mCurrentLocation.getLongitude();     //get the longitude
        place = new LatLng(latitude, longitude);  //initiate LatLng object


        if(marker!=null){
            marker.remove();    //remove previous marker
        }

        marker = navigationmap.addMarker(new MarkerOptions().position(place).title("You're here")); //and set it at new location

        if(i==0) {
            navigationmap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 16)); //zoom on the location
            LatLng carlocation = new LatLng(carlatitude,carlongitude);
            navigationmap.addMarker(new MarkerOptions().position(carlocation).title("You're here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            drawroute(carlatitude,carlongitude);
        }
        i=i+1;

    }



    public void drawroute(double carlatitude,double carlongitude){
        LatLng origin = new LatLng(latitude, longitude);
        LatLng dest = new LatLng(carlatitude, carlongitude);
        navigationmap.addMarker(new MarkerOptions().position(dest).title("Car's here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        String url = getUrl(origin, dest);
        FetchUrl FetchUrl = new FetchUrl();
        FetchUrl.execute(url);

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
                lineOptions.width(10);
                lineOptions.color(android.graphics.Color.RED);

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
        String key = "AIzaSyDKQYvSAVhRH6s8WW-RmtJPAyLnbjA9t8I";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&key=";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters +key;


        return url;
    }


}

