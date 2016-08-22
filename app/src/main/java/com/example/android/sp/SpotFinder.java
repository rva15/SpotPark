package com.example.android.sp;
//Necassary imports
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
import java.util.List;


/**
 * Created by ruturaj on 8/19/16.
 */

public class SpotFinder {
    //Necassary variable declaration
    double latitude=0.0,longitude=0.0;
    private DatabaseReference database;
    private static final String TAG = "Debugger ";
    ArrayList<String> array = new ArrayList<String>();
    LatLng spotplace;
    Marker spotmarker;
    private GoogleMap searchmap;
    int i=0;

    public SpotFinder(){};  //empty constructor

    public SpotFinder(double latitude, double longitude, GoogleMap searchmap){  //contructor receives parameters
        this.latitude=latitude;
        this.longitude=longitude;
        this.searchmap=searchmap;

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
            }
        }

        for(int k=0;k<9;k++){
            database.child("CheckInKeys").child(array.get(k)).addChildEventListener(listener1); //add listeners to corresponding nodes in the database
        }

    }


    //define the ChildEventListener
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "somesh : " + dataSnapshot.getKey() );
            CheckInDetails details = dataSnapshot.getValue(CheckInDetails.class);  //retrieve a snapshot from the node and store it in CheckInDetails.class

            spotplace = new LatLng(details.getlatitude(),details.getlongitude());  //store the spot's location in spotplace
            //add a blue marker at the spot
            spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

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
