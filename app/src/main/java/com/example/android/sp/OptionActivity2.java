package com.example.android.sp;

//Necessary imports
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class OptionActivity2 extends AppCompatActivity {
    //Variable declaration
    String UID="";
    public final static String ID="";
    public DatabaseReference database;
    public static final String TAG ="";
    String latlngcode="",key="";
    int count=0;

    //onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option2);
        Intent intentoption2 = getIntent();           //Receive intent from Login Activity
        UID     = intentoption2.getStringExtra(LoginActivity.UID);    //get user's unique ID
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }


    public void search(View view){
        //Go to search Activity
        Intent intent = new Intent(this, SearchActivity.class); //send Intent
        intent.putExtra(ID,UID);
        startActivity(intent);
        this.finish();
    }

    public void navigate(View view){
        //Go to Main Activity
        Intent intent = new Intent(this, NavigationActivity.class); //send Intent
        intent.putExtra("user_id",UID);
        startActivity(intent);
        this.finish();
    }

    public void report(View view){
        //Go to report Activity
    }

    public void delete(View view){
        stopService(new Intent(OptionActivity2.this,LocationService.class));
        stopService(new Intent(OptionActivity2.this,DirectionService.class));
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

    //define the ChildEventListener
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if(count==0) {
                Log.d(TAG, "detected something");
                CheckInUser user = dataSnapshot.getValue(CheckInUser.class);
                latlngcode = user.getlatlngcode();
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
        childUpdates.put("/CheckInUsers/"+UID,null);
        database.updateChildren(childUpdates);
        Toast.makeText(this,"Previous checkin deleted",Toast.LENGTH_LONG).show();
        Intent intentfromoption2 = new Intent(this, OptionsActivity.class); //send Intent
        intentfromoption2.putExtra(ID,UID);
        startActivity(intentfromoption2);
        this.finish();
    }

}