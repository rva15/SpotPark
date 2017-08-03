package com.application.android.sp;
//All imports
import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.string.yes;

/**
 * Created by ruturaj on 9/15/16.
 */
public class SearchFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener,GoogleMap.OnMarkerClickListener,View.OnClickListener,GoogleMap.OnMapClickListener,GoogleMap.OnCameraMoveStartedListener {




    //Variable Declaration

    //--Utility Variables
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private double latitude,longitude;
    private static String UID="";
    private static final String TAG = "Debugger ";
    private int i=0;
    private Timer t;
    private static final String ARG_PAGE = "ARG_PAGE";
    private SpotFinder finder;
    private SlidingUpPanelLayout mLayout;
    private SearchHelperDB helperDB;
    private TextView category,rate,heading,spotdescription,curkeys;
    private boolean isAutoMode=true,isComplaint=false,isUpvoted=false,isDownvoted=false,zoomalertgiven=false,isgridview=true;
    private String key="",latlngcode,uid,type="",arkey="";
    private LinearLayout recenter;
    private RelativeLayout navigate,route;
    private ParkWhizSpots parkWhizSpots;
    private ImageView startsearch,refreshspots;
    private double cameralatitude,cameralongitude;
    private ImageView ssatview,sgridview;
    private Calendar startcalendar, endcalendar;
    private TextView displaystart,displayend;
    private LinearLayout fromuntil1,fromuntil2;
    private boolean showCheckIns=true;
    private RelativeLayout feedback,book;
    private static boolean searchStarted;
    private String curLatLng;
    private String label;
    private ArrayList<String> array = new ArrayList<String>();



    //--Google Map and Firebase variables
    private GoogleMap searchmap;
    private GoogleApiClient ApiClient;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private LatLng place,currentmarker;
    private Marker marker;
    private DatabaseReference database;
    private MapView gMapView;
    private com.google.firebase.database.Query getcheckin,getreported;
    private SupportPlaceAutocompleteFragment placeSelection;


    //---------------------------------Fragment Lifecycle Functions---------------------------//

    public static SearchFragment newInstance(int page,String id,boolean searchstarted) {
        UID = id;
        searchStarted = searchstarted;
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);



        ApiClient = new GoogleApiClient.Builder(this.getActivity())   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        locationRequest = new LocationRequest();            // Create location request
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS); //periodically update location
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS); //fastest update interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        helperDB = new SearchHelperDB(SPApplication.getContext()); //initialize phone db

    }

    @Override
    public void onStart(){
        ApiClient.connect();      //onStart of the activity, connect apiclient
        super.onStart();
    }

    @Override
    public void onStop() {
        i=0;                            //set counter for UpdateUI back to 0
        if(ApiClient.isConnected()) {
            ApiClient.disconnect();         //disconnect apiclient on stop
        }
        if (t != null) {
            t.cancel();                 //cancel timer for SpotFinder
        }

        /*String LatLngCode = getLatLngCode(latitude,longitude);
        database = FirebaseDatabase.getInstance().getReference();
        String key = database.child("CheckInKeys/"+LatLngCode).push().getKey();  //delete entry from Searcher database
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Searchers/"+LatLngCode+"/"+UID, null);
        database.updateChildren(childUpdates);*/
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(ApiClient.isConnected()) {
            stopLocationUpdates();       //stop location updates when activity pauses as defined below
        }
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
    public void onDestroyView()
    {
        super.onDestroyView();
        if(placeSelection!=null){
            FragmentTransaction ft = this.getFragmentManager().beginTransaction();
            ft.remove(placeSelection);
            ft.commitAllowingStateLoss(); //since this comes after save instance state
        }
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
        return view;

    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        gMapView = (MapView) view.findViewById(R.id.smap);
        gMapView.onCreate(savedInstanceState);
        gMapView.onResume();                                                      //get mapView and initialize it
        MapsInitializer.initialize(getActivity());
        gMapView.getMapAsync(this);

        //Get UI elements and set listeners on them
        mLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        category = (TextView) view.findViewById(R.id.category);
        rate = (TextView) view.findViewById(R.id.rate);
        spotdescription = (TextView) view.findViewById(R.id.spotdescription);
        heading = (TextView) view.findViewById(R.id.heading);
        heading.setOnClickListener(this);
        navigate = (RelativeLayout) view.findViewById(R.id.navigate);
        navigate.setOnClickListener(this);
        route = (RelativeLayout) view.findViewById(R.id.route);
        route.setOnClickListener(this);
        startsearch = (ImageView) view.findViewById(R.id.startsearch);
        startsearch.setOnClickListener(this);
        gMapView.setOnClickListener(this);
        recenter = (LinearLayout) view.findViewById(R.id.recenter);
        recenter.setVisibility(View.GONE);
        recenter.setOnClickListener(this);
        book = (RelativeLayout) view.findViewById(R.id.book);
        book.setOnClickListener(this);
        ssatview = (ImageView) view.findViewById(R.id.ssatview);
        ssatview.setOnClickListener(this);
        sgridview = (ImageView) view.findViewById(R.id.sgridview);
        sgridview.setOnClickListener(this);
        displaystart = (TextView) view.findViewById(R.id.displaystart);
        displayend = (TextView) view.findViewById(R.id.displayend);
        fromuntil1 = (LinearLayout) view.findViewById(R.id.fromuntil1);
        fromuntil2 = (LinearLayout) view.findViewById(R.id.fromuntil2);
        fromuntil1.setVisibility(View.GONE);
        fromuntil2.setVisibility(View.GONE);
        fromuntil2.setOnClickListener(this);
        feedback= (RelativeLayout) view.findViewById(R.id.feedback);
        feedback.setOnClickListener(this);
        refreshspots = (ImageView) view.findViewById(R.id.refreshspots);
        refreshspots.setOnClickListener(this);
        curkeys = (TextView) view.findViewById(R.id.curkeys);

        initializeComponents();
        getKeyCount();

    }

    //-----------------------------Location Related Functions-------------------//

    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int x){
        Toast.makeText(this.getActivity(), "Connection suspended", Toast.LENGTH_SHORT); //notify user when connection is suspended
    }

    @Override
    public void onLocationChanged(Location location) {   //triggered after location change
        currentLocation = location;                      //stores current location
        updateUI();                                      //will update UI accordingly
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        searchmap = googleMap;                    //when GoogleMap is ready, put it into the existing map object
        searchmap.setOnMarkerClickListener(this); //set different listeners on it
        searchmap.setOnMapClickListener(this);
        searchmap.setOnCameraMoveStartedListener(this);
        searchmap.getUiSettings().setMyLocationButtonEnabled(true);
        if(searchStarted){
            startSearch();
        }
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                ApiClient, this);
    }

    protected void startLocationUpdates() {
        //you need to check first if you have permissions from user
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this.getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)){
                //somehow display message to user
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permission necessary");
                alertBuilder.setMessage("Permission to access your location is necessary");
                alertBuilder.setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 65);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            }
            else{
                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        65);
            }
        }
        else {
            //if yes, request location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    ApiClient, locationRequest, this);         //location request requests updates periodically
        }
    }

    //--------------------------------Other helper functions-------------------------//

    private void initializeComponents() {
        placeSelection = new SupportPlaceAutocompleteFragment();
        placeSelection.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                isAutoMode = false;
                searchmap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                recenter.setVisibility(View.VISIBLE);
                if(searchStarted){
                    resetParkWhiz(place.getLatLng().latitude,place.getLatLng().longitude);
                    resetSpotFinder(place.getLatLng().latitude,place.getLatLng().longitude);
                }
            }
            @Override
            public void onError(Status status) {

            }
        });
        FragmentManager mgr = getFragmentManager();
        FragmentTransaction transaction = mgr.beginTransaction();
        transaction.replace(R.id.searchplacefragmentholder,placeSelection, "AutoSearchFragment");
        transaction.commitAllowingStateLoss();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 65: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    if (Build.VERSION.SDK_INT >= 23 &&
                            ContextCompat.checkSelfPermission(this.getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                ApiClient, locationRequest, this);
                    }

                } else {

                    // permission denied,
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.route) {                  //draw a route on the searchmap
            if (currentmarker != null && place != null) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                Toast.makeText(getContext(),"Drawing route",Toast.LENGTH_SHORT).show();
                String url = getUrl(place, currentmarker);
                FetchUrl FetchUrl = new FetchUrl();
                FetchUrl.execute(url);
                return;
            }
        }

        if(v.getId()==R.id.fromuntil2){
            showSearchDialog();
        }

        if(v.getId()==R.id.refreshspots){
            if(searchStarted) {
                resetParkWhiz(cameralatitude, cameralongitude);
                resetSpotFinder(cameralatitude, cameralongitude);
                Toast.makeText(getContext(),"Refreshing spots",Toast.LENGTH_SHORT).show();
            }
        }
        if(v.getId()==R.id.navigate){    //send an intent to the Google Maps app
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            if(currentmarker!=null) {
                double lat = currentmarker.latitude;
                double lon = currentmarker.longitude;
                String uriBegin = "geo:" + lat + "," + lon;
                String query = lat + "," + lon + "(" + label + ")";
                String encodedQuery = Uri.encode(query);
                String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                if(!TextUtils.isEmpty(uriString)) {
                    Uri uri = Uri.parse(uriString);
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().getApplicationContext().startActivity(intent);
                }
            }
        }

        if(v.getId()==R.id.recenter){
            // animate camera back to user's location
            recenter.setVisibility(View.GONE);
            if(searchmap!=null && place!=null) {
                searchmap.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 15));
                if(searchStarted) {
                    resetParkWhiz(latitude, longitude);
                    resetSpotFinder(latitude, longitude);
                }
            }
            isAutoMode=true;
        }
        if(v.getId()==R.id.startsearch){
            //check number of keys
            HomeScreenActivity homeScreenActivity = (HomeScreenActivity)getActivity();
            homeScreenActivity.setStartSearch(true);
            reduceKey();
        }

        if(v.getId()==R.id.feedback){
            // Create an instance of the dialog fragment and show it
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            if(!type.equals("actrec")) {
                checkFeedbacks(key);
            }
            else{
                showFeedbackDialog();
            }
        }

        if(v.getId()==R.id.book){
            //send intent to web browser along with spot's url
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            Map PWSpotlinks = parkWhizSpots.getParkWhizlinks();
            String url = (String) PWSpotlinks.get(currentmarker);
            if(TextUtils.isEmpty(url)){
                Toast.makeText(getContext(),"Unable to perform action. Please refresh and try again.",Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }

        if(v.getId() == R.id.ssatview){
            ssatview.setVisibility(View.GONE);
            sgridview.setVisibility(View.VISIBLE);
            searchmap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            isgridview = false;
            updateUI();
        }
        if(v.getId() == R.id.sgridview){
            sgridview.setVisibility(View.GONE);
            ssatview.setVisibility(View.VISIBLE);
            searchmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            isgridview = true;
            updateUI();
        }
        if(v.getId() == R.id.heading){
            if(heading.getText().equals("What is Activity Prediction?")) {
                HomeScreenActivity homeScreenActivity = (HomeScreenActivity) this.getActivity();
                homeScreenActivity.getARGuide();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //fetch information from the dialog and call the checkIn function
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== 5){
            Bundle bundle = data.getExtras();
            if(!type.equals("actrec")) {
                Boolean yes = bundle.getBoolean("yes");
                if (yes) {
                    if (type.equals("reported")) {
                        upvoteReported();
                    } else {
                        upvoteCheckin();
                    }
                } else {
                    Boolean notav = bundle.getBoolean("notav");
                    Boolean nospace = bundle.getBoolean("nospace");
                    Boolean notfree = bundle.getBoolean("notfree");
                    registerComplaint(notav, nospace, notfree);           //register user's complaint
                    if (type.equals("reported")) {
                        downvoteReported();
                    }
                }
                if (type.equals("reported")) {
                    giveKeys(2);
                    Toast.makeText(getContext(), "You've earned 2 keys for this feedback!", Toast.LENGTH_SHORT).show();
                } else {
                    giveKeys(1);
                    Toast.makeText(getContext(), "You've earned 1 key for this feedback!", Toast.LENGTH_SHORT).show();
                }

            }
            else{
                Boolean aryes = bundle.getBoolean("aryes");
                Boolean arno  = bundle.getBoolean("arno");
                Log.d("debugger","reached the right place "+aryes);
                if(arno){
                    Log.d("debugger",getLatLngCode(currentmarker.latitude,currentmarker.longitude)+" "+arkey);
                    database = FirebaseDatabase.getInstance().getReference();
                    database.child("ARSpots").child(getLatLngCode(currentmarker.latitude,currentmarker.longitude)).child(arkey).child("millis").setValue(0);
                }

            }
        }
        if(requestCode==4){
            Bundle bundle = data.getExtras();  //set the range of dates to search in
            startcalendar = (Calendar)bundle.get("startcalendar");
            endcalendar = (Calendar)bundle.get("endcalendar");
            if(startcalendar.getTimeInMillis()>=endcalendar.getTimeInMillis()){
                showSearchDialog();
                Toast.makeText(getContext(),"Invalid range of dates!",Toast.LENGTH_LONG).show();
                return;
            }
            displaystart.setText((String)bundle.get("displaystart"));
            if(bundle.get("displaystart").equals("Now")){
                showCheckIns=true;
            }
            else{
                showCheckIns=false;
            }
            displayend.setText((String)bundle.get("displayend"));
            resetSpotFinder(searchmap.getCameraPosition().target.latitude,searchmap.getCameraPosition().target.longitude);
            resetParkWhiz(searchmap.getCameraPosition().target.latitude,searchmap.getCameraPosition().target.longitude);
        }
    }

    private void resetParkWhiz(double curlatitude,double curlongitude){
        if(parkWhizSpots!=null){
            parkWhizSpots.removeParkWhizspots();
        }
        parkWhizSpots = new ParkWhizSpots(curlatitude,curlongitude,startcalendar,endcalendar,searchmap,getContext()); //send a single call to ParkWhiz API
        parkWhizSpots.getParkWhizspots();

    }

    private void resetSpotFinder(double curlatitude,double curlongitude){
        if(finder!=null){
            finder.detachListeners();      //detach previous finder's listeners
        }

        finder = new SpotFinder(curlatitude,curlongitude, searchmap, UID,getContext(),startcalendar,endcalendar,showCheckIns); //declare the SpotFinder and pass it user's location and searchmap
        finder.addListener();   //call its addListener method
    }

    private void showSearchDialog(){
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new SearchTimeDialog();
        dialog.setTargetFragment(SearchFragment.this,4);       //set target fragment to this fragment
        dialog.show(this.getActivity().getSupportFragmentManager(),"Search fragment");
    }

    private void startSearch(){
        startsearch.setVisibility(View.GONE);
        fromuntil1.setVisibility(View.VISIBLE);
        fromuntil2.setVisibility(View.VISIBLE);
        refreshspots.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(),"Tap on the parking spot markers",Toast.LENGTH_SHORT).show();
        startcalendar = Calendar.getInstance();
        Calendar tmp = (Calendar) startcalendar.clone();
        tmp.add(Calendar.HOUR_OF_DAY, 3);
        endcalendar = tmp;
        searchStarted=true;
        if(searchmap!=null) {
            resetSpotFinder(searchmap.getCameraPosition().target.latitude, searchmap.getCameraPosition().target.longitude);
            resetParkWhiz(searchmap.getCameraPosition().target.latitude, searchmap.getCameraPosition().target.longitude);
        }

    }

    private void registerComplaint(Boolean notav,Boolean nospace,Boolean notfree){
        if(type.equals("reported")){
            if(nospace||notfree){
                //reduce verifications on the reported spot
                if(nospace){
                    //register complaint against user if this is not a parking place at all
                    updatecomplaints();
                }
                isComplaint = true;
                String code = getLatLngCode(currentmarker.latitude,currentmarker.longitude);
                getreported = database.child("ReportedDetails").child(code).orderByKey().equalTo(key);
                getreported.addChildEventListener(listener5);
            }
        }
        else{
            if(notav){
                //delete the corresponding checkin
                latlngcode = getLatLngCode((int)Math.round(currentmarker.latitude*100),(int)Math.round(currentmarker.longitude*100));
                getcheckin = database.child("CheckInKeys").child(latlngcode).orderByKey().equalTo(key);
                getcheckin.addChildEventListener(listener1);
            }
            if(nospace){
                //register complaint against user
                updatecomplaints();
            }
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        // clicking anywhere on the map makes the slider layout collapse
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public void onCameraMoveStarted(int reason){
        // display the recenter button
        if(reason==REASON_GESTURE) {
            if(!zoomalertgiven){
                Toast.makeText(getContext(),"Use search bar or refresh button",Toast.LENGTH_SHORT).show();
                zoomalertgiven = true;
            }
            recenter.setVisibility(View.VISIBLE);
            isAutoMode = false;
        }
    }

    //define the ChildEventListener for deleting corresponding checkin
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String mycode = getLatLngCode((int)Math.round(place.latitude*100),(int)Math.round(place.longitude*100));
            if(mycode.equals(latlngcode)){
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/CheckInKeys/"+latlngcode+"/"+key, null);
                database.updateChildren(childUpdates);
            }
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



    // helper functions for updating stuff

    private void giveKeys(final int numkeys){
        database.child("UserInformation").child(UID).child("numberofkeys").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer keys = dataSnapshot.getValue(Integer.class);
                dataSnapshot.getRef().setValue(keys+numkeys);
                HomeScreenActivity homeScreenActivity = (HomeScreenActivity) getActivity();
                homeScreenActivity.refreshMainAdapter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showFeedbackDialog(){
        DialogFragment dialog = new ComplainDialog();
        Bundle args = new Bundle();
        args.putString("type", type);
        dialog.setArguments(args);
        dialog.setTargetFragment(SearchFragment.this, 5);       //set target fragment to this fragment
        dialog.show(this.getActivity().getSupportFragmentManager(), "Search fragment");
    }

    private void checkFeedbacks(String key){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("Feedbacks").child(key).child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //user has already given a feedback
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
                    builder.setMessage("You have already given a feedback for this spot.");
                    builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }
                    });
                    android.support.v7.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else{
                    showFeedbackDialog();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getKeyCount(){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("UserInformation").child(UID).child("numberofkeys").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer keys = dataSnapshot.getValue(Integer.class);
                curkeys.setText(Integer.toString(keys));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void reduceKey(){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("UserInformation").child(UID).child("numberofkeys").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer keys = dataSnapshot.getValue(Integer.class);
                if(keys>0){
                    dataSnapshot.getRef().setValue(keys-1);
                    curkeys.setText(Integer.toString(keys-1));
                    HomeScreenActivity homeScreenActivity = (HomeScreenActivity) getActivity();
                    homeScreenActivity.refreshMainAdapter();
                    if(keys>4) {
                        startSearch();
                    }
                    else{
                        lowAlert(keys-1);
                    }
                }
                else{
                    curkeys.setText("0");
                    oopsAlert();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void lowAlert(int keys){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        builder.setTitle("Low Key Count Alert");
        builder.setMessage("You have "+Integer.toString(keys)+" keys left. Check-in when you park to earn keys");
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                startSearch();
            }
        });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void oopsAlert(){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        builder.setMessage("Oops! Looks like you don't have any keys left. Check-in when you park to earn 2 keys.");
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void upvoteCheckin(){
        // delete the corresponding checkin and update this user's points
        latlngcode = getLatLngCode((int)Math.round(currentmarker.latitude*100),(int)Math.round(currentmarker.longitude*100));
        getcheckin = database.child("CheckInKeys").child(latlngcode).orderByKey().equalTo(key);
        getcheckin.addChildEventListener(listener1);
        updatecinfeed();
        resetSpotFinder(searchmap.getCameraPosition().target.latitude,searchmap.getCameraPosition().target.longitude);
        return;
    }


    private void upvoteReported(){
        String code = getLatLngCode(currentmarker.latitude,currentmarker.longitude);
        getreported = database.child("ReportedDetails").child(code).orderByKey().equalTo(key);
        getreported.addChildEventListener(listener5);
        updaterepfeed();
        giveUpvote(key);
        resetSpotFinder(searchmap.getCameraPosition().target.latitude,searchmap.getCameraPosition().target.longitude);
        return;
    }

    private void downvoteReported(){
        updaterepfeed();
        database = FirebaseDatabase.getInstance().getReference();
        String tempkey = database.child("Feedbacks/"+key).push().getKey();  //add new entry to searcher database
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Feedbacks/"+key+"/"+UID, -1);
        database.updateChildren(childUpdates);
        resetSpotFinder(searchmap.getCameraPosition().target.latitude,searchmap.getCameraPosition().target.longitude);
    }


    private void giveUpvote(String key){
        database = FirebaseDatabase.getInstance().getReference();
        String tempkey = database.child("Feedbacks/"+key).push().getKey();  //add new entry to searcher database
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Feedbacks/"+key+"/"+UID, 1);
        database.updateChildren(childUpdates);

    }

    private void removeUpvote(String key){
        database = FirebaseDatabase.getInstance().getReference();
        String tempkey = database.child("Feedbacks/"+key).push().getKey();  //add new entry to searcher database
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Feedbacks/"+key+"/"+UID, null);
        database.updateChildren(childUpdates);
        resetSpotFinder(searchmap.getCameraPosition().target.latitude,searchmap.getCameraPosition().target.longitude);
    }


    private void changeVerification(String uid){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("ReportedTimes").child(uid).child(key).child("verification").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long verification = (long) dataSnapshot.getValue();
                if(isComplaint) {
                    verification = verification - 1;
                    dataSnapshot.getRef().setValue(verification);
                }
                else{
                    verification = verification + 1;
                    dataSnapshot.getRef().setValue(verification);
                }
                isComplaint=false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updatecinfeed(){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("UserInformation").child(UID).child("checkinfeed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long cinfeed = (long) dataSnapshot.getValue();
                cinfeed = cinfeed + 1;
                dataSnapshot.getRef().setValue(cinfeed);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updatecomplaints(){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("UserInformation").child(UID).child("complaints").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long complaints = (long) dataSnapshot.getValue();
                complaints = complaints + 1;
                dataSnapshot.getRef().setValue(complaints);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updaterepfeed(){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("UserInformation").child(UID).child("reportfeed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long repfeed = (long) dataSnapshot.getValue();
                repfeed = repfeed + 1;
                dataSnapshot.getRef().setValue(repfeed);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    ChildEventListener listener5 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            uid = dataSnapshot.getValue(String.class);
            changeVerification(uid);
            getreported.removeEventListener(listener5);
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
        latitude = currentLocation.getLatitude();       //get the current latitude
        longitude = currentLocation.getLongitude();     //get the current longitude
        placeSelection.setHint("Search for parking at?");
        //sendLocation();                                 //update this user's location in searcher database
        place = new LatLng(latitude, longitude);        //initiate LatLng object
        if(marker!=null){
            marker.remove();                            //remove previous marker
        }
        if(isgridview) {
            marker = searchmap.addMarker(new MarkerOptions().position(place).title("You're here").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mylocation))); //and set it at new location
        }
        else{
            marker = searchmap.addMarker(new MarkerOptions().position(place).title("You're here").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mylocationwhite))); //and set it at new location
        }
        if(isAutoMode) {
            searchmap.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 15)); //zoom on the location
        }

        cameralatitude = searchmap.getCameraPosition().target.latitude;
        cameralongitude = searchmap.getCameraPosition().target.longitude;

        // call the spot finder algorithm
        if(searchStarted) {
            if (i == 0) {
                curLatLng = getLatLngCode(latitude,longitude);
                resetParkWhiz(cameralatitude,cameralongitude);
                t = new Timer();
                t.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        resetSpotFinder(cameralatitude,cameralongitude);
                    }

                }, 0, 45000);                    //new spotfinder declared every 45 secs
            }
            i = i + 1;
            String tmpLatLng = getLatLngCode(latitude,longitude);
            if(!(tmpLatLng.equals(curLatLng))){
                resetParkWhiz(latitude,longitude);
                curLatLng = tmpLatLng;
            }
        }

    }

    // Adding this user's location to searcher's list
    private void sendLocation(){
        latitude = currentLocation.getLatitude();       //get the latitude
        longitude = currentLocation.getLongitude();     //get the longitude
        String LatLngCode = getLatLngCode(latitude,longitude);
        database = FirebaseDatabase.getInstance().getReference();
        String key = database.child("Searchers/"+LatLngCode).push().getKey();  //add new entry to searcher database


        com.application.android.sp.Searcher searcher = new com.application.android.sp.Searcher(latitude,longitude);
        Map<String, Object> searcherMap = searcher.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Searchers/"+LatLngCode+"/"+UID, searcherMap);
        database.updateChildren(childUpdates);
    }

    //getting the latlng code
    private String getLatLngCode(double lat, double lon){

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

        currentmarker = marker.getPosition(); //get the marker's position and set it to currentmarker
        if(finder!=null) {
            Map Keys = finder.getKeys();        //get these maps from the spotfinder object
            Map Times = finder.getTimes();
            Map Cats = finder.getCats();
            Map Desc = finder.getDesc();
            Map DriveTime = finder.getDriveTimes();
            Map UserFeedbacks = finder.getUserFeedbacks();
            Map ARTypes = finder.getARTypes();
            Map ARKeys = finder.getArKeys();
            Map PWSpotnames = parkWhizSpots.getPWSpotnames();
            heading.setPaintFlags(heading.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
            if (Cats.get(currentmarker) != null) {    //the marker belongs to a reported spot
                type = "reported";
                book.setVisibility(View.GONE);
                feedback.setVisibility(View.VISIBLE);
                key = (String) Keys.get(currentmarker);
                if(UserFeedbacks.get(key)==null){
                    //user did not give a feedback about this spot
                    isUpvoted=false;
                    isDownvoted=false;
                }
                else{
                    //user has given feedback
                    if((int)UserFeedbacks.get(key)==1){
                        //positive feedback
                        isUpvoted=true;
                        isDownvoted=false;
                    }
                    if((int)UserFeedbacks.get(key)==-1){
                        //negative feedback
                        isUpvoted=false;
                        isDownvoted=true;
                    }
                }
                heading.setText("Reporter's Description");
                heading.setTextColor(ContextCompat.getColor(getContext(),R.color.dimgrey));
                if ((boolean) Cats.get(currentmarker) == true) {
                    category.setText("Verified user-reported spot");
                    //float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    //rate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, pixels);
                    rate.setVisibility(View.GONE);
                    spotdescription.setVisibility(View.VISIBLE);
                    spotdescription.setText((String) Desc.get(currentmarker));
                    label = (String) Desc.get(currentmarker);
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    return true;
                } else {
                    category.setText("Unverified user-reported spot");
                    //float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    //rate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, pixels);
                    rate.setVisibility(View.GONE);
                    spotdescription.setVisibility(View.VISIBLE);
                    spotdescription.setText((String) Desc.get(currentmarker));
                    label = (String) Desc.get(currentmarker);
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    return true;
                }
            }
            if (Keys.get(currentmarker) != null) {         //marker belongs to a checkin spot
                book.setVisibility(View.GONE);
                feedback.setVisibility(View.VISIBLE);
                type = "checkin";
                label = "SpotPark user Check-In";
                heading.setText("Cost per Hour");
                key = (String) Keys.get(currentmarker);
                int time = (int) Times.get(currentmarker);
                if (key != null) {
                    Cursor res = helperDB.getInfo(key);
                    if (res.getCount() <= 0) {
                        res.close();
                    }
                    res.moveToFirst();
                    int dollars = Integer.parseInt(res.getString(res.getColumnIndex("Dollars")));   //get dollars and cents from phone db
                    int cents = Integer.parseInt(res.getString(res.getColumnIndex("Cents")));
                    category.setText("Vacating in : "+Integer.toString((int)Times.get(currentmarker))+"mins  |  Travel time : " + Integer.toString((int) DriveTime.get(key)) + "mins");
                    rate.setText("$ " + Integer.toString(dollars) + "." + Integer.toString(cents));
                    spotdescription.setVisibility(View.GONE);
                    rate.setVisibility(View.VISIBLE);
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
            }
            if (PWSpotnames.get(currentmarker) != null) {  //marker belongs to ParkWhiz spot
                book.setVisibility(View.VISIBLE);
                feedback.setVisibility(View.GONE);
                type = "parkwhiz";
                heading.setText("Parking Lot Name");
                category.setText("Paid Parking lot (from ParkWhiz)");
                rate.setVisibility(View.GONE);
                spotdescription.setVisibility(View.VISIBLE);
                spotdescription.setText((String) PWSpotnames.get(currentmarker));
                label = (String) PWSpotnames.get(currentmarker);
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
            if (ARTypes.get(currentmarker) != null) { //marker belongs to AR Spot
                book.setVisibility(View.GONE);
                feedback.setVisibility(View.VISIBLE);
                type = "actrec";
                label = "Activity Prediction Spot";
                heading.setText("What is Activity Prediction?");
                arkey = (String)ARKeys.get(currentmarker);
                heading.setTextColor(ContextCompat.getColor(getContext(),R.color.black));
                if((Double)ARTypes.get(currentmarker)==0.0){
                    category.setText("Activity Prediction Algorithm");
                    rate.setVisibility(View.GONE);
                    spotdescription.setText("A user taking out his car in ~ 2min");
                }
                if((Double)ARTypes.get(currentmarker)==1.0){
                    category.setText("Activity Prediction Algorithm");
                    rate.setVisibility(View.GONE);
                    spotdescription.setText("A user just took his car out");
                }
                rate.setVisibility(View.GONE);
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

            }
        }
        return false;
    }

    private void panelListener(){

        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            // During the transition of expand and collapse onPanelSlide function will be called.
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            // This method will be call after slide up layout
            @Override
            public void onPanelExpanded(View panel) {

            }

            // This method will be call after slide down layout.
            @Override
            public void onPanelCollapsed(View panel) {

            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });
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
            if(result!=null) {
                parserTask.execute(result);
            }

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
                jObject = new JSONObject(jsonData[0]);;
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
                lineOptions.color(ContextCompat.getColor(getContext(),R.color.statusbarcolor));

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                searchmap.addPolyline(lineOptions);
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
        String key = getResources().getString(R.string.googleAPI_serverkey);

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&key=";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters +key;


        return url;
    }




}
