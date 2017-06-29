package com.app.android.sp;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

/**
 * Created by ruturaj on 6/27/17.
 */

public class ARTestActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private Button strtar, stpar;
    public GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artest);
        strtar = (Button) findViewById(R.id.strtar);
        stpar  = (Button) findViewById(R.id.stpar);
        strtar.setOnClickListener(this);
        stpar.setOnClickListener(this);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

    }

    @Override
    public void onStart () {
        super.onStart();
    }

    @Override
    public void onStop () {
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
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ARService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 3000, pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.strtar){

        }
        if(v.getId()==R.id.stpar){
            stopService(new Intent(ARTestActivity.this,ARService.class)); //Stop services
        }
    }
}
