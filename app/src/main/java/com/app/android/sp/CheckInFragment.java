package com.app.android.sp;
// All imports
import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.delay;
import static com.app.android.sp.SearchHelperDB.key;
import static com.facebook.FacebookSdk.getCacheDir;

/**
 * Created by ruturaj on 9/13/16.
 */
public class CheckInFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener,View.OnClickListener{

    //variable declarations

    //--General Utility--
    private  double curlatitude,curlongitude;
    private float zoom = 18;
    private String checkinTime,deftitle="Untitled",key;
    private static final String TAG = "Debugger ";
    private static String UID="";
    private int i=0,walktimedef = 10031,delay = -1;
    private double hours=123,mins=123,checkinhour,checkinmin;
    private int dollars,cents,sub;
    private ImageView pin;
    private static final int REQ_CODE = 1;
    private String rph,h,m,o,c,t;
    private static final String ARG_PAGE = "ARG_PAGE";
    private CheckInHelperDB dbHelper ;
    private boolean inputerror= false,isgridview=true;
    private ImageView csatview,cgridview;

    //--Google API variables--
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Calendar calendar;
    private LatLng place;
    private SimpleDateFormat simpleDateFormat;
    private DatabaseReference database;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private LocationRequest mLocationRequest;
    private CameraPosition position;
    private LatLng cameracenter;
    private MapView gMapView;
    private Bitmap bitmap;
    private Marker marker;


    //------------------------------Fragment Lifecycle Related Functions-------------------------//

