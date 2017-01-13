package com.example.android.sp;
// All imports
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.Map;

public class HomeScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    // -- Variable Declarations --

    // General Utility
    private String UID="",starter="",latlngcode,key;
    private int count=0;
    private Boolean isCheckedin;
    private LinearLayout fragmentcontainer;
    private String TAG="debugger";
    private final static String logoutFlagString = "logoutflag";


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
        inflater.inflate(R.menu.activity_main_actions, menu); //inflate menu
        return super.onCreateOptionsMenu(menu);
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
            delete();
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ----------- Main Menu Functions-----------------//


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
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        getcheckin = database.child("CheckInUsers").orderByKey().equalTo(UID);   //Attach listener to Checkinusers
        getcheckin.addChildEventListener(listener1);
        getcheckin.addValueEventListener(listener2);
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

    //--------------------------------Helper Functions----------------------------------------//

    //define the ChildEventListener for Delete CheckIn function
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if(count==0) {
                Log.d(TAG, "detected something");
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
                        Log.d(TAG,"google signed out");
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

    public void getReportForm(String s, String lat, String lon, byte[] bytearray){
        Log.d(TAG,"reportform string "+s);
        Intent intent = new Intent(this, ReportForm.class); //send Intent
        intent.putExtra("user_id",s);
        intent.putExtra("lats",lat);
        intent.putExtra("lons",lon);
        intent.putExtra("image",bytearray);
        startActivity(intent);
        this.finish();
    }




}