package com.app.android.sp;
// All imports
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.support.v7.app.ActionBarDrawerToggle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.app.android.sp.SPApplication.getContext;

public class HomeScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, EditCheckInDialog.EditCheckInDialogListener {

    // -- Variable Declarations --

    // General Utility
    private String UID="",starter="",latlngcode,key;
    private Boolean isCheckedin,inputerror=false;
    private LinearLayout fragmentcontainer;
    private String TAG="debugger",locationcode,checkinkey,cinnotes="";
    private final static String logoutFlagString = "logoutflag";
    private double latitude,longitude;
    private int couthours,coutmins;
    private int dollars,cents;
    private boolean searchstarted=false;
    private int awardcount=0;
    private Handler mHandler;


    // Google and Firebase
    private GoogleApiClient mGoogleApiClient;
    public DatabaseReference database;
    com.google.firebase.database.Query getcheckin;


    // Toolbar and Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private String TITLES[] = {"Home","History","Favorites","Contributions","Settings","Help","Logout"};
    private int ICONS[] = {R.drawable.home,
            R.drawable.history,
            R.drawable.favorite,
            R.drawable.contri,
            R.drawable.settings,
            R.drawable.help,
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
        starter=intent1.getStringExtra("startedfrom");
        if(starter!=null && starter.equals("notification")){       //check if it was opened from a notification
            NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancel(1);  //remove the notifications that started this
            nMgr.cancel(13);
        }
        setContentView(R.layout.activity_home_screen);
        mHandler = new Handler();


        GoogleSignInOptions checkingso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //Google signin options
                .requestIdToken(getApplicationContext().getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, checkingso)
                .build();

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
        mAdapter = new MainmenuAdapter(TITLES,ICONS,this,Drawer,UID,getContext());
        mRecyclerView.setAdapter(mAdapter);

        // Check if gps is on, otherwise display message to user
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) && !manager.isProviderEnabled( LocationManager.NETWORK_PROVIDER )) {
            buildAlertMessageNoLocation();
        }

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
            buildAlertMessageNoGPS();
        }

        checkAwards();  //see if the user has been awarded keys
        initsingletouch(); //(re)start the singletouch service
        checkExistingCin(); //check if there is an active checkin

    }

    private void setView(){
        // Load the TabsFragment
        getHome();
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
        inflater.inflate(R.menu.activity_main_actions, menu); //inflate menu
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu); //inflate menu
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
            deletedialog();
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

    public void deletedialog() {   //show a confirmation dialog before deleting the spot
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Check-In?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                delete();
                Toast.makeText(getContext(),"Checkin deleted",Toast.LENGTH_SHORT).show(); //Show a message to user
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Delete the checkin
    public void delete(){
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        getcheckin = database.child("CheckInUsers").orderByKey().equalTo(UID);   //Attach listener to Checkinusers
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
        //the runnables ensure a smooth transition
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("userid", UID);
                HistoryFragment historyFragment = new HistoryFragment();
                historyFragment.setArguments(data);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, historyFragment, "history");
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.commit();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
            setupActionBar("History");
        }
    }

    // Reload TabsFragment
    public void getHome(){
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("userid", UID);
                data.putBoolean("isCheckedin", isCheckedin);
                data.putBoolean("searchstarted", searchstarted);
                TabsFragment tabsFragment = new TabsFragment();
                tabsFragment.setArguments(data);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, tabsFragment, "home");
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.commit();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
            setupActionBar("home");
        }
    }

    // Get the Favorites Fragment
    public void getFavorite(){
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("userid", UID);
                FavoriteFragment favoriteFragment = new FavoriteFragment();
                favoriteFragment.setArguments(data);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, favoriteFragment, "favorites");
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.commit();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
            setupActionBar("Favorites");
        }
    }

    // Get the Contributions Fragment
    public void getContri(){
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("userid", UID);
                data.putInt("width", fragmentcontainer.getWidth());
                ContributionsFragment contributionsFragment = new ContributionsFragment();
                contributionsFragment.setArguments(data);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, contributionsFragment, "contributions");
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.commit();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
            setupActionBar("Contributions");
        }

    }

    // Get the settings fragment
    public void getSettings(){
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("userid", UID);
                SettingsFragment settingsFragment = new SettingsFragment();
                settingsFragment.setArguments(data);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, settingsFragment, "settings");
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.commit();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
            setupActionBar("Settings");
        }

    }

    // Get the single touch settings fragment
    public void getSTSettings(){
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("userid", UID);
                STSettingsFragment stsettingsFragment = new STSettingsFragment();
                stsettingsFragment.setArguments(data);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, stsettingsFragment, "stsettings");
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.commit();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
            setupActionBar("Single Touch Settings");
        }

    }

    public void getHelp(){
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getContext(), TutorialActivity.class); //send Intent
                intent.putExtra("userid", UID);
                intent.putExtra("sendstatus", isCheckedin);
                intent.putExtra("startedfrom", starter);
                startActivity(intent);
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }
    }

    // Get the checked in fragment
    public void getCheckedin(final byte[] mapimage,final double hours,final double mins,final int sub){
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                isCheckedin = true;
                Bundle data = new Bundle();
                data.putString("userid", UID);
                data.putByteArray("mapimage", mapimage);
                data.putDouble("hours", hours);
                data.putDouble("mins", mins);
                data.putInt("sub", sub);
                data.putInt("width", fragmentcontainer.getWidth());
                PostCheckinFragment postCheckinFragment = new PostCheckinFragment();
                postCheckinFragment.setArguments(data);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, postCheckinFragment, "postcheckin");
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.commit();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
            setupActionBar("home");
        }
    }

    // Get the Report Form fragment
    public void getReportForm(final byte[] mapimage,final double latitude,final double longitude){
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("userid", UID);
                data.putByteArray("mapimage", mapimage);
                data.putDouble("latitude", latitude);
                data.putDouble("longitude", longitude);
                ReportFormFragment reportFormFragment = new ReportFormFragment();
                reportFormFragment.setArguments(data);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, reportFormFragment, "reportform");
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.commit();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
            setupActionBar("home");
        }
    }

    public void getPostReport(final byte[] mapimage){
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putString("userid", UID);
                data.putByteArray("repmapimage", mapimage);
                data.putInt("width", fragmentcontainer.getWidth());
                data.putInt("width", fragmentcontainer.getWidth());
                PostReportFragment postReportFragment = new PostReportFragment();
                postReportFragment.setArguments(data);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, postReportFragment, "postreport");
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.commit();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
            setupActionBar("home");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            TabsFragment tabsFragment = (TabsFragment) getSupportFragmentManager().findFragmentByTag("home");
            STSettingsFragment stSettingsFragment = (STSettingsFragment) getSupportFragmentManager().findFragmentByTag("stsettings");
            if (tabsFragment != null && tabsFragment.isVisible()) {
                // add your code here
                moveTaskToBack(true);
            }
            else if (stSettingsFragment!=null && stSettingsFragment.isVisible()){ //if current page is STsettings
                getSettings();  //go to settings page with back button
            }
            else{
                getHome(); //otherwise go to home
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    //--------------------------------Helper Functions----------------------------------------//

    private void checkExistingCin(){
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        database.child("CheckInUsers").orderByKey().equalTo(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isCheckedin = dataSnapshot.exists();
                setView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initsingletouch(){
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        database.child("UserInformation").child(UID).child("singletouch").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){ //check if singletouch child exists in database
                    boolean ststatus = dataSnapshot.getValue(Boolean.class);
                    if(ststatus){ //singletouch is set as active
                        Intent servIntent = new Intent(getContext(), SingleTouchService.class);
                        getContext().startService(servIntent);
                    }
                }
                else{ //create a single touch branch and make it active
                    dataSnapshot.getRef().setValue(true);
                    Intent servIntent = new Intent(getContext(), SingleTouchService.class);
                    getContext().startService(servIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //setup the action bar
    private void setupActionBar(String title){
        TextView title1 = (TextView) findViewById(R.id.title1);
        TextView title2 = (TextView) findViewById(R.id.title2);
        ImageView logo  = (ImageView) findViewById(R.id.logo);
        if(title.equals("home")){
            logo.setVisibility(View.VISIBLE);
            title2.setVisibility(View.VISIBLE);
            title1.setText("SPOT  ");
            title2.setText("  PARK");
        }
        else{
            logo.setVisibility(View.GONE);
            title2.setVisibility(View.GONE);
            title1.setText(title);
        }
    }

    //function to check if user has gotten awards
    private void checkAwards() {
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        database.child("ReportedTimes").child(UID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ReportedTimes reportedTimes = dataSnapshot.getValue(ReportedTimes.class);
                if((reportedTimes.getverification()>1)){
                    if((!reportedTimes.getawarded()) || ((Boolean)reportedTimes.getawarded()==null)){
                        awardKeys();
                        dataSnapshot.child("awarded").getRef().setValue(true);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //award keys to user
    private void awardKeys(){
        if(awardcount==0){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            TextView tv = new TextView(this);
            tv.setText("You've earned Keys !!");
            tv.setPadding(0,10,0,10);
            tv.setGravity(Gravity.CENTER);
            final int version = Build.VERSION.SDK_INT;
            final TextView et = new TextView(this);
            et.setText("Some of your reported spots just got verified.");
            et.setPadding(0,10,0,0);
            if (version >= 23) {
                tv.setTextColor(ContextCompat.getColor(this,R.color.white));
                et.setTextColor(ContextCompat.getColor(this,R.color.black));
                tv.setBackgroundColor(ContextCompat.getColor(this,R.color.tab_background_selected));
            } else {
                tv.setTextColor(getResources().getColor(R.color.white));
                et.setTextColor(getResources().getColor(R.color.black));
                tv.setBackgroundColor(this.getResources().getColor(R.color.tab_background_selected));
            }
            alertDialogBuilder.setView(et);
            et.setGravity(Gravity.CENTER);
            alertDialogBuilder.setCustomTitle(tv);
            // Setting Positive "OK" Button
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            awardcount=1;
        }
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        database.child("UserInformation").child(UID).child("numberofkeys").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long keys = (long) dataSnapshot.getValue();
                keys = keys+5;
                dataSnapshot.getRef().setValue(keys);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //show this if user has location service turned off
    private void buildAlertMessageNoLocation() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("SpotPark needs location service, should we enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    //show this if user's location is not on high accuracy mode
    private void buildAlertMessageNoGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("For best results, it is recommended that your location be on 'High Accuracy' mode. Should we change it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    public void showEditCheckInDialog() {
        // Create an instance of the EditCheckIn Dialog fragment and show it
        DialogFragment dialog = new EditCheckInDialog();
        Bundle args = new Bundle();
        args.putInt("dollars",dollars); //pass the previous checkin's rate to dialog
        args.putInt("cents",cents);
        args.putInt("couthours",couthours);
        args.putInt("coutmins",coutmins);
        args.putString("cinnotes",cinnotes);
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

    public void setCoutTime(int couthours,int coutmins){
        this.couthours = couthours;
        this.coutmins = coutmins;
    }

    public void setNotes(String cinnotes){
        this.cinnotes = cinnotes;
    }

    public void setStartSearch(boolean searchstarted){
        this.searchstarted = searchstarted;
    }



    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Dialog dialogView = dialog.getDialog();
        //get the info from editcheckin dialog
        EditText cph = (EditText) dialogView.findViewById(R.id.editcph);
        RadioButton otherspark = (RadioButton) dialogView.findViewById(R.id.editothersyes);
        RadioButton free   = (RadioButton) dialogView.findViewById(R.id.editfree);
        String hourlyrate = cph.getText().toString();
        EditText cinnotes = (EditText) dialogView.findViewById(R.id.editcinnotes);
        String notes = cinnotes.getText().toString();


        checkIn(hourlyrate,couthours,coutmins,otherspark.isChecked(),free.isChecked(),notes);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        //Do nothing

    }


    private void checkIn(String parkrate,int parkhour,int parkmin,boolean otherspark,boolean free,String notes) {

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
        if(otherspark) { //if others can park
            if (!free) {   //and it is not free
                double d = toDouble(parkrate);
                if (d == 12345.) {
                    Toast.makeText(this, "Invalid cost/hour!", Toast.LENGTH_SHORT).show(); //show a message if parking rate is invalid
                    return;
                }
                dollars = (int) Math.round(Math.floor(d));
                cents = (int) Math.round(100 * (d - Math.floor(d)));
            }
        }
        else{  //otherwise rate is 0
            dollars=0;
            cents = 0;
        }

        // Setup notifications and alert user if time entered is invalid
        if(parkhour!=123 && parkmin!=123) {
            double hours = (double)parkhour;
            double mins =  (double)parkmin;
            //get the requested delay period
            int delay = (int) getDelay(currenthour, currentmin, hours, mins);    //get the delay for notification
            if (delay < 0) {
                Toast.makeText(this, "Your requested alert time has already passed!", Toast.LENGTH_LONG).show();  //cant set notification if time is too less
                return;
            }
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

            scheduleNotification(getAlertNotification(), delay, 1);              //schedule new alert notification for ticket expiring
            if(otherspark) {
               scheduleNotification(getInformNotification(), delay + 180000, 23);    //ask user if he wants to inform others by this notification
            }
        }
        else{ //just cancel previous alarms if no new alarm set
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
        }


        // Proceed to make database entries
        //construct the CheckInDetails  and CheckInUser objects
        int walktimedef;
        if(!otherspark){
            walktimedef = 20041;
        }
        else{
            walktimedef = 10031;
        }
        CheckInDetails checkInDetails = new CheckInDetails(latitude,longitude,dollars,cents,UID,walktimedef,strDate,(int)currenthour,(int)currentmin,notes);
        Map<String, Object> checkInDetailsMap = checkInDetails.toMap(); //call its toMap method
        CheckInUser user = new CheckInUser(latitude,longitude,(int)couthours,(int)coutmins,locationcode,checkinkey);  // construct the CheckInUser object
        Map<String, Object> userMap = user.toMap();                    //call its toMap method


        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/CheckInKeys/"+locationcode+"/"+checkinkey, checkInDetailsMap);
        childUpdates.put("/CheckInUsers/"+UID,userMap);
        database.updateChildren(childUpdates);                        //simultaneously update the database at all locations

        //Put in checkin information into phone local storage
        if(otherspark) {
            CheckInHelperDB dbHelper = new CheckInHelperDB(this);
            dbHelper.updateInfo(UID, latitude, longitude, currenthour, currentmin, currenthour, currentmin);
            Intent servIntent = new Intent(this, LocationService.class);     //start the LocationService
            this.startService(servIntent);
        }

        Toast.makeText(this,"Changes Saved",Toast.LENGTH_SHORT).show();
        getHome();

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
    private Notification getAlertNotification() {

        Intent navigate = new Intent(this, HomeScreenActivity.class);
        navigate.putExtra("startedfrom","notification");
        navigate.putExtra("sendstatus",true);
        navigate.putExtra("userid",UID);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, navigate, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.ic_recenter, "Navigate to Car", pIntent).build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(this, R.color.tab_background_unselected));
        builder.setContentTitle("SpotPark");
        builder.setContentText("Your parking is about to expire !");
        builder.addAction(accept);
        builder.setAutoCancel(true);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(uri);

        return builder.build();
    }

    //construct notification asking users to inform others
    private Notification getInformNotification() {

        Intent serviceintent = new Intent(this,DirectionService.class);
        serviceintent.putExtra("started_from","checkin");
        PendingIntent pIntent = PendingIntent.getService(this, 0, serviceintent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action accept = new NotificationCompat.Action.Builder(R.drawable.accept, "Yes", pIntent).build();
        Intent buttonIntent = new Intent(getApplicationContext(), CancelNotification.class);
        buttonIntent.putExtra("notificationId",23);
        PendingIntent btPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, buttonIntent,0);
        NotificationCompat.Action cancel = new NotificationCompat.Action.Builder(R.drawable.clear, "No", btPendingIntent).build();


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.logowhite);
        builder.setColor(ContextCompat.getColor(this, R.color.tab_background_unselected));
        builder.setContentTitle("SpotPark");
        builder.setContentText("Walking back to vacate parking spot?");
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

    //define the ChildEventListener for Delete CheckIn function
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            CheckInUser user = dataSnapshot.getValue(CheckInUser.class);
            latlngcode=user.getlatlngcode();
            key = user.getkey();
            deletedata();
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

    // Helper for deleting the CheckIn
    public void deletedata(){
        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/CheckInKeys/"+latlngcode+"/"+key, null);
        childUpdates.put("/CheckInUsers/"+UID,null);                   //Remove the entries from CheckInKeys and CheckInUsers
        database.updateChildren(childUpdates);
        isCheckedin=false;
        getHome();
    }

    public void backToLogin(){
        final String message1 = "1";

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(       //signout google
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                    }
                });

        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(HomeScreenActivity.this, LoginActivity.class);  //pass intent to login activity
                intent.putExtra(logoutFlagString, message1);  //put the boolean string into it
                startActivity(intent); //start login activity and kill itself
                finish();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

    }

    public String getUID() {
        return UID;
    }

    public void refreshMainAdapter(){
        mAdapter.notifyDataSetChanged();
    }






}