    public static CheckInFragment newInstance(int page,String id) {
        UID = id;
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
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }                              //disconnect apiclient on stop
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
        return view;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){

        pin = (ImageView)view.findViewById(R.id.pinimage);                         //get the marker
        pin.setOnClickListener(this);                                              //and set its onclicklistener
        gMapView = (MapView) view.findViewById(R.id.gmap);
        gMapView.onCreate(savedInstanceState);
        gMapView.onResume();                                                      //get mapView and initialize it
        try {
            MapsInitializer.initialize(getActivity());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        gMapView.getMapAsync(this);
        csatview = (ImageView) view.findViewById(R.id.csatview);
        csatview.setOnClickListener(this);
        cgridview = (ImageView) view.findViewById(R.id.cgridview);
        cgridview.setOnClickListener(this);
        doFailSafe();

        // Initialize the Ad unit
        /*NativeExpressAdView adView = (NativeExpressAdView)view.findViewById(R.id.cinadView);
        AdRequest request = new AdRequest.Builder()
                .addTestDevice(getResources().getString(R.string.test_device_ID))
                .build();
        adView.loadAd(request);*/

    }


    //----------------------Location Related Functions----------------------------------//

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
        mCurrentLocation = location;                     //stores current location
        updateUI();                                      //will update UI accordingly
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;                    //when GoogleMap is ready, put it into the existing map object
        map.getUiSettings().setMyLocationButtonEnabled(true);

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
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)){
                //display message to user
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permission necessary");
                alertBuilder.setMessage("Permission to access your location is necessary");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 64);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();

            }
            else{
                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        64);
            }
        }
        else {
            //if yes, request location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);         //location request requests updates periodically
        }
    }

    //-------------------------------CheckIn Action Related Functions-------------------------//

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 64: {
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
        if(v.getId()==R.id.pinimage) {
            showCheckInDialog();           //get the dialog when user clicks marker
        }
        if(v.getId() == R.id.csatview){
            csatview.setVisibility(View.GONE);
            cgridview.setVisibility(View.VISIBLE);
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            isgridview = false;
            updateUI();
        }
        if(v.getId() == R.id.cgridview){
            cgridview.setVisibility(View.GONE);
            csatview.setVisibility(View.VISIBLE);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            isgridview = true;
            updateUI();
        }
    }

    private void showCheckInDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new CheckInDialog();
        dialog.setTargetFragment(CheckInFragment.this, REQ_CODE);       //set target fragment to this fragment
        dialog.show(this.getActivity().getSupportFragmentManager(),"CheckIn fragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //fetch information from the dialog and call the checkIn function
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== 1){
            Bundle bundle = data.getExtras();
            if(bundle==null){
                showCheckInDialog();
                Toast.makeText(getContext(),"Sorry, an error occurred.",Toast.LENGTH_SHORT).show();
            }
            String rate = "0",cinnotes="";
            int hour=0,min=0;
            Boolean otherspark = false,free=true;
            rate = bundle.getString("cph", rph);
            hour = bundle.getInt("hours");
            min  = bundle.getInt("mins");
            otherspark = bundle.getBoolean("otherspark");
            free = bundle.getBoolean("free");
            cinnotes = bundle.getString("cinnotes");

            checkIn(rate,hour,min,otherspark,free,cinnotes);
        }
    }


    private void checkIn(String parkrate, int parkhour, int parkmin, final boolean otherspark, boolean free, final String cinnotes) {

        Toast.makeText(getContext(),"Saving location",Toast.LENGTH_SHORT).show();
        // Initialize all required data for checking in
        position = map.getCameraPosition();                 //get the camera position
        cameracenter = position.target;                     //and get the center of the map
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        calendar = Calendar.getInstance();                    //get current time
        checkinTime = simpleDateFormat.format(calendar.getTime());  //convert time into desirable format
        String[] timearray = checkinTime.split(":");               //split the time into hours and mins
        checkinhour = Double.parseDouble(timearray[0]);
        checkinmin = Double.parseDouble(timearray[1]);
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd "); //also get current date in this format
        String strDate = mdformat.format(calendar.getTime());


        // Get the parking rate in dollars and cents
        if(otherspark) {  //if others can park
            if (!free) {    //and it is not free
                double d = toDouble(parkrate);  //check if rate entered is valid
                if (d == 12345.) {
                    Toast.makeText(this.getActivity(), "Invalid cost/hour!", Toast.LENGTH_SHORT).show(); //show a message if parking rate is invalid
                    return;
                }
                dollars = (int) Math.round(Math.floor(d));
                cents = (int) Math.round(100 * (d - Math.floor(d)));
            }
        }
        else{ //if others cannot park, rate is 0
            dollars=0;
            cents = 0;
        }

        // Get the spot's LatLngCode
        final String LatLngCode = getLatLngCode(cameracenter.latitude,cameracenter.longitude);  //convert the checkin location to its LatLngCode


        // Setup notifications and alert user if time entered is invalid
        //Proceed towards starting NotificationBroadcast
        if(parkhour!=123 && parkmin!=123) { //if reminder has been set
            hours = (double)parkhour;
            mins =  (double)parkmin;
            //get the requested delay period
            delay = (int) getDelay(checkinhour, checkinmin, hours, mins);    //get the delay for notification
            if (delay < 0) {
                Toast.makeText(this.getActivity(), "Invalid reminder time!", Toast.LENGTH_LONG).show();  //cant set notification if time is too less
                return;
            }

        }

        //Put the screenshot of map into internal storage
        final GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {

                bitmap = snapshot;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] data = baos.toByteArray();
                File file;
                FileOutputStream outputStream;
                try {
                    file = new File(getContext().getFilesDir(), key); //name of file is the checkin key
                    outputStream = new FileOutputStream(file);
                    outputStream.write(data);
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Put in checkin information into phone local storage
                if(otherspark) {
                    dbHelper = new CheckInHelperDB(getActivity());
                    dbHelper.updateInfo(UID,cameracenter.latitude,cameracenter.longitude,checkinhour,checkinmin,checkinhour,checkinmin);
                    Intent servIntent = new Intent(getActivity(), LocationService.class);     //start the LocationService if others can park
                    getActivity().startService(servIntent);
                }
                showPostCheckin(LatLngCode,key,data,cinnotes,otherspark);
            }
        };


        // Proceed to make database entries
        key = database.child("CheckInKeys/"+LatLngCode).push().getKey();  //push an entry into CheckInKeys node and get its key
        //construct the CheckInDetails  and CheckInUser objects
        if(!otherspark){  //set this value if others cannot park
            walktimedef = 20041;
        }
        CheckInDetails checkInDetails = new CheckInDetails(cameracenter.latitude,cameracenter.longitude,dollars,cents,UID,walktimedef,strDate,(int)checkinhour,(int)checkinmin,cinnotes);
        Map<String, Object> checkInDetailsMap = checkInDetails.toMap(); //call its toMap method
        CheckInUser user = new CheckInUser(cameracenter.latitude,cameracenter.longitude,(int)hours,(int)mins,LatLngCode,key);  // construct the CheckInUser object
        Map<String, Object> userMap = user.toMap();                    //call its toMap method

        // Make the entries
        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/CheckInKeys/"+LatLngCode+"/"+key, checkInDetailsMap);
        childUpdates.put("/CheckInUsers/"+UID,userMap);


        incrementKeys();                                              //award the user with 2 keys
        HomeScreenActivity homeScreenActivity = (HomeScreenActivity) this.getActivity();
        homeScreenActivity.refreshMainAdapter();
        database.updateChildren(childUpdates).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showCheckInDialog();
                Toast.makeText(getContext(),"Sorry, an error occurred.",Toast.LENGTH_SHORT).show();
                return;
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                pin.setVisibility(View.GONE);
                marker.remove();
                map.addMarker(new MarkerOptions().position(cameracenter).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                map.snapshot(callback);
            }
        });                        //simultaneously update the database at all locations

    }

    private void showPostCheckin(String latlngcode,String key,byte[] mapimage,String cinnotes,boolean otherspark){
        if(delay != -1) {
            scheduleNotification(getAlertNotification(), delay, 1);              //schedule alert notification for ticket expiring
            if (otherspark) {
                scheduleNotification(getInformNotification(), delay + 180000, 23);    //ask user if he wants to inform others by this notification
            }
        }
        HomeScreenActivity homeScreenActivity = (HomeScreenActivity)this.getActivity(); //pass information to homescreen activity
        homeScreenActivity.setLatlngcode(latlngcode);
        homeScreenActivity.setLatitude(cameracenter.latitude);
        homeScreenActivity.setLongitude(cameracenter.longitude);
        homeScreenActivity.setCheckinkey(key);
        homeScreenActivity.setRate(dollars,cents);
        homeScreenActivity.setNotes(cinnotes);
        homeScreenActivity.getCheckedin(mapimage,hours,mins,sub); //display the post checkin screen
        homeScreenActivity.refreshMenu();
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

        //open the app on tapping the notification
        Intent openapp = new Intent(getContext(), HomeScreenActivity.class);
        openapp.putExtra("startedfrom","notification");
        openapp.putExtra("userid",UID);
        openapp.addCategory("cinfragalert");
        PendingIntent pendingIntent1 = PendingIntent.getActivity(getContext(), 0, openapp, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent navigate = new Intent(this.getActivity(), HomeScreenActivity.class);
        navigate.putExtra("startedfrom","notification");
        navigate.putExtra("sendstatus",true);
        navigate.putExtra("userid",UID);
        PendingIntent pIntent = PendingIntent.getActivity(this.getActivity(), 0, navigate, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.ic_recenter, "Navigate to Car", pIntent).build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getActivity());
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(this.getContext(), R.color.tab_background_unselected));
        builder.setContentIntent(pendingIntent1);
        builder.setContentTitle("SpotPark");
        builder.setContentText("Your parking is about to expire !");
        builder.addAction(accept);
        builder.setAutoCancel(true);
        builder.setSound(Uri.parse("android.resource://" + getContext().getPackageName() + "/" +R.raw.expiry)); //add custom ringtone

        return builder.build();
    }

    //construct notification asking users to inform others
    private Notification getInformNotification() {

        //open the app on tapping the notification
        Intent openapp = new Intent(getContext(), HomeScreenActivity.class);
        openapp.putExtra("startedfrom","notification");
        openapp.putExtra("userid",UID);
        openapp.addCategory("cinfraginform");
        PendingIntent pendingIntent2 = PendingIntent.getActivity(getContext(), 0, openapp, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent serviceintent = new Intent(this.getActivity(),DirectionService.class);
        serviceintent.putExtra("started_from","checkin");
        PendingIntent pIntent = PendingIntent.getService(this.getActivity(), 0, serviceintent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.accept, "Yes", pIntent).build();
        Intent buttonIntent = new Intent(getContext(), CancelNotification.class);
        buttonIntent.putExtra("notificationId",23);
        PendingIntent btPendingIntent = PendingIntent.getBroadcast(getContext(), 0, buttonIntent,0);
        NotificationCompat.Action cancel = new NotificationCompat.Action.Builder(R.drawable.clear, "No", btPendingIntent).build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getActivity());
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(this.getContext(), R.color.tab_background_unselected));
        builder.setContentIntent(pendingIntent2);
        builder.setContentTitle("SpotPark");
        builder.setContentText("Walking back to vacate parking spot?");
        builder.addAction(accept);
        builder.addAction(cancel);
        builder.setAutoCancel(true);
        builder.setSound(Uri.parse("android.resource://" + getContext().getPackageName() + "/" +R.raw.expiry));//add custom ringtone


        return builder.build();
    }

    //--------------------------------------Other Helper Functions------------------------------------//

    private void updateUI() {
        curlatitude = mCurrentLocation.getLatitude();       //get the latitude
        curlongitude = mCurrentLocation.getLongitude();     //get the longitude
        place = new LatLng(curlatitude, curlongitude);      //initiate LatLng object
        //first time this method is called, put a marker on user's location and zoom in on it
        if(marker!=null){
            marker.remove();                            //remove previous marker
        }
        if(isgridview) {
            marker = map.addMarker(new MarkerOptions().position(place).title("You're here").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mylocation))); //and set it at new location
        }
        else{
            marker = map.addMarker(new MarkerOptions().position(place).title("You're here").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mylocationwhite))); //and set it at new location
        }
        if(i==0){
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(place, zoom));//zoom on the location
        }
        i=i+1;

    }

    //increment user's keys by 2
    private void incrementKeys(){
        database.child("UserInformation").child(UID).child("numberofkeys").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long keys = (long) dataSnapshot.getValue();
                keys = keys+2;
                dataSnapshot.getRef().setValue(keys);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private Double toDouble(String var){                                   //convert String to Double
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
    private String getLatLngCode(double lat, double lon){

        lat = lat*100;                     //get the centi latitudes and centi longitudes
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


    private void doFailSafe(){
        if(!TextUtils.isEmpty(UID)) {
            database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
            database.child("CheckInUsers").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        database.child("CheckInUsers").child(UID).setValue(null);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }



    //function that calculates time difference between two times in milliseconds
    private double getDelay(double checkinhour,double checkinmin,double checkouthour,double checkoutmin){
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
