package com.app.android.sp;
// All imports
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.id;
import static com.app.android.sp.SPApplication.getContext;

/**
 * Created by ruturaj on 4/3/17.
 */

public class CheckinService extends Service{
    // Variable Declaration
    private String TAG = "debugger",UID="",checkinkey="",latlngcode="";
    private double userlat, userlon;
    private DatabaseReference database;
    private Calendar calendar;
    private double carlatitude,carlongitude;
    private boolean sentcin = false;

    //---------------------------Service LifeCycle Methods------------------------//

    //onCreate method
    @Override
    public void onCreate(){
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(13);   //destroy the checkin notification since user responded

    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public CheckinService getService() {
            return CheckinService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        UID = readUID();    //get UID from phone storage
        if(TextUtils.isEmpty(UID)){
            Toast.makeText(getApplicationContext(),"An error occured. Please complete this action manually from the SpotPark app",Toast.LENGTH_LONG).show();
            stopSelf();
        }
        if(intent!=null) {
            if((boolean)intent.getExtras().get("action")) { //user asked to checkin
                readLocation();
            }
            else{ //user asked to veto this place
                String vplacename = (String) intent.getExtras().get("vplacename"); //get the place information
                double vplacelat  = (double) intent.getExtras().get("vplacelat");
                double vplacelon  = (double) intent.getExtras().get("vplacelon");

                //add this place to user vetoes
                Places places = new Places(vplacename,vplacelat,vplacelon);
                database = FirebaseDatabase.getInstance().getReference();
                Map<String, Object> placesMap = places.toMap();
                Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
                final String key = database.child("STVetoes/"+UID).push().getKey();
                childUpdates.put("/STVetoes/"+UID+"/"+key,placesMap);
                database.updateChildren(childUpdates);
                Toast.makeText(getApplicationContext(),"You will not be reminded at this place again. You may undo this action from 'Single Touch Settings'",Toast.LENGTH_LONG).show();
                stopSelf();
            }
        }
        else{
            stopSelf();
        }
        return START_NOT_STICKY;

    }

    private void readLocation(){
        final double templat = Double.parseDouble(readLatitude());
        final double templon = Double.parseDouble(readLongitude());
        final Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(templat!=Double.parseDouble(readLatitude()) || templon!=Double.parseDouble(readLongitude())){
                    if(!sentcin) {
                        checkExistingCin(Double.parseDouble(readLatitude()), Double.parseDouble(readLongitude()));  //check if there is an active checkin
                        sentcin = true;
                    }
                    t.cancel();
                }


            }

        }, 0, 2000);
    }

    //function to read the UID
    private String readUID(){
        String line="";
        StringBuffer buffer= new StringBuffer();
        BufferedReader input = null;
        File file = null;
        try {
            file = new File(getCacheDir(), "UIDFile");
            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"An error occured. Please complete this action manually from the SpotPark app",Toast.LENGTH_LONG).show();
            stopSelf();
            e.printStackTrace();
        }

        return buffer.toString();
    }

    //functions to read the current location
    private String readLatitude(){
        String line="";
        StringBuffer buffer= new StringBuffer();
        BufferedReader input = null;
        File file = null;
        try {
            file = new File(getCacheDir(), "curLat");
            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"An error occured. Please complete this action manually from the SpotPark app",Toast.LENGTH_LONG).show();
            stopSelf();
            e.printStackTrace();
        }

        return buffer.toString();
    }

    private String readLongitude(){
        String line="";
        StringBuffer buffer= new StringBuffer();
        BufferedReader input = null;
        File file = null;
        try {
            file = new File(getCacheDir(), "curLon");
            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"An error occured. Please complete this action manually from the SpotPark app",Toast.LENGTH_LONG).show();
            stopSelf();
            e.printStackTrace();
        }

        return buffer.toString();
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

    private void checkExistingCin(double lat, double lon){
        userlat = lat;
        userlon = lon;
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        database.child("CheckInUsers").orderByKey().equalTo(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    getExistingCin();
                }
                else{
                    makeCheckin(); //make checkin directly if there is no active checkin
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getExistingCin(){
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        database.child("CheckInUsers").orderByKey().equalTo(UID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                CheckInUser user = dataSnapshot.getValue(CheckInUser.class);
                carlatitude = user.getcarlatitude();
                carlongitude = user.getcarlongitude();
                checkinkey = user.getkey();
                latlngcode = user.getlatlngcode();
                addToHistory();  //add existing checkin to history, delete it and then call makecheckin
                database.child("CheckInUsers").orderByKey().equalTo(UID).removeEventListener(this);
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



    private void makeCheckin(){
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        String LatLngCode = getLatLngCode(userlat,userlon);   //get latlng code
        calendar = Calendar.getInstance();                    //get current time
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");      //format for date
        String checkinTime = simpleDateFormat.format(calendar.getTime());  //convert time into desirable format
        String[] timearray = checkinTime.split(":");               //split the time into hours and mins
        double checkinhour = Double.parseDouble(timearray[0]);
        double checkinmin = Double.parseDouble(timearray[1]);
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd "); //also get current date in this format
        String strDate = mdformat.format(calendar.getTime());
        //check in while keeping cost 0, no reminder and inform others as false
        CheckInDetails checkInDetails = new CheckInDetails(userlat,userlon,0,0,UID,20041,strDate,(int)checkinhour,(int)checkinmin,"");
        Map<String, Object> checkInDetailsMap = checkInDetails.toMap();              //call its toMap method
        final String key = database.child("CheckInKeys/"+LatLngCode).push().getKey();  //push an entry into CheckInKeys node and get its key
        CheckInUser user = new CheckInUser(userlat,userlon,123,123,LatLngCode,key);  // construct the CheckInUser object
        Map<String, Object> userMap = user.toMap();                    //call its toMap method
        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/CheckInKeys/"+LatLngCode+"/"+key, checkInDetailsMap);
        childUpdates.put("/CheckInUsers/"+UID,userMap);
        database.updateChildren(childUpdates);
        Toast.makeText(getApplicationContext(),"Your car's location is now saved on SpotPark",Toast.LENGTH_SHORT).show(); //show a message to user
        stopSelf();
    }

    private void addToHistory(){

        // Make an entry in user's history saying it has not been favorited
        Calendar calendar = Calendar.getInstance();                    //get current time
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");      //format for date
        String checkinTime = simpleDateFormat.format(calendar.getTime());  //convert time into desirable format
        String[] timearray = checkinTime.split(":");               //split the time into hours and mins
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd "); //also get current date in this format
        String strDate = mdformat.format(calendar.getTime());
        HistoryPlace historyPlace = new HistoryPlace(carlatitude,carlongitude,strDate,gettimeformat(timearray[0],timearray[1]),0);
        Map<String, Object> historyMap = historyPlace.toMap();
        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/HistoryKeys/"+UID+"/"+checkinkey,historyMap);
        database = FirebaseDatabase.getInstance().getReference();
        database.updateChildren(childUpdates);                        //simultaneously update the database at all locations

        Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(getContext(),R.drawable.mapnotav)).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        FirebaseStorage storage = FirebaseStorage.getInstance(); //now upload it to firebase
        StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
        StorageReference historyRef = storageRef.child(UID+"/History/"+checkinkey+".jpg");

        UploadTask uploadTask = historyRef.putBytes(bitmapdata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                delete();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                delete();
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

    // Delete the checkin
    public void delete(){
        stopService(new Intent(getApplicationContext(),LocationService.class)); //Stop services
        stopService(new Intent(getApplicationContext(),DirectionService.class));
        Intent cancelaction = new Intent(this, NotificationPublisher.class);    //Cancel notifications
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, cancelaction, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Intent cancelaction2 = new Intent(this, NotificationPublisher.class);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 23, cancelaction2, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager2 = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager2.cancel(pendingIntent2);
        database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
        Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
        childUpdates.put("/CheckInKeys/"+latlngcode+"/"+checkinkey, null);
        childUpdates.put("/CheckInUsers/"+UID,null);                   //Remove the entries from CheckInKeys and CheckInUsers
        database.updateChildren(childUpdates);
        makeCheckin();
    }



}
