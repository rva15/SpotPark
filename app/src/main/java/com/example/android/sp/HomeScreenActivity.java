package com.example.android.sp;
// All imports
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.support.v7.app.ActionBarDrawerToggle;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HomeScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, EditCheckInDialog.EditCheckInDialogListener {

    // -- Variable Declarations --

    // General Utility
    private String UID="",starter="",latlngcode,key;
    private int count=0;
    private Boolean isCheckedin,inputerror=false;
    private LinearLayout fragmentcontainer;
    private String TAG="debugger",locationcode,checkinkey;
    private final static String logoutFlagString = "logoutflag";
    private double latitude,longitude;
    private int dollars,cents,isfavorite;


    // Google and Firebase
    private GoogleApiClient mGoogleApiClient;
    public DatabaseReference database;
    com.google.firebase.database.Query getcheckin;


    // Toolbar and Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private String TITLES[] = {"Home","History","Favorites","Contributions","Settings","Logout"};
    private int ICONS[] = {R.drawable.home,
            R.drawable.history,
            R.drawable.favorite,
            R.drawable.contri,
            R.drawable.settings,
            R.drawable.logout};
    private Toolbar toolbar;                              // Declaring the Toolbar Object
    private RecyclerView mRecyclerView;                           // Declaring RecyclerView
    private RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    private RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    private DrawerLayout Drawer;                                  // Declaring DrawerLayout
    private ActionBarDrawerToggle mDrawerToggle;


    //-------------------------------Activity LifeCycle Functions--------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent1 = getIntent();           //Receive intent from loginActivity
        UID     = intent1.getStringExtra("userid"); //Receive logged in user's unique ID
        isCheckedin = intent1.getExtras().getBoolean("sendstatus"); //true if user has active CheckIn
        starter=intent1.getStringExtra("startedfrom");
        Log.d(TAG,"startedfrom "+starter);
        if(starter.equals("notification")){       //check if it was opened from a notification
            Log.d(TAG,"startedfrom notification"); //then cancel pending notifications
            NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancel(1);
        }
        setContentView(R.layout.activity_home_screen);


        GoogleSignInOptions checkingso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //Google signin options
                .requestIdToken(getApplicationContext().getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, checkingso)
                .build();

        // Load the TabsFragment
        getHome();
        fragmentcontainer = (LinearLayout) findViewById(R.id.fragment_container);

        // Get the toolbar and remove it's default title
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup the navigation drawer
        mRecyclerView = (RecyclerView) findViewById(R.id.left_drawer);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        Drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer,toolbar,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }

        };
        Drawer.addDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();
        mAdapter = new MainmenuAdapter(TITLES,ICONS,this,Drawer,UID);
        mRecyclerView.setAdapter(mAdapter);

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStart () {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop () {
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onPause () {
        super.onPause();
    }

    @Override
    public void onResume () {
        super.onResume();
    }

    @Override
    public void onConnected (Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended (int x) {
    }

    //---------------------------Menu Functions--------------------------------//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        if(isCheckedin){
            inflater.inflate(R.menu.checkinmenu, menu); //inflate menu
        }
        else{
            inflater.inflate(R.menu.activity_main_actions, menu); //inflate menu
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        if(isCheckedin){
            inflater.inflate(R.menu.checkinmenu, menu); //inflate menu
        }
        else{
            inflater.inflate(R.menu.activity_main_actions, menu); //inflate menu
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.settings){
            getSettings();
        }
        if(id == R.id.delete){
            database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
            getcheckin = database.child("CheckInUsers").orderByKey().equalTo(UID);   //Attach listener to Checkinusers
            getcheckin.addListenerForSingleValueEvent(listener2);
        }
        if(id == R.id.edit){
            showEditCheckInDialog();
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ----------- Main Menu Functions-----------------//

    private void deletedialog() {   //show a confirmation dialog before deleting the spot
        Log.d(TAG, "entered deletedialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Check-In?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "deletedialog yes");
                delete();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "deletedialog no");
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Delete the checkin
    public void delete(){
        stopService(new Intent(HomeScreenActivity.this,LocationService.class)); //Stop services
        stopService(new Intent(HomeScreenActivity.this,DirectionService.class));
        Intent cancelaction = new Intent(this, NotificationPublisher.class);    //Cancel notifications
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, cancelaction, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Intent cancelaction2 = new Intent(this, NotificationPublisher.class);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 23, cancelaction2, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager2 = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager2.cancel(pendingIntent2);
        getcheckin.addChildEventListener(listener1);

    }

    // Get History Fragment
    public void getHistory(){
        Bundle data = new Bundle();
        data.putString("userid",UID);
        HistoryFragment historyFragment = new HistoryFragment();
        historyFragment.setArguments(data);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, historyFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // Reload TabsFragment
    public void getHome(){
        Bundle data = new Bundle();
        data.putString("userid",UID);
        data.putBoolean("isCheckedin",isCheckedin);
        TabsFragment tabsFragment = new TabsFragment();
        tabsFragment.setArguments(data);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, tabsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // Get the Favorites Fragment
    public void getFavorite(){
        Bundle data = new Bundle();
        data.putString("userid",UID);
        FavoriteFragment favoriteFragment = new FavoriteFragment();
        favoriteFragment.setArguments(data);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, favoriteFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // Get the Contributions Fragment
    public void getContri(){
        Bundle data = new Bundle();
        data.putString("userid",UID);
        data.putInt("width",fragmentcontainer.getWidth());
        ContributionsFragment contributionsFragment = new ContributionsFragment();
        contributionsFragment.setArguments(data);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, contributionsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // Get the settings fragment
    public void getSettings(){
        Bundle data = new Bundle();
        data.putString("userid",UID);
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setArguments(data);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, settingsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // Get the checked in fragment
    public void getCheckedin(Bitmap mapimage,double hours,double mins,int sub){
        isCheckedin=true;
        Bundle data = new Bundle();
        data.putString("userid",UID);
        data.putParcelable("mapimage",mapimage);
        data.putDouble("hours",hours);
        data.putDouble("mins",mins);
        data.putInt("sub",sub);
        data.putInt("width",fragmentcontainer.getWidth());
        PostCheckinFragment postCheckinFragment = new PostCheckinFragment();
        postCheckinFragment.setArguments(data);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, postCheckinFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // Get the Report Form fragment
    public void getReportForm(byte[] mapimage,double latitude,double longitude){
        Bundle data = new Bundle();
        data.putString("userid",UID);
        data.putByteArray("mapimage",mapimage);
        data.putDouble("latitude",latitude);
        data.putDouble("longitude",longitude);
        ReportFormFragment reportFormFragment = new ReportFormFragment();
        reportFormFragment.setArguments(data);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, reportFormFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void getPostReport(byte[] mapimage){
        Bundle data = new Bundle();
        data.putString("userid",UID);
        data.putByteArray("repmapimage",mapimage);
        data.putInt("width",fragmentcontainer.getWidth());
        data.putInt("width",fragmentcontainer.getWidth());
        PostReportFragment postReportFragment = new PostReportFragment();
        postReportFragment.setArguments(data);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, postReportFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            getHome();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }



    //--------------------------------Helper Functions----------------------------------------//

    public void showEditCheckInDialog() {
        // Create an instance of the EditCheckIn Dialog fragment and show it
        DialogFragment dialog = new EditCheckInDialog();
        Bundle args = new Bundle();
        args.putInt("dollars",dollars); //pass the previous checkin's rate to dialog
        args.putInt("cents",cents);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(),"Edit Checkin fragment");
    }

    //functions accessed by other classes
    public void setLatlngcode(String latlngcode){
        this.locationcode = latlngcode;
    }

    public void setCheckinkey(String checkinkey){
        this.checkinkey = checkinkey;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public void setRate(int dollars,int cents){
        this.dollars = dollars;
        this.cents = cents;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Dialog dialogView = dialog.getDialog();
        Spinner spin;
        spin = (Spinner) dialogView.findViewById(R.id.editspinner1);
        CheckBox remind = (CheckBox) dialogView.findViewById(R.id.editremind);
        String checked="1";    //default values for checked and favorite
        String favorite="0";
        if(!remind.isChecked()){   //see if the user asked to be reminded
            checked = "0";
        }
        String text = spin.getSelectedItem().toString();
        EditText rph = (EditText) dialogView.findViewById(R.id.editrate);
        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.edittime);
        String hourlyrate = rph.getText().toString();
        double hours=0,mins=0;
        if (Build.VERSION.SDK_INT >= 23 ) {            //Use the correct method according to API levels
            hours = (double) timePicker.getHour();
        }
        else {
            hours = (double) timePicker.getCurrentHour();
        }
        if (Build.VERSION.SDK_INT >= 23 ) {
            mins = (double) timePicker.getMinute();
        }
        else{
            mins = (double) timePicker.getCurrentMinute();
        }
        String hour = Double.toString(hours);
        String min = Double.toString(mins);
        checkIn(hourlyrate,hour,min,text,checked,favorite);
    }

    private void checkIn(String parkrate,String parkhour,String parkmin,String parkoption,String parkchecked,final String tag) {

        stopService(new Intent(HomeScreenActivity.this,LocationService.class)); //Stop services
        stopService(new Intent(HomeScreenActivity.this,DirectionService.class));
        Intent cancelaction = new Intent(this, NotificationPublisher.class);    //Cancel notifications
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, cancelaction, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Intent cancelaction2 = new Intent(this, NotificationPublisher.class);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 23, cancelaction2, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager2 = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager2.cancel(pendingIntent2);

        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        Calendar calendar = Calendar.getInstance();                    //get current time
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");      //format for date
        String checkinTime = simpleDateFormat.format(calendar.getTime());  //convert time into desirable format
        String[] timearray = checkinTime.split(":");               //split the time into hours and mins
        double currenthour = Double.parseDouble(timearray[0]);
        double currentmin = Double.parseDouble(timearray[1]);
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd "); //also get current date in this format
        String strDate = mdformat.format(calendar.getTime());

        // Get the parking rate in dollars and cents
        double d = toDouble(parkrate);
        if(d==12345.){
            Toast.makeText(this,"Invalid parking rate!",Toast.LENGTH_SHORT).show(); //show a message if parking rate is invalid
            return;
        }
        int dollars =  (int)Math.round(Math.floor(d));
        int cents = (int)Math.round(100*(d - Math.floor(d)));


        // Setup notifications and alert user if time entered is invalid
        //Proceed towards starting NotificationBroadcast
        double hours=123,mins=123;
        int sub;
        if(parkchecked.equals("1")) {
            hours = Double.parseDouble(parkhour);
            mins = Double.parseDouble(parkmin);
            //get the requested delay period
            sub = 900000;
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
            int delay = (int) getDelay(currenthour, currentmin, hours, mins) - sub;    //get the delay for notification
            if (delay < 0) {
                Toast.makeText(this, "Your requested alert time has already passed!", Toast.LENGTH_LONG).show();  //cant set notification if time is too less
                return;
            }
            scheduleNotification(getAlertNotification(sub/60000), delay, 1);              //schedule alert notification for ticket expiring
            scheduleNotification(getInformNotification(sub/60000), delay + 12000, 23);    //ask user if he wants to inform others by this notification
        }



        // Proceed to make database entries
        //construct the CheckInDetails  and CheckInUser objects

        CheckInDetails checkInDetails = new CheckInDetails(latitude,longitude,dollars,cents,UID,10031,strDate,(int)currenthour,(int)currentmin);
        Map<String, Object> checkInDetailsMap = checkInDetails.toMap(); //call its toMap method
        CheckInUser user = new CheckInUser(latitude,longitude,(int)hours,(int)mins,locationcode,checkinkey);  // construct the CheckInUser object
        Map<String, Object> userMap = user.toMap();                    //call its toMap method


        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/CheckInKeys/"+locationcode+"/"+checkinkey, checkInDetailsMap);
        childUpdates.put("/CheckInUsers/"+UID,userMap);
        database.updateChildren(childUpdates);                        //simultaneously update the database at all locations

        //Put in checkin information into phone local storage
        CheckInHelperDB dbHelper = new CheckInHelperDB(this);
        dbHelper.updateInfo(UID,latitude,longitude,currenthour,currentmin,currenthour,currentmin);
        Intent servIntent = new Intent(this,LocationService.class);     //start the LocationService
        this.startService(servIntent);

        Toast.makeText(this,"Changes Saved",Toast.LENGTH_SHORT).show();

    }


    private void scheduleNotification(Notification notification, int delay, int unique) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);   //send intent to NotificationPublisher class
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, unique);  //attach Notification ID
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification); //and Notification with the intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, unique, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT); //setup the broadcase with the pending intent

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);       //setup an AlarmService
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    //construct the notification that allows the user to navigate back to his car
    private Notification getAlertNotification(int mins) {

        Intent navigate = new Intent(this, HomeScreenActivity.class);
        navigate.putExtra("startedfrom","notification");
        navigate.putExtra("sendstatus",true);
        navigate.putExtra("userid",UID);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, navigate, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.accept, "Navigate to Car", pIntent).build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(this, R.color.tab_background_unselected));
        builder.setContentTitle("SpotPark");
        builder.setContentText("Parking Ticket expires in "+Integer.toString(mins)+"min !");
        builder.addAction(accept);
        builder.setAutoCancel(true);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(uri);

        return builder.build();
    }

    //construct notification asking users to inform others
    private Notification getInformNotification(int mins) {

        Intent serviceintent = new Intent(this,DirectionService.class);
        serviceintent.putExtra("started_from","checkin");
        PendingIntent pIntent = PendingIntent.getService(this, 0, serviceintent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.accept, "Yes", pIntent).build();
        Intent buttonIntent = new Intent(getApplicationContext(), CancelNotification.class);
        buttonIntent.putExtra("notificationId",23);
        PendingIntent btPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, buttonIntent,0);
        NotificationCompat.Action cancel = new NotificationCompat.Action.Builder(R.drawable.cancel, "No", btPendingIntent).build();


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(this, R.color.tab_background_unselected));
        builder.setContentTitle("SpotPark");
        builder.setContentText("Inform other users that you're leaving in about "+Integer.toString(mins)+"mins ?");
        builder.addAction(accept);
        builder.addAction(cancel);
        builder.setAutoCancel(true);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(uri);


        return builder.build();
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

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        //Do nothing

    }


    //define the ChildEventListener for Delete CheckIn function
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if(count==0) {
                CheckInUser user = dataSnapshot.getValue(CheckInUser.class);
                latlngcode=user.getlatlngcode();
                key = user.getkey();
                deletedata();
                getcheckin.removeEventListener(listener1);
                getcheckin.removeEventListener(listener2);
            }
            count = count+1;
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

    ValueEventListener listener2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(!dataSnapshot.exists()){
                Toast.makeText(getApplicationContext(),"There is no active CheckIn",Toast.LENGTH_SHORT).show();
            }
            else{
                deletedialog();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    // Helper for deleting the CheckIn
    public void deletedata(){
        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/CheckInKeys/"+latlngcode+"/"+key, null);
        childUpdates.put("/CheckInUsers/"+UID,null);                   //Remove the entries from CheckInKeys and CheckInUsers
        database.updateChildren(childUpdates);
        Toast.makeText(this,"Previous checkin deleted",Toast.LENGTH_LONG).show(); //Show a message to user
        isCheckedin=false;
        getHome();
    }

    public void backToLogin(){
        String message1 = "1";
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(       //signout google
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                    }
                });
        Intent intent = new Intent(HomeScreenActivity.this, LoginActivity.class);  //pass intent to login activity
        intent.putExtra(logoutFlagString, message1);  //put the boolean string into it
        startActivity(intent); //start login activity and kill itself
        finish();

    }

    public String getUID() {
        return UID;
    }

    public void refreshMainAdapter(){
        mAdapter.notifyDataSetChanged();
    }






}