package com.example.android.sp;

//Necessary imports
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

public class OptionsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {
    //Variable declaration
    String UID="";
    public final static String ID="";
    public final String TAG="debugger";
    GoogleApiClient mGoogleApiClient;
    public final static String logoutflag = "logoutflag";


    //--------------------------- Activity Lifecycle Functions--------------------------------//

    //onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);           //set content view
        Intent intent1 = getIntent();                        //Receive intent
        UID     = intent1.getStringExtra(LoginActivity.UID); //get user's unique ID
        GoogleSignInOptions checkingso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //Google Signin
                .requestIdToken("283432722166-icn0f1dke2845so2ag841mpvdklssum7.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)   //GoogleApiClient object initialization
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, checkingso)
                .build();

    }

    @Override
    protected void onStart(){
        mGoogleApiClient.connect();      //onStart of the activity, connect apiclient
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();  //disconnect apiclient on stop
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //call this function
    }

    @Override
    public void onConnectionSuspended(int x){
        //notify user of lost connection
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT); //notify user when connection is suspended
    }

    //-------------------------------Menu Creation--------------------------------//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);          //inflate menu

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:

                Log.d(TAG,"pressed logout");                           //logout button in menu
                backToLogin();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //gets called on pressing the logout button
    public void backToLogin(){
        String message1 = "1";
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(       //signout google
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG,"google signed out");
                    }
                });
        Intent intent3 = new Intent(OptionsActivity.this, LoginActivity.class);  //pass intent to login activity
        intent3.putExtra(logoutflag,message1);                                       //put the boolean string into it
        startActivity(intent3);                                               //start login activity and kill itself
        finish();

    }

    //------------------------------Button onclick functions-----------------------------//

    public void search(View view){
        //Go to search Activity
        Intent intent = new Intent(this, SearchActivity.class); //send Intent
        intent.putExtra(ID,UID);
        startActivity(intent);

    }

    public void checkin(View view){
        //Go to Main Activity
        Intent intent = new Intent(this, CheckInActivity.class); //send Intent
        intent.putExtra(ID,UID);
        startActivity(intent);

    }

    public void report(View view){
        Intent intent = new Intent(this, ReportActivity.class); //send Intent
        intent.putExtra(ID,UID);
        startActivity(intent);

    }
}