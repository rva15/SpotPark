package com.example.android.sp;
// All imports
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 9/13/16.
 */
public class CheckInFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener,View.OnClickListener{

    //variable declarations

    //--General Utility--
    private  double curlatitude,curlongitude;
    private float zoom = 16;
    private String checkinTime,deftitle="Untitled";
    private static final String TAG = "Debugger ";
    private static String UID="";
    private int i=0,walktimedef = 10031;
    private double hours=123,mins=123,checkinhour,checkinmin;
    private int dollars,cents;
    private ImageView pin;
    private static final int REQ_CODE = 1;
    private String rph,h,m,o,c,t;
    private static final String ARG_PAGE = "ARG_PAGE";
    private CheckInHelperDB dbHelper ;
    private boolean inputerror= false;

    //--Google API variables--
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Calendar calendar;
    private LatLng place;
    private SimpleDateFormat simpleDateFormat;
    private DatabaseReference database;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private LocationRequest mLocationRequest;
    private CameraPosition position;
    private LatLng cameracenter;
    private MapView gMapView;
    private Bitmap bitmap;


    //------------------------------Fragment Lifecycle Related Functions-------------------------//

    public static CheckInFragment newInstance(int page,String id) {
        UID = id;
        Log.d(TAG," id passed :"+UID);
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

        // Check if gps is on, otherwise display message to user
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(),12);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                }
            }
        });


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

        // Initialize the Ad unit
        NativeExpressAdView adView = (NativeExpressAdView)view.findViewById(R.id.cinadView);
        AdRequest request = new AdRequest.Builder()
                .addTestDevice(getResources().getString(R.string.test_device_ID))
                .build();
        adView.loadAd(request);
        return view;
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
            String rate = bundle.getString("rates", rph);
            String hour = bundle.getString("hours",h);
            String min  = bundle.getString("mins",m);
            String option = bundle.getString("option",o);
            String checked = bundle.getString("checked",c);
            String tag = bundle.getString("tag",t);

            checkIn(rate,hour,min,option,checked,tag);
        }
    }


    private void checkIn(String parkrate,String parkhour,String parkmin,String parkoption,String parkchecked,final String tag) {

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
        double d = toDouble(parkrate);
        if(d==12345.){
            Toast.makeText(this.getActivity(),"Invalid parking rate!",Toast.LENGTH_SHORT).show(); //show a message if parking rate is invalid
            return;
        }
        dollars =  (int)Math.round(Math.floor(d));
        cents = (int)Math.round(100*(d - Math.floor(d)));

        // Get the spot's LatLngCode
        String LatLngCode = getLatLngCode(cameracenter.latitude,cameracenter.longitude);  //convert the checkin location to its LatLngCode
        Log.d(TAG, "LatLngCode : " + LatLngCode);

        // Setup notifications and alert user if time entered is invalid
        //Proceed towards starting NotificationBroadcast
        if(parkchecked.equals("1")) {
            hours = Double.parseDouble(parkhour);
            mins = Double.parseDouble(parkmin);
            //get the requested delay period
            int sub = 900000;
            if (parkoption.equals("15")) {
                sub = 900000;
            }
            if (parkoption.equals("30")) {
                sub = 1800000;
            }
            if (parkoption.equals("45")) {
                sub = 2700000;
            }
            if (parkoption.equals("60")) {
                sub = 3600000;
            }
            int delay = (int) getDelay(checkinhour, checkinmin, hours, mins) - sub;    //get the delay for notification
            if (delay < 0) {
                Toast.makeText(this.getActivity(), "Your requested alert time has already passed!", Toast.LENGTH_LONG).show();  //cant set notification if time is too less
                return;
            }
            Log.d(TAG,"notification delay "+Integer.toString(delay));
            scheduleNotification(getAlertNotification(), delay, 1);              //schedule alert notification for ticket expiring
            scheduleNotification(getInformNotification(), delay + 12000, 23);    //ask user if he wants to inform others by this notification
        }

        // Proceed to make database entries
        String key = database.child("CheckInKeys/"+LatLngCode).push().getKey();  //push an entry into CheckInKeys node and get its key
        //construct the CheckInDetails  and CheckInUser objects
        CheckInDetails checkInDetails = new CheckInDetails(cameracenter.latitude,cameracenter.longitude,dollars,cents,UID,walktimedef);
        Map<String, Object> checkInDetailsMap = checkInDetails.toMap(); //call its toMap method
        CheckInUser user = new CheckInUser(cameracenter.latitude,cameracenter.longitude,(int)hours,(int)mins,LatLngCode,key);  // construct the CheckInUser object
        Map<String, Object> userMap = user.toMap();                    //call its toMap method
        // Make an entry in user's history
        HistoryPlace historyPlace = new HistoryPlace(cameracenter.latitude,cameracenter.longitude,strDate,gettimeformat(timearray[0],timearray[1]));
        Map<String, Object> historyMap = historyPlace.toMap();
        // Make the entries
        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/CheckInKeys/"+LatLngCode+"/"+key, checkInDetailsMap);
        childUpdates.put("/CheckInUsers/"+UID,userMap);
        childUpdates.put("/HistoryKeys/"+UID+"/"+key,historyMap);
        if(tag.equals("1")){
            // If user asked to add the spot to favorites
            FavoritePlace favoritePlace = new FavoritePlace(cameracenter.latitude,cameracenter.longitude,deftitle);
            Map<String, Object> favoriteMap = favoritePlace.toMap();
            childUpdates.put("/FavoriteKeys/"+UID+"/"+key,favoriteMap);
            Toast.makeText(this.getContext(),"Spot added to Favorites",Toast.LENGTH_SHORT).show();
        }
        incrementKeys();                                              //award the user with 2 keys
        HomeScreenActivity homeScreenActivity = (HomeScreenActivity) this.getActivity();
        homeScreenActivity.refreshMainAdapter();
        database.updateChildren(childUpdates);                        //simultaneously update the database at all locations


        //Put the screenshot of map into FileStorage
        final String userid = UID;
        final String cikey  = key;
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {

                bitmap = snapshot;
                showPostCheckin();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
                StorageReference historyRef = storageRef.child(userid+"/History/"+cikey+".jpg");

                UploadTask uploadTask = historyRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.d(TAG,"image upload failed");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG,"image upload success");

                    }
                });

                if(tag.equals("1")){
                    StorageReference favoriteRef = storageRef.child(userid+"/Favorites/"+cikey+".jpg");
                    UploadTask uploadTask2 = favoriteRef.putBytes(data);
                    uploadTask2.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Log.d(TAG,"image upload failed");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.d(TAG,"image upload success");
                        }
                    });
                }

            }
        };

        pin.setVisibility(View.GONE);
        map.addMarker(new MarkerOptions().position(cameracenter).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        map.snapshot(callback);



        //Put in checkin information into phone local storage
        dbHelper = new CheckInHelperDB(this.getActivity());
        dbHelper.updateInfo(UID,cameracenter.latitude,cameracenter.longitude,checkinhour,checkinmin,checkinhour,checkinmin);
        Intent servIntent = new Intent(this.getActivity(),LocationService.class);     //start the LocationService
        this.getActivity().startService(servIntent);


    }

    private void showPostCheckin(){
        HomeScreenActivity homeScreenActivity = (HomeScreenActivity)this.getActivity(); //display post checkin message
        homeScreenActivity.getCheckedin(bitmap,hours,mins);
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

        Intent navigate = new Intent(this.getActivity(), HomeScreenActivity.class);
        navigate.putExtra("startedfrom","notification");
        navigate.putExtra("sendstatus",true);
        navigate.putExtra("userid",UID);
        PendingIntent pIntent = PendingIntent.getActivity(this.getActivity(), 0, navigate, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.accept, "Navigate to Car", pIntent).build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getActivity());
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(this.getContext(), R.color.tab_background_unselected));
        builder.setContentTitle("SpotPark");
        builder.setContentText("Parking Ticket expires in 15min !");
        builder.addAction(accept);
        builder.setAutoCancel(true);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(uri);

        return builder.build();
    }

    //construct notification asking users to inform others
    private Notification getInformNotification() {

        Intent serviceintent = new Intent(this.getActivity(),DirectionService.class);
        serviceintent.putExtra("started_from","checkin");
        PendingIntent pIntent = PendingIntent.getService(this.getActivity(), 0, serviceintent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.accept, "Yes", pIntent).build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getActivity());
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(this.getContext(), R.color.tab_background_unselected));
        builder.setContentTitle("SpotPark");
        builder.setContentText("Inform other users that you're leaving?");
        builder.addAction(accept);
        builder.setAutoCancel(true);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(uri);


        return builder.build();
    }

    //--------------------------------------Other Helper Functions------------------------------------//

    private void updateUI() {
        Log.d(TAG, "location updated");
        curlatitude = mCurrentLocation.getLatitude();       //get the latitude
        curlongitude = mCurrentLocation.getLongitude();     //get the longitude
        place = new LatLng(curlatitude, curlongitude);      //initiate LatLng object
        //first time this method is called, put a marker on user's location and zoom in on it
        if(i==0){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, zoom));//zoom on the location
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

    private String gettimeformat(String hour,String min){
        int hours = Integer.parseInt(hour);
        int mins  = Integer.parseInt(min);
        String time="";
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
