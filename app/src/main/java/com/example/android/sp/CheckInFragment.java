package com.example.android.sp;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 9/13/16.
 */
public class CheckInFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener,View.OnClickListener{

    //variable declarations

    private GoogleMap map;
    private CheckInHelperDB dbHelper ;
    private GoogleApiClient mGoogleApiClient;
    public  double curlatitude;
    public  double curlongitude;
    float zoom = 16;
    public String checkinTime;
    private static final String TAG = "Debugger ";
    Location mCurrentLocation;
    Calendar calendar;
    LatLng place;
    SimpleDateFormat simpleDateFormat;
    private DatabaseReference database;
    String UID="";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    LocationRequest mLocationRequest;
    boolean inputerror= false;
    int i=0;
    double hours,mins,checkinhour,checkinmin;
    int dollars,cents;
    CameraPosition position;
    LatLng cameracenter;
    public static final String ARG_PAGE = "ARG_PAGE";
    MapView gMapView;
    private int mPage;
    ImageView pin;
    private static final int REQ_CODE = 1;
    String rph,h,m;

    //------------------------------Fragment Lifecycle Related Functions-------------------------//

    public static CheckInFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        CheckInFragment fragment = new CheckInFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mPage = getArguments().getInt(ARG_PAGE);

        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = new LocationRequest();            // Create location request
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS); //periodically update location
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS); //fastest update interval
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");      //format for date

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
        if (mGoogleApiClient.isConnected()) {    //start location updates once apiclient is connected
            startLocationUpdates();
        }
        if (null != gMapView){
            gMapView.onResume();}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkin, container, false); //inflate the view
        pin = (ImageView)view.findViewById(R.id.pinimage);                         //get the marker
        pin.setOnClickListener(this);                                              //and set its onclicklistener
        gMapView = (MapView) view.findViewById(R.id.gmap);
        gMapView.onCreate(savedInstanceState);
        gMapView.onResume();                                                      //get mapView and initialize it
        MapsInitializer.initialize(getActivity());
        gMapView.getMapAsync(this);

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
        map = googleMap;                    //when GoogleMap is ready, put it into the existing map object
        map.getUiSettings().setMyLocationButtonEnabled(true);

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

    //-------------------------------CheckIn Action Related Functions-------------------------//

    @Override
    public void onClick(View v) {
        showCheckInDialog();           //get the dialog when user clicks marker
    }

    public void showCheckInDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new CheckInDialog();
        dialog.setTargetFragment(CheckInFragment.this, REQ_CODE);       //set target fragment to this fragment
        dialog.show(this.getActivity().getSupportFragmentManager(),"CheckIn fragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //fetch information from the dialog and call the checkIn function
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==-1){
            Bundle bundle = data.getExtras();
            String rate = bundle.getString("rates", rph);
            String hour = bundle.getString("hours",h);
            String min  = bundle.getString("mins",m);
            Log.d(TAG,"rate per hour : "+ rate);
            Log.d(TAG,"rate per hour : "+ hour);
            Log.d(TAG,"rate per hour : "+ min);
            checkIn(rate,hour,min);
        }
    }


    public void checkIn(String parkrate,String parkhour,String parkmin) {

        position = map.getCameraPosition();                 //get the camera position
        cameracenter = position.target;                     //and get the center of the map
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        calendar = Calendar.getInstance();                    //get current time
        checkinTime = simpleDateFormat.format(calendar.getTime());  //convert time into desirable format
        String[] timearray = checkinTime.split(":");               //split the time into hours and mins
        checkinhour = Double.parseDouble(timearray[0]);
        checkinmin = Double.parseDouble(timearray[1]);
        double d = toDouble(parkrate);
        if(d==12345.){
            Toast.makeText(this.getActivity(),"Invalid parking rate!",Toast.LENGTH_SHORT).show();
            return;
        }
        dollars =  (int)Math.round(Math.floor(d));
        cents = (int)Math.round(100*(d - Math.floor(d)));
        String LatLngCode = getLatLngCode(cameracenter.latitude,cameracenter.longitude);  //convert the checkin location to its LatLngCode
        Log.d(TAG, "LatLngCode : " + LatLngCode);

        String key = database.child("CheckInKeys/"+LatLngCode).push().getKey();  //push an entry into CheckInKeys node and get its key
        //construct the CheckInDetails object
        CheckInDetails checkInDetails = new CheckInDetails(cameracenter.latitude,cameracenter.longitude,dollars,cents,UID,10031);
        Map<String, Object> checkInDetailsMap = checkInDetails.toMap(); //call its toMap method
        CheckInUser user = new CheckInUser(cameracenter.latitude,cameracenter.longitude,LatLngCode,key);  // construct the CheckInUser object
        Map<String, Object> userMap = user.toMap();                    //call its toMap method

        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/CheckInKeys/"+LatLngCode+"/"+key, checkInDetailsMap);
        childUpdates.put("/CheckInUsers/"+UID,userMap);
        database.updateChildren(childUpdates);                        //simultaneously update the database at both locations

        //Proceed towards starting NotificationBroadcast
        hours = Double.parseDouble(parkhour);
        mins = Double.parseDouble(parkmin);
        int delay = (int)getDelay(checkinhour,checkinmin,hours,mins) - 900000;            //get the delay for notification
        if (delay<0){
            Toast.makeText(this.getActivity(),"Cannot set notification",Toast.LENGTH_LONG).show();  //cant set notification if time is too less
            return;
        }
        scheduleNotification(getAlertNotification(),delay,1);       //schedule notification 15mins prior to ticket expiring
        scheduleNotification(getInformNotification(),delay + 60000 ,23);    //ask user if he wants to inform others by this notification

        //Put in checkin information into phone local storage
        dbHelper = new CheckInHelperDB(this.getActivity());
        dbHelper.updateInfo(UID,cameracenter.latitude,cameracenter.longitude,checkinhour,checkinmin,checkinhour,checkinmin);
        Intent servIntent = new Intent(this.getActivity(),LocationService.class);     //start the LocationService
        this.getActivity().startService(servIntent);
        Intent intent = new Intent(this.getActivity(), CheckedIn.class);             //move on to the next activity
        startActivity(intent);

    }

    //---------------------------Notifications Related Functions---------------------------//

    private void scheduleNotification(Notification notification, int delay, int unique) {

        Intent notificationIntent = new Intent(this.getActivity(), NotificationPublisher.class);   //send intent to NotificationPublisher class
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, unique);  //attach Notification ID
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification); //and Notification with the intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getActivity(), unique, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT); //setup the broadcase with the pending intent

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)this.getActivity().getSystemService(Context.ALARM_SERVICE);       //setup an AlarmService
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    //construct the notification that allows the user to navigate back to his car
    private Notification getAlertNotification() {

        Intent navigate = new Intent(this.getActivity(), NavigationActivity.class);
        navigate.putExtra("user_id",UID);
        navigate.putExtra("started_from","notification");
        PendingIntent pIntent = PendingIntent.getActivity(this.getActivity(), 0, navigate, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.accept, "Navigate to Car", pIntent).build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getActivity());
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle("SpotPark");
        builder.setContentText("Parking Ticket expires in 15min !");
        builder.addAction(accept);
        builder.setAutoCancel(true);

        return builder.build();
    }

    //construct notification asking users to inform others
    private Notification getInformNotification() {

        Intent serviceintent = new Intent(this.getActivity(),DirectionService.class);
        serviceintent.putExtra("started_from","checkin");
        PendingIntent pIntent = PendingIntent.getService(this.getActivity(), 0, serviceintent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.accept, "Yes", pIntent).build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getActivity());
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle("SpotPark");
        builder.setContentText("Inform other users that you're leaving?");
        builder.addAction(accept);
        builder.setAutoCancel(true);


        return builder.build();
    }

    //--------------------------------------Other Helper Functions------------------------------------//

    private void updateUI() {
        Log.d(TAG, "yayy location updated !!!");
        curlatitude = mCurrentLocation.getLatitude();       //get the latitude
        curlongitude = mCurrentLocation.getLongitude();     //get the longitude
        place = new LatLng(curlatitude, curlongitude);      //initiate LatLng object
        //first time this method is called, put a marker on user's location and zoom in on it
        if(i==0){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, zoom));//zoom on the location
        }
        i=i+1;

    }

    public Double toDouble(String var){                                   //convert String to Double
        try{
            Double i = Double.parseDouble(var.trim());
            return i;
        }

        catch (NumberFormatException nfe){
            inputerror = true;
            return 12345.;
        }

    }

    //function to generate the LatLngCode
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


    //function that calculates time difference between two times in milliseconds
    public double getDelay(double checkinhour,double checkinmin,double checkouthour,double checkoutmin){
        double comins;
        double cimins;
        double mindelay=0.;
        cimins = checkinhour*60 + checkinmin;
        comins = checkouthour*60 + checkoutmin;
        if(comins>=cimins){
            mindelay = comins - cimins;
        }
        if(comins<cimins){
            mindelay = (24*60-cimins)+comins;
        }
        return (mindelay*60*1000);
    }



}
