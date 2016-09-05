package com.example.android.sp;
//Necassary imports
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by ruturaj on 8/19/16.
 */

public class SpotFinder {
    //Necassary variable declaration
    double latitude=0.0,longitude=0.0;
    String UID="";
    private DatabaseReference database;
    private static final String TAG = "Debugger ";
    ArrayList<String> array = new ArrayList<String>();
    LatLng spotplace;
    Marker spotmarker;
    private GoogleMap searchmap;
    int i=0;
    SearchHelperDB helperDB;
    Map markers = new HashMap();
    Map searchers = new HashMap();

    public SpotFinder(){};  //empty constructor

    public SpotFinder(double latitude, double longitude, GoogleMap searchmap,String id){  //contructor receives parameters
        this.latitude=latitude;
        this.longitude=longitude;
        this.searchmap=searchmap;
        this.UID = id;
        helperDB = new SearchHelperDB(SPApplication.getContext());

    }

    //addListener method
    public void addListener(){

        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference

        double lat = latitude*100;              //get centi latitude and centi longitude of user
        double lon = longitude*100;
        int lat1 = (int)Math.round(lat);        //round them off
        int lon1 = (int)Math.round(lon);

        int [] latarray = new int[]{lat1-1,lat1,lat1+1};   //get the two nearest neighbours of above rounded off quantities
        int [] lonarray = new int[]{lon1-1,lon1,lon1+1};



        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                array.add(this.getCode(latarray[i],lonarray[j]));           //get LatLngCodes for all 9points and store them in an array
                Log.d(TAG,"LatLng Code " + this.getCode(latarray[i],lonarray[j]));
            }
        }

        for(int k=0;k<9;k++){
            database.child("CheckInKeys").child(array.get(k)).addChildEventListener(listener1); //add listeners to corresponding nodes in the database
            database.child("Searchers").child(array.get(k)).addChildEventListener(listener2);
            Log.d(TAG,"adding listener to "+array.get(k));
        }

    }


    //define the ChildEventListener
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "somesh : " + dataSnapshot.getKey() );
            CheckInDetails details = dataSnapshot.getValue(CheckInDetails.class);  //retrieve a snapshot from the node and store it in CheckInDetails.class
            Log.d(TAG,"added listener " + Double.toString(details.getlatitude()));
            Log.d(TAG,"added listener " + Double.toString(details.getlongitude()));
            int time = details.getminstoleave();
            if (time>10){
                Log.d(TAG,"time greater than 10");
                //do nothing
            }
            if(time<=2){
                insertdata(dataSnapshot.getKey(),time,1);
                spotplace = new LatLng(details.getlatitude(),details.getlongitude());  //store the spot's location in spotplace
                //add a blue marker at the spot
                Marker marker = (Marker) markers.get(spotplace);
                if(marker!=null){
                    Log.d(TAG,"marker exists");
                }
                else {
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    markers.put(spotplace,spotmarker);
                }
            }
            if(time>2 && time<=10){
                Log.d(TAG,"inserting data");
                if(checkStatus(dataSnapshot.getKey())){
                    spotplace = new LatLng(details.getlatitude(),details.getlongitude());  //store the spot's location in spotplace
                    Marker marker = (Marker) markers.get(spotplace);
                    if(marker!=null){
                        Log.d(TAG,"marker exists");
                    }
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    markers.put(spotplace,spotmarker);
                }
                else {
                    insertdata(dataSnapshot.getKey(), time, 0);
                }
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG,"onchildchanged fired");
            CheckInDetails details = dataSnapshot.getValue(CheckInDetails.class);
            if(makedecision(dataSnapshot.getKey(),details.getminstoleave())){
                Log.d(TAG,"decision positive");
                spotplace = new LatLng(details.getlatitude(),details.getlongitude());  //store the spot's location in spotplace
                //add a blue marker at the spot
                Marker marker = (Marker) markers.get(spotplace);
                if(marker!=null){
                    Log.d(TAG,"marker exists");
                }
                else {
                    Log.d(TAG,"adding marker");
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    markers.put(spotplace,spotmarker);
                }
            }
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

    //define the ChildEventListener
    ChildEventListener listener2 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Searcher searcher = dataSnapshot.getValue(Searcher.class);
            if(!dataSnapshot.getKey().equals(UID)){
                spotplace = new LatLng(searcher.getlatitude(),searcher.getlongitude());  //store the spot's location in spotplace
                Marker marker = (Marker) searchers.get(dataSnapshot.getKey());
                if(marker!=null){
                    Log.d(TAG,"marker exists");
                    marker.remove();
                }
                else {
                    Log.d(TAG,"adding marker");
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    searchers.put(dataSnapshot.getKey(),spotmarker);
                }
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Searcher searcher = dataSnapshot.getValue(Searcher.class);
            if(!dataSnapshot.getKey().equals(UID)){
                spotplace = new LatLng(searcher.getlatitude(),searcher.getlongitude());  //store the spot's location in spotplace
                Marker marker = (Marker) searchers.get(dataSnapshot.getKey());
                if(marker!=null){
                    Log.d(TAG,"marker exists");
                    marker.remove();
                }
                else {
                    Log.d(TAG,"adding marker");
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    searchers.put(dataSnapshot.getKey(),spotmarker);
                }
            }

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {                 //currently all these functions have been left empty
            Searcher searcher = dataSnapshot.getValue(Searcher.class);
            if(!dataSnapshot.getKey().equals(UID)){
                Marker marker = (Marker) searchers.get(dataSnapshot.getKey());
                if(marker!=null){
                    Log.d(TAG,"marker exists");
                    marker.remove();
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void insertdata(String unique, int mins,int status){
        helperDB.insertEntry(unique,mins,status);
    }

    public boolean makedecision(String unique, int mins){
        Log.d(TAG,"making decision");
        Log.d(TAG,"making decision "+unique+Integer.toString(mins));
        Cursor res = helperDB.getInfo(unique);
        res.moveToFirst();
        int status = Integer.parseInt(res.getString(res.getColumnIndex("Status")));
        Log.d(TAG,"making decision status "+Integer.toString(status));
        if(status==0) {
            int min = Integer.parseInt(res.getString(res.getColumnIndex("Time")));
            Log.d(TAG,"making decision "+Integer.toString(min));
            if (min-mins >= 2) {
                helperDB.updateStatus(unique);
                return true;
            }
        }
        if(status==1){
            return true;
        }
        return false;
    }

    public boolean checkStatus(String unique){
        Log.d(TAG,"checking status");
        Cursor res = helperDB.getInfo(unique);
        if(res.getCount() <= 0){
            res.close();
            return false;
        }
        res.moveToFirst();
        int status = Integer.parseInt(res.getString(res.getColumnIndex("Status")));
        if(status==1){
            return true;
        }
        return false;
    }


    //the getCode method that returns LatLngCodes
    public String getCode(int i,int j){
        String lats = Integer.toString(i);
        String lons = Integer.toString(j);
        if(i>=0){
            lats = "+" + lats;
        }
        if(j>=0){
            lons = "+" + lons;
        }

        return (lons+lats);
    }






}
