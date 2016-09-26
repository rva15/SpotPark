package com.example.android.sp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ruturaj on 9/15/16.
 */
public class SearchFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener,GoogleMap.OnMarkerClickListener,View.OnClickListener {

    //This function SHOULD NOT be moved from this position
    @Override
    public void onSaveInstanceState(Bundle outState) {
        //This MUST be done before saving any of your own or your base class's variables
        final Bundle mapViewSaveState = new Bundle(outState);
        gMapView.onSaveInstanceState(mapViewSaveState);
        outState.putBundle("mapViewSaveState", mapViewSaveState);
        //Add any other variables here.
        super.onSaveInstanceState(outState);
    }


    //declare required variables
    private GoogleMap searchmap;
    GoogleApiClient ApiClient;
    LocationRequest locationRequest;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    Location currentLocation;
    double latitude,longitude;
    LatLng place,currentmarker;
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
    Button button;
    private SlidingUpPanelLayout mLayout;
    SearchHelperDB helperDB;
    TextView category,rate;
    boolean isReported = false;
    String key="",latlngcode;

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
        helperDB = new SearchHelperDB(SPApplication.getContext());

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
        if (t != null) {
            t.cancel();                     //cancel timer for SpotFinder
        }
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
        //stopLocationUpdates();       //stop location updates when activity pauses as defined below
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
        final Bundle mapViewSavedInstanceState = savedInstanceState != null ? savedInstanceState.getBundle("mapViewSaveState") : null;
        gMapView.onCreate(mapViewSavedInstanceState);
        gMapView.onResume();                                                      //get mapView and initialize it
        MapsInitializer.initialize(getActivity());
        gMapView.getMapAsync(this);
        mLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        category = (TextView) view.findViewById(R.id.category);
        rate = (TextView) view.findViewById(R.id.rate);
        Button button = (Button) view.findViewById(R.id.navigatebutton);
        button.setOnClickListener(this);
        Button button2 = (Button) view.findViewById(R.id.parkedbutton);
        button2.setOnClickListener(this);


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
        searchmap.setOnMarkerClickListener(this);
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

    @Override
    public void onClick(View v) {
        Log.d(TAG,"clicked it");
        if(v.getId()==R.id.navigatebutton) {
            if (currentmarker != null && place != null) {
                String url = getUrl(place, currentmarker);
                FetchUrl FetchUrl = new FetchUrl();
                FetchUrl.execute(url);
            }
        }
        if(v.getId()==R.id.parkedbutton){
            if(!isReported) {
                Log.d(TAG,"I parked checkin");
                latlngcode = getCode((int)Math.round(currentmarker.latitude*100),(int)Math.round(currentmarker.longitude*100));
                Log.d(TAG,"I parked checkin "+latlngcode + key);
                com.google.firebase.database.Query getcheckin = database.child("CheckInKeys").child(latlngcode).orderByKey().equalTo(key);
                getcheckin.addChildEventListener(listener1);
            }
        }
    }

    //define the ChildEventListener
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG,"got the I parked checkin");
            String mycode = getCode((int)Math.round(place.latitude*100),(int)Math.round(place.longitude*100));
            Log.d(TAG,"I parked checkin " +mycode);
            if(mycode.equals(latlngcode)){
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/CheckInKeys/"+latlngcode+"/"+key, null);
                database.updateChildren(childUpdates);
            }
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

    @Override
    public boolean onMarkerClick(Marker marker){
        LatLng l = marker.getPosition();
        currentmarker = l;
        Log.d(TAG,"marker lat "+Double.toString(l.latitude));
        Log.d(TAG,"marker lat "+Double.toString(l.longitude));
        Map Keys = finder.getKeys();
        Map Times = finder.getTimes();
        Map Cats = finder.getCats();
        if(Cats.get(l)!=null){
            isReported=true;
            if((boolean)Cats.get(l)==true){
                category.setText("Category : Verified free parking spot");
                rate.setText("$ 0.0");
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                return true;
            }
            else{
                category.setText("Category : Unverified free parking spot");
                rate.setText("$ 0.0");
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                return true;
            }
        }
        if(Keys.get(l)!=null) {
            isReported=false;
            key = (String) Keys.get(l);
            int time = (int) Times.get(l);
            if (key != null) {
                Log.d(TAG, "marker lat " + key);
                Cursor res = helperDB.getInfo(key);
                if (res.getCount() <= 0) {
                    res.close();
                }
                res.moveToFirst();
                int dollars = Integer.parseInt(res.getString(res.getColumnIndex("Dollars")));
                int cents = Integer.parseInt(res.getString(res.getColumnIndex("Cents")));
                Log.d(TAG, "marker lat " + Integer.toString(dollars));
                Log.d(TAG, "marker lat " + Integer.toString(cents));


                category.setText("Category : Possibly empty in " + Integer.toString(time) + " mins");
                rate.setText("$ " + Integer.toString(dollars) + "." + Integer.toString(cents));
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        }
        return false;
    }

    public void panelListener(){

        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            // During the transition of expand and collapse onPanelSlide function will be called.
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.e(TAG, "onPanelSlide, offset " + slideOffset);
            }

            // This method will be call after slide up layout
            @Override
            public void onPanelExpanded(View panel) {
                Log.e(TAG, "onPanelExpanded");

            }

            // This method will be call after slide down layout.
            @Override
            public void onPanelCollapsed(View panel) {
                Log.e(TAG, "onPanelCollapsed");

            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.e(TAG, "onPanelAnchored");
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.e(TAG, "onPanelHidden");
            }
        });
    }

    //the getCode method that returns LatLngCodes
    public String getCode(int i,int j){
        String lats = Integer.toString(i);
        String lons = Integer.toString(j);
        if(i>=0){
            lats = "+" + lats;
        }
        if(j>=0){
            lons = "+" + lons;
        }

        return (lons+lats);
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
                searchmap.addPolyline(lineOptions);
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




}
