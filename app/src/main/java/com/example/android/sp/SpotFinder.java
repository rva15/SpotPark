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
import java.util.Iterator;
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
    Map chintimes = new HashMap();
    Map chinkeys = new HashMap();

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
            database.child("CheckInKeys").child(array.get(k)).addChildEventListener(listener1); //add listener1 for checkin spots
            database.child("Searchers").child(array.get(k)).addChildEventListener(listener2);   //add listener2 for other searchers
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
            spotplace = new LatLng(details.getlatitude(),details.getlongitude());  //get location of spot
            int time = details.getminstoleave();                                   //and the mins to leave
            int dollars = details.getdollars();
            int cents = details.getcents();
            chintimes.put(spotplace,time);                                         //map the time to the place
            chinkeys.put(spotplace,dataSnapshot.getKey());                         //map the checkinkey to the place
            if (time>10){
                Log.d(TAG,"time greater than 10");
                //do nothing
            }
            if(time<=2){
                insertdata(dataSnapshot.getKey(),time,1,dollars,cents);                             //insert entry in local db and make it active
                spotplace = new LatLng(details.getlatitude(),details.getlongitude());  //store the spot's location in spotplace

                Marker marker = (Marker) markers.get(spotplace);
                if(marker!=null){
                    Log.d(TAG,"marker exists");                             //check if marker already exists at the place

                }
                else {
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    markers.put(spotplace,spotmarker);    //else put a marker and map it to the place

                }
            }
            if(time>2 && time<=10){
                Log.d(TAG,"inserting data");
                if(checkStatus(dataSnapshot.getKey())){                                     //check if spot is already active
                    spotplace = new LatLng(details.getlatitude(),details.getlongitude());  //store the spot's location in spotplace
                    Marker marker = (Marker) markers.get(spotplace);
                    if(marker!=null){
                        Log.d(TAG,"marker exists");                                        //then check if there is a marker at the spot

                    }
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    markers.put(spotplace,spotmarker);          //else put a marker and map it to spot

                }
                else {
                    insertdata(dataSnapshot.getKey(), time, 0,dollars,cents);         //make an entry in local db and mark it inactive

                }
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG,"onchildchanged fired");
            CheckInDetails details = dataSnapshot.getValue(CheckInDetails.class);      //get value of the changed spot detail
            spotplace = new LatLng(details.getlatitude(),details.getlongitude());
            int time = details.getminstoleave();
            int dollars = details.getdollars();
            int cents = details.getcents();
            chintimes.put(spotplace,time);                                            //update the new time in the map
            if(makedecision(dataSnapshot.getKey(),details.getminstoleave(),dollars,cents)){         //make a decision if that spot is now active
                Log.d(TAG,"decision positive");
                spotplace = new LatLng(details.getlatitude(),details.getlongitude());  //store the spot's location in spotplace

                Marker marker = (Marker) markers.get(spotplace);
                if(marker!=null){
                    Log.d(TAG,"marker exists");
                }
                else {
                    Log.d(TAG,"adding marker");
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    markers.put(spotplace,spotmarker);    //add a marker and map it if it doesn't exist already
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {    //currently all these functions have been left empty

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

            if(dataSnapshot.exists()) {    //check if any searcher exists
                Searcher searcher = dataSnapshot.getValue(Searcher.class);
                if (!dataSnapshot.getKey().equals(UID)) {
                    spotplace = new LatLng(searcher.getlatitude(), searcher.getlongitude());  //store the spot's location in spotplace
                    Marker marker = (Marker) searchers.get(dataSnapshot.getKey());
                    if (marker != null) {
                        Log.d(TAG, "marker exists");
                        marker.remove();
                    } else {
                        Log.d(TAG, "adding marker");
                        spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                        searchers.put(dataSnapshot.getKey(), spotmarker);   //put a marker at searchers spot and add to map
                    }
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
                    marker.remove();            //remove marker belonging to the guy
                }
                else {
                    Log.d(TAG,"adding marker");
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    searchers.put(dataSnapshot.getKey(),spotmarker);        //add marker at the new place
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
                    marker.remove();                //remove marker when searcher quits search
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


    public Map getKeys(){
        return chinkeys;
    }

    public void insertdata(String unique, int mins,int status,int dollar,int cent){
        helperDB.insertEntry(unique,mins,status,dollar,cent);                   //insert entry into localdb
    }

    public boolean makedecision(String unique, int mins,int dollar,int cent){
        Log.d(TAG,"making decision");
        Log.d(TAG,"making decision "+unique+Integer.toString(mins));
        Cursor res = helperDB.getInfo(unique);
        if(res.getCount() <= 0){
            res.close();
            insertdata(unique, mins, 0,dollar,cent);  //if there is no such entry in db, add one and mark it inactive
            return false;
        }
        res.moveToFirst();
        int status = Integer.parseInt(res.getString(res.getColumnIndex("Status")));
        Log.d(TAG,"making decision status "+Integer.toString(status));
        if(status==0) {
            int min = Integer.parseInt(res.getString(res.getColumnIndex("Time")));
            Log.d(TAG,"making decision "+Integer.toString(min));
            if (min-mins >= 2) {
                helperDB.updateStatus(unique);   //if time difference is >=2, change spot to active
                return true;
            }
        }
        if(status==1){
            return true;
        }
        return false;
    }

    public boolean checkStatus(String unique){   //check status of spot
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

    public void deletenearest(double x,double y,String LatLngCode ){
        Log.d(TAG,"delete nearest "+Double.toString(x));
        Log.d(TAG,"delete nearest "+Double.toString(y));
        Iterator it = markers.entrySet().iterator();
        int c = 0;
        String key ="";
        LatLng l= new LatLng(0,0);
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            l = (LatLng)pair.getKey();
            Log.d(TAG,"delete nearest "+Double.toString(l.latitude));
            Log.d(TAG,"delete nearest "+Double.toString(l.longitude));
            double dx = 10000*Math.abs(x-l.latitude);
            double dy = 10000*Math.abs(y-l.longitude);
            if(dx<6 && dy<6){
                key = (String) chinkeys.get(l);
                c=c+1;
            }
        }
        if(c==1){
            int t = (int)chintimes.get(l);
            if (t<=1) {
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/CheckInKeys/"+LatLngCode+"/"+key, null);
                database = FirebaseDatabase.getInstance().getReference();   //get Firebase reference
                database.updateChildren(childUpdates);
                Log.d(TAG, "delete nearest key " + key);
                //delete the entry
            }
        }
        Log.d(TAG,"delete nearest c "+Integer.toString(c));

    }






}
