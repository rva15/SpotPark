package com.example.android.sp;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

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

import java.util.HashMap;
import java.util.Map;

public class HomeScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    String UID="",starter="";
    Boolean isCheckedin;
    public String TAG="debugger";
    private GoogleApiClient mGoogleApiClient;
    public final static String logoutFlagString = "logoutflag";
    public DatabaseReference database;
    int count=0;
    String latlngcode,key;
    TabsFragment tabsFragment;
    LinearLayout fragmentcontainer;

    //-------------------------------Activity LifeCycle Functions--------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent1 = getIntent();           //Receive intent
        UID     = intent1.getStringExtra("userid"); //Receive logged in user's unique ID
        isCheckedin = intent1.getExtras().getBoolean("sendstatus");
        starter=intent1.getStringExtra("startedfrom");
        Log.d(TAG,"startedfrom "+starter);
        if(starter.equals("notification")){
            Log.d(TAG,"startedfrom notification");
            NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancel(1);
        }
        setContentView(R.layout.activity_home_screen);

        GoogleSignInOptions checkingso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //Google signin options
                .requestIdToken("283432722166-icn0f1dke2845so2ag841mpvdklssum7.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, checkingso)
                .build();

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        TabsFragment tabsFragment = new TabsFragment();
        fragmentTransaction.add(R.id.fragment_container, tabsFragment, "HELLO");
        fragmentTransaction.commit();
        fragmentcontainer = (LinearLayout) findViewById(R.id.fragment_container);


    }

    @Override
    public void onStart () {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop () {
        mGoogleApiClient.disconnect();
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
        switch (item.getItemId()) {
            case R.id.logout:
                Log.d(TAG,"pressed logout");                           //logout button in menu
                backToLogin();
                return true;

            case R.id.delete:
                Log.d(TAG,"pressed delete");                           //logout button in menu
                delete();
                return true;

            case R.id.history:
                Log.d(TAG,"pressed delete");                           //logout button in menu
                getHistory();
                return true;

            case R.id.favorite:
                Log.d(TAG,"pressed delete");                           //logout button in menu
                getFavorite();
                return true;

            case R.id.contri:
                Log.d(TAG,"pressed contri");                           //logout button in menu
                getContri();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void delete(){
        stopService(new Intent(HomeScreenActivity.this,LocationService.class));
        stopService(new Intent(HomeScreenActivity.this,DirectionService.class));
        Intent cancelaction = new Intent(this, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, cancelaction, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Intent cancelaction2 = new Intent(this, NotificationPublisher.class);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 23, cancelaction2, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager2 = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager2.cancel(pendingIntent2);
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        com.google.firebase.database.Query getcheckin = database.child("CheckInUsers").orderByKey().equalTo(UID);
        getcheckin.addChildEventListener(listener1);
    }

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



    //define the ChildEventListener
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if(count==0) {
                Log.d(TAG, "detected something");
                CheckInUser user = dataSnapshot.getValue(CheckInUser.class);
                latlngcode=user.getlatlngcode();
                key = user.getkey();
                deletedata();
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

    public void deletedata(){
        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/CheckInKeys/"+latlngcode+"/"+key, null);
        //childUpdates.put("/CheckInUsers/"+UID+"/"+key,"deleting");
        childUpdates.put("/CheckInUsers/"+UID,null);
        database.updateChildren(childUpdates);
        Toast.makeText(this,"Previous checkin deleted",Toast.LENGTH_LONG).show();
        isCheckedin=false;
        TabsFragment tabsFragment = new TabsFragment();
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, tabsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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



    //--------------------------------Helper Functions----------------------------------------//

    public String getUID() {
        return UID;
    }

    public boolean getStatus() {
        Log.d(TAG,"checked getstatus");
        return isCheckedin;
    }

    public void test(String s, String lat, String lon, byte[] bytearray){
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
