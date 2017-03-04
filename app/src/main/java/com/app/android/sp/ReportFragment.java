package com.app.android.sp;
//All imports
import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.ByteArrayOutputStream;

/**
 * Created by ruturaj on 9/16/16.
 */
public class ReportFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener,View.OnClickListener{

    //variable declarations
    private GoogleMap reportmap;
    private GoogleApiClient mGoogleApiClient;
    private  double curlatitude;
    private  double curlongitude;
    private float zoom = 18;
    private static final String TAG = "Debugger ";
    private Location mCurrentLocation;
    private LatLng place;
    private String tid,ID;
    private static String UID="";
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private LocationRequest mLocationRequest;
    private int i=0;
    private CameraPosition position;
    private LatLng cameracenter;
    private static final String ARG_PAGE = "ARG_PAGE";
    private MapView sMapView;
    private ImageView rpin;
    private SupportPlaceAutocompleteFragment placeSelection;
    private Marker marker;
    private LinearLayout reportcenter;
    private ImageView satview,gridview;
    //------------------------------Fragment Lifecycle Related Functions-------------------------//

    public static ReportFragment newInstance(int page,String id) {
        UID = id;
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


        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
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
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();  //disconnect apiclient on stop
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected()) {
            stopLocationUpdates();       //stop location updates when activity pauses as defined below
        }
        if (null != sMapView){
            sMapView.onPause();}
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if(placeSelection!=null){
            FragmentTransaction ft = this.getFragmentManager().beginTransaction(); //need to remove to fix user bug
            ft.remove(placeSelection);
            ft.commitAllowingStateLoss(); //since this comes after save instance state
        }
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
        /*if (null != sMapView) {
            //sMapView.onSaveInstanceState(outState);
        }*/
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
        initializeComponents();
        NativeExpressAdView adView = (NativeExpressAdView)view.findViewById(R.id.repadView);
        AdRequest request = new AdRequest.Builder()
                .addTestDevice(getResources().getString(R.string.test_device_ID))
                .build();
        adView.loadAd(request);
        reportcenter = (LinearLayout) view.findViewById(R.id.reportrecenter);
        reportcenter.setOnClickListener(this);
        satview = (ImageView) view.findViewById(R.id.satview);
        satview.setOnClickListener(this);
        gridview = (ImageView) view.findViewById(R.id.gridview);
        gridview.setOnClickListener(this);
        return view;
    }

    //----------------------Location Related Functions----------------------------------//

    private void initializeComponents() {
        placeSelection = new SupportPlaceAutocompleteFragment();
        placeSelection.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                reportcenter.setVisibility(View.VISIBLE);
                reportmap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            }
            @Override
            public void onError(Status status) {

            }
        });
        FragmentManager mgr = getFragmentManager();
        FragmentTransaction transaction = mgr.beginTransaction();
        transaction.replace(R.id.placefragmentholder,placeSelection, "AutoSearchFragment");
        transaction.commit();
    }

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
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                //somehow display message to user
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permission necessary");
                alertBuilder.setMessage("Permission to access your location is necessary");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 63);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            }
            else{
                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        63);
            }
        }
        else {
            //if yes, request location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);         //location request requests updates periodically
        }
    }


    //----------------------Other Helper Functions----------------------------------//

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 63: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    if (Build.VERSION.SDK_INT >= 23 &&
                            ContextCompat.checkSelfPermission(this.getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, mLocationRequest, this);
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

        if(v.getId()==R.id.rpinimage) {
            final GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
                Bitmap bitmap;

                @Override
                public void onSnapshotReady(Bitmap snapshot) {
                    bitmap = snapshot;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();
                    showForm(data, cameracenter.latitude, cameracenter.longitude);
                }
            };
            rpin.setVisibility(View.GONE);
            marker.remove();
            position = reportmap.getCameraPosition();
            cameracenter = position.target;
            reportmap.addMarker(new MarkerOptions().position(cameracenter).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            reportmap.snapshot(callback);
            //get next activity when user clicks marker
        }
        if(v.getId()==R.id.reportrecenter){
            reportcenter.setVisibility(View.GONE);
            reportmap.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 16));
        }
        if(v.getId() == R.id.satview){
            satview.setVisibility(View.GONE);
            gridview.setVisibility(View.VISIBLE);
            reportmap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        if(v.getId() == R.id.gridview){
            gridview.setVisibility(View.GONE);
            satview.setVisibility(View.VISIBLE);
            reportmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

    }

    public void showForm(byte[] bytearray,double lat,double lon){

        HomeScreenActivity activity = (HomeScreenActivity) getActivity();
        activity.getReportForm(bytearray,lat,lon);
    }



    private void updateUI() {
        curlatitude = mCurrentLocation.getLatitude();       //get the latitude
        curlongitude = mCurrentLocation.getLongitude();     //get the longitude
        place = new LatLng(curlatitude, curlongitude);  //initiate LatLng object
        //first time this method is called, put a marker on user's location and zoom in on it
        if(i==0){
            reportmap.animateCamera(CameraUpdateFactory.newLatLngZoom(place, zoom));//zoom on the location
        }
        if(marker!=null){
            marker.remove();                            //remove previous marker
        }
        marker = reportmap.addMarker(new MarkerOptions().position(place).title("You're here").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mylocation)));
        i=i+1;
    }


}
