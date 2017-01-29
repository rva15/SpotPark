package com.example.android.sp;
//All imports
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Created by ruturaj on 8/19/16.
 */

public class SpotFinder {

    //Variable Declaration
    private double latitude=0.0,longitude=0.0;
    private String UID="",currtime;
    private int currhour,currmin;
    private DatabaseReference database;
    private static final String TAG = "Debugger ";
    private ArrayList<String> array = new ArrayList<String>();
    private LatLng spotplace;
    private Marker spotmarker;
    private GoogleMap searchmap;
    private SearchHelperDB helperDB;
    private Map markers = new HashMap();
    private Map reportcat = new HashMap();
    private Map reportdesc = new HashMap();
    private Map searchers = new HashMap();
    private Map chintimes = new HashMap();
    private Map chinkeys = new HashMap();
    private Map uidkey   = new HashMap();
    private SimpleDateFormat dayFormat,simpleDateFormat;
    private ArrayList<Integer> markerimage = new ArrayList<Integer>();
    private ArrayList<Bitmap>  markerbitmaps = new ArrayList<>();
    private com.google.firebase.database.Query getReported;
    private Calendar calendar;
    private Context context;



    public SpotFinder(){};  //empty constructor

    public SpotFinder(double latitude, double longitude, GoogleMap searchmap,String id,Context context){  //contructor receives parameters
        this.latitude=latitude;
        this.longitude=longitude;
        this.searchmap=searchmap;
        this.UID = id;
        this.context = context;
        helperDB = new SearchHelperDB(SPApplication.getContext());     //get context and initialize phone db
        dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault()); //format for day
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");           //format for time

        //Add all minute markers to markerimage array
        markerimage.add(R.drawable.marker0);
        markerimage.add(R.drawable.marker1);
        markerimage.add(R.drawable.marker2);
        markerimage.add(R.drawable.marker3);
        markerimage.add(R.drawable.marker4);
        markerimage.add(R.drawable.marker5);
        markerimage.add(R.drawable.marker6);
        markerimage.add(R.drawable.marker7);
        markerimage.add(R.drawable.marker8);
        markerimage.add(R.drawable.marker9);
        markerimage.add(R.drawable.marker10);

        //make the markers the right size
        for(int i=0;i<markerimage.size();i++){
            BitmapDrawable bitmapdraw=(BitmapDrawable)context.getResources().getDrawable(markerimage.get(i),null);
            Bitmap b=bitmapdraw.getBitmap();
            markerbitmaps.add(Bitmap.createScaledBitmap(b, dpToPx(40), dpToPx(40), false));
        }

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
            database.child("CheckInKeys").child(array.get(k)).addChildEventListener(listener1); //add listener1 for checkin spots
            //database.child("Searchers").child(array.get(k)).addChildEventListener(listener2);   //add listener2 for other searchers
            database.child("ReportedDetails").child(array.get(k)).addChildEventListener(listener3);//add listener3 for reported spots
        }

    }


    //define the ChildEventListener added to checkinkeys
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            CheckInDetails details = dataSnapshot.getValue(CheckInDetails.class);  //retrieve a snapshot from the node and store it in CheckInDetails.class

            spotplace = new LatLng(details.getlatitude(),details.getlongitude());  //get location of spot
            int time = details.getminstoleave();                                   //and the mins to leave
            int dollars = details.getdollars();
            int cents = details.getcents();
            chintimes.put(spotplace,time);                                         //map the 'time to leave' to the place
            chinkeys.put(spotplace,dataSnapshot.getKey());                         //map the checkinkey to the place
            if (time>10){
                //do nothing
            }
            if(time<=2){
                beServerCheckIn(details,dataSnapshot.getKey());
                insertdata(dataSnapshot.getKey(),time,1,dollars,cents);           //insert entry in local db and make it active
                Marker marker = (Marker) markers.get(spotplace);                  //get the marker that sits at the spotplace
                if(marker!=null){                                           //check if marker already exists at the place
                    marker.remove();                                        //remove the old marker and add the timed marker
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.fromBitmap(markerbitmaps.get(time))));
                    markers.put(spotplace,spotmarker);
                }
                else {
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.fromBitmap(markerbitmaps.get(time))));
                    markers.put(spotplace,spotmarker);    //else put a timed marker and map it to the place

                }
            }
            if(time>2 && time<=10){

                if(checkStatus(dataSnapshot.getKey())){                                     //check if spot is already active
                    beServerCheckIn(details,dataSnapshot.getKey());
                    spotplace = new LatLng(details.getlatitude(),details.getlongitude());   //store the spot's location in spotplace
                    Marker marker = (Marker) markers.get(spotplace);
                    if(marker!=null){       //then check if there is a marker at the spot
                        marker.remove();    //if yes then remove the old marker and add a new timed marker
                        spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.fromBitmap(markerbitmaps.get(time))));
                        markers.put(spotplace,spotmarker);
                    }
                    else {
                        spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.fromBitmap(markerbitmaps.get(time))));
                        markers.put(spotplace, spotmarker);      //else put a marker and map it to spot
                    }

                }
                else { //if status is inactive
                    insertdata(dataSnapshot.getKey(), time, 0,dollars,cents);         //make an entry in local db and mark it inactive
                }
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            CheckInDetails details = dataSnapshot.getValue(CheckInDetails.class);      //get value of the changed spot detail
            spotplace = new LatLng(details.getlatitude(),details.getlongitude());
            int time = details.getminstoleave();
            int dollars = details.getdollars();                         //get all details about the changed spot
            int cents = details.getcents();
            chintimes.put(spotplace,time);                             //update the new time in the map
            if(makedecision(dataSnapshot.getKey(),details.getminstoleave(),dollars,cents)){ //make a decision whether or not that spot is now active
                spotplace = new LatLng(details.getlatitude(),details.getlongitude());  //store the spot's location in spotplace
                Marker marker = (Marker) markers.get(spotplace);                       //decision is positive so add a timed marker
                if(marker!=null){
                    marker.remove();
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.fromBitmap(markerbitmaps.get(time))));
                    markers.put(spotplace,spotmarker);
                }
                else {
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.fromBitmap(markerbitmaps.get(time))));
                    markers.put(spotplace,spotmarker);    //add a marker and map it if it doesn't exist already
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {    //remove marker from the map when a checkin is deleted
            CheckInDetails checkInDetails = dataSnapshot.getValue(CheckInDetails.class);
            spotplace = new LatLng(checkInDetails.getlatitude(),checkInDetails.getlongitude());
            Marker marker = (Marker)markers.get(spotplace);
            if(marker!=null){
                marker.remove();
            }

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //define the ChildEventListener to get to listener4
    ChildEventListener listener3 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String userid = dataSnapshot.getValue(String.class);
            uidkey.put(dataSnapshot.getKey(),userid);            //map these to each other for reported server function
            getReported = database.child("ReportedTimes").child(userid).orderByKey().equalTo(dataSnapshot.getKey());
            getReported.addChildEventListener(listener4);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

    //define the ChildEventListener for Reported Spots
    ChildEventListener listener4 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            ReportedTimes times = dataSnapshot.getValue(ReportedTimes.class);
            beServerRep(times,dataSnapshot.getKey());
            if (analyzeReported(times)) { //see if the reported spot's timings match present time and day
                spotplace = new LatLng(times.getlatitude(),times.getlongitude());
                Marker marker = (Marker) markers.get(spotplace);
                if (marker != null) {
                } else {
                    chinkeys.put(spotplace,dataSnapshot.getKey());   //map the reported spot's key to the place
                    if (times.getverification() > 1) {
                        spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.fromResource(R.drawable.repver)));
                        markers.put(spotplace, spotmarker);    //add a marker and map it if it doesn't exist already
                        reportcat.put(spotplace, true);        // put 'true' in category specifying that it is verified
                        reportdesc.put(spotplace,times.getdescription());
                    } else {
                        spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.fromResource(R.drawable.repunver)));
                        markers.put(spotplace, spotmarker);    //add a marker and map it if it doesn't exist already
                        reportcat.put(spotplace, false);       //put 'false' in category specifying it is unverified
                        reportdesc.put(spotplace,times.getdescription());
                    }
                }

            }
            if (!analyzeReported(times)) {
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {    //currently all these functions have been left empty
            ReportedTimes reportedtimes = dataSnapshot.getValue(ReportedTimes.class);
            spotplace = new LatLng(reportedtimes.getlatitude(),reportedtimes.getlongitude());
            Marker marker = (Marker)markers.get(spotplace);
            if(marker!=null){
                marker.remove();
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //define the ChildEventListener for searchers
    ChildEventListener listener2 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            if(dataSnapshot.exists()) {    //check if any searcher exists
                Searcher searcher = dataSnapshot.getValue(Searcher.class);
                if (!dataSnapshot.getKey().equals(UID)) {                                     //check if the searcher is not himself
                    spotplace = new LatLng(searcher.getlatitude(), searcher.getlongitude());  //store the spot's location in spotplace
                    Marker marker = (Marker) searchers.get(dataSnapshot.getKey());
                    if (marker != null) {
                    } else {
                        spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                        searchers.put(dataSnapshot.getKey(), spotmarker);   //put a marker at searchers spot and add to map
                    }
                }
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) { //probably triggered when a searcher's location changes
            Searcher searcher = dataSnapshot.getValue(Searcher.class);
            if(!dataSnapshot.getKey().equals(UID)){  //again check if it is not the user himself
                spotplace = new LatLng(searcher.getlatitude(),searcher.getlongitude());  //store the spot's location in spotplace
                Marker marker = (Marker) searchers.get(dataSnapshot.getKey());
                if(marker!=null){
                    marker.remove();            //remove previous marker belonging to the searcher
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    searchers.put(dataSnapshot.getKey(),spotmarker);        //add marker at the new place
                }
                else {
                    spotmarker = searchmap.addMarker(new MarkerOptions().position(spotplace).title("spot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    searchers.put(dataSnapshot.getKey(),spotmarker);        //add marker at the place
                }
            }

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Searcher searcher = dataSnapshot.getValue(Searcher.class);
            if(!dataSnapshot.getKey().equals(UID)){
                Marker marker = (Marker) searchers.get(dataSnapshot.getKey());
                if(marker!=null){
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

    //public functions to pass these maps to other classes
    public Map getKeys(){
        return chinkeys;
    }
    public Map getTimes(){return chintimes;}
    public Map getCats(){return reportcat;}
    public Map getDesc(){return reportdesc;}

    private void beServerCheckIn(CheckInDetails checkInDetails,String key){
        String updatedate = checkInDetails.getupdatedate();
        int updatehour = checkInDetails.getupdatehour();
        int updatemin  = checkInDetails.getupdatemin();
        if(removeCheckIn(updatedate,updatehour,updatemin)){
            Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
            for(int k=0;k<9;k++){
                childUpdates.put("/CheckInKeys/"+array.get(k)+"/"+key,null);
            }
            database.updateChildren(childUpdates);
        }

    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private boolean removeCheckIn(String updatedate,int updatehour,int updatemin){
        calendar = Calendar.getInstance();                    //get current time
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd "); //also get current date in this format
        String strDate = mdformat.format(calendar.getTime());
        if(!strDate.equals(updatedate)){
           return true;
        }
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min  = calendar.get(Calendar.MINUTE);
        int currenttime = hour*60 + min;
        int updatetime  = updatehour*60 + updatemin;
        if(currenttime-updatetime>10){
            return true;
        }
        return false;
    }

    private void beServerRep(ReportedTimes reportedtimes,String key){
        if(reportedtimes.getverification()<(-1)){
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/ReportedTimes/"+uidkey.get(key)+"/"+key,null);
            for(int k=0;k<9;k++){
                childUpdates.put("/ReportedDetails/"+array.get(k)+"/"+key,null);
            }
            database.updateChildren(childUpdates);
        }
    }

    //function to detach all listeners
    public void detachListeners(){
        for(int k=0;k<9;k++){
            database.child("CheckInKeys").child(array.get(k)).removeEventListener(listener1); //remove listener1 for checkin spots
            //database.child("Searchers").child(array.get(k)).removeEventListener(listener2);   //remove listener2 for other searchers
            database.child("ReportedDetails").child(array.get(k)).removeEventListener(listener3);//remove listener3 for reported spots
        }
        if(getReported!=null) {
            getReported.removeEventListener(listener4);
        }
    }

    private void insertdata(String unique, int mins,int status,int dollar,int cent){
        helperDB.insertEntry(unique,mins,status,dollar,cent);                   //insert entry into localdb
    }

    private boolean makedecision(String unique, int mins,int dollar,int cent){ //make decision of whether spot is now active
        Cursor res = helperDB.getInfo(unique);
        if(res.getCount() <= 0){
            res.close();
            insertdata(unique, mins, 0,dollar,cent);  //if there is no such entry in db, add one and mark it inactive
            return false;                             //return negative
        }
        res.moveToFirst();
        int status = Integer.parseInt(res.getString(res.getColumnIndex("Status"))); //get the status entry from db
        if(status==0) {  //if status shows inactive
            int min = Integer.parseInt(res.getString(res.getColumnIndex("Time")));  //get previous mins to leave
            if (min-mins >= 2) {
                helperDB.updateStatus(unique);   //if time difference is >=2, change spot to active
                return true;
            }
        }
        if(status==1){   //return positive if spot is already active
            return true;
        }
        return false;
    }

    private boolean checkStatus(String unique){   //check status of spot directly
        Cursor res = helperDB.getInfo(unique);
        if(res.getCount() <= 0){
            res.close();
            return false;       //no such entry and return inactive
        }
        res.moveToFirst();
        int status = Integer.parseInt(res.getString(res.getColumnIndex("Status")));
        if(status==1){
            return true;  //entry exists and is active
        }
        return false;    //entry exists and is inactive
    }


    //the getCode method that returns LatLngCodes
    private String getCode(int i,int j){
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

    private boolean analyzeReported(ReportedTimes reportedTimes){
        Calendar calendar = Calendar.getInstance();
        currtime = simpleDateFormat.format(calendar.getTime());  //convert time into desirable format
        String[] timearray = currtime.split(":");               //split the time into hours and mins
        currhour = Integer.parseInt(timearray[0]);
        currmin = Integer.parseInt(timearray[1]);
        String weekDay = dayFormat.format(calendar.getTime());  //get day of the week
        if(reportedTimes.getfullweek()==true && reportedTimes.getfullday()==true){
            return true;
        }
        if(reportedTimes.getfullweek()==true && reportedTimes.getfullday()==false){
            int starthours = reportedTimes.getstarthours();
            int startmins  = reportedTimes.getstartmins();
            int endhours   = reportedTimes.getendhours();
            int endmins    = reportedTimes.getendmins();
            int realstart = starthours*60+startmins;
            int realend   = endhours*60+endmins;
            int curr      = currhour*60+currmin;
            if(realstart<realend){
                if(curr>realstart && curr<realend){
                    return true;
                }
            }
            if(realstart>realend){
                if(curr<realstart && curr>realend){
                    return false;
                }
                else{
                    return true;
                }
            }

        }
        if(reportedTimes.getfullweek()==false){
            if(weekDay.equals("Monday")){
                if(reportedTimes.getmon()==true){
                    if(reportedTimes.getfullday()==true){
                        return true;
                    }
                    else if(reportedTimes.getfullday()==false){
                        int starthours = reportedTimes.getstarthours();
                        int startmins  = reportedTimes.getstartmins();
                        int endhours   = reportedTimes.getendhours();
                        int endmins    = reportedTimes.getendmins();
                        int realstart = starthours*60+startmins;
                        int realend   = endhours*60+endmins;
                        int curr      = currhour*60+currmin;
                        if(realstart<realend){
                            if(curr>realstart && curr<realend){
                                return true;
                            }
                        }
                        if(realstart>realend){
                            if(curr<realstart && curr>realend){
                                return false;
                            }
                            else{
                                return true;
                            }
                        }
                    }
                }

            }
            if(weekDay.equals("Tuesday")){
                if(reportedTimes.gettue()==true){
                    if(reportedTimes.getfullday()==true){
                        return true;
                    }
                    else if(reportedTimes.getfullday()==false){
                        int starthours = reportedTimes.getstarthours();
                        int startmins  = reportedTimes.getstartmins();
                        int endhours   = reportedTimes.getendhours();
                        int endmins    = reportedTimes.getendmins();
                        int realstart = starthours*60+startmins;
                        int realend   = endhours*60+endmins;
                        int curr      = currhour*60+currmin;
                        if(realstart<realend){
                            if(curr>realstart && curr<realend){
                                return true;
                            }
                        }
                        if(realstart>realend){
                            if(curr<realstart && curr>realend){
                                return false;
                            }
                            else{
                                return true;
                            }
                        }
                    }
                }

            }
            if(weekDay.equals("Wednesday")){
                if(reportedTimes.getwed()==true){
                    if(reportedTimes.getfullday()==true){
                        return true;
                    }
                    else if(reportedTimes.getfullday()==false){
                        int starthours = reportedTimes.getstarthours();
                        int startmins  = reportedTimes.getstartmins();
                        int endhours   = reportedTimes.getendhours();
                        int endmins    = reportedTimes.getendmins();
                        int realstart = starthours*60+startmins;
                        int realend   = endhours*60+endmins;
                        int curr      = currhour*60+currmin;
                        if(realstart<realend){
                            if(curr>realstart && curr<realend){
                                return true;
                            }
                        }
                        if(realstart>realend){
                            if(curr<realstart && curr>realend){
                                return false;
                            }
                            else{
                                return true;
                            }
                        }
                    }
                }

            }
            if(weekDay.equals("Thursday")){
                if(reportedTimes.getthu()==true){
                    if(reportedTimes.getfullday()==true){
                        return true;
                    }
                    else if(reportedTimes.getfullday()==false){
                        int starthours = reportedTimes.getstarthours();
                        int startmins  = reportedTimes.getstartmins();
                        int endhours   = reportedTimes.getendhours();
                        int endmins    = reportedTimes.getendmins();
                        int realstart = starthours*60+startmins;
                        int realend   = endhours*60+endmins;
                        int curr      = currhour*60+currmin;
                        if(realstart<realend){
                            if(curr>realstart && curr<realend){
                                return true;
                            }
                        }
                        if(realstart>realend){
                            if(curr<realstart && curr>realend){
                                return false;
                            }
                            else{
                                return true;
                            }
                        }
                    }
                }

            }
            if(weekDay.equals("Friday")){
                if(reportedTimes.getfri()==true){
                    if(reportedTimes.getfullday()==true){
                        return true;
                    }
                    else if(reportedTimes.getfullday()==false){
                        int starthours = reportedTimes.getstarthours();
                        int startmins  = reportedTimes.getstartmins();
                        int endhours   = reportedTimes.getendhours();
                        int endmins    = reportedTimes.getendmins();
                        int realstart = starthours*60+startmins;
                        int realend   = endhours*60+endmins;
                        int curr      = currhour*60+currmin;
                        if(realstart<realend){
                            if(curr>realstart && curr<realend){
                                return true;
                            }
                        }
                        if(realstart>realend){
                            if(curr<realstart && curr>realend){
                                return false;
                            }
                            else{
                                return true;
                            }
                        }
                    }
                }

            }
            if(weekDay.equals("Saturday")){
                if(reportedTimes.getsat()==true){
                    if(reportedTimes.getfullday()==true){
                        return true;
                    }
                    else if(reportedTimes.getfullday()==false){
                        int starthours = reportedTimes.getstarthours();
                        int startmins  = reportedTimes.getstartmins();
                        int endhours   = reportedTimes.getendhours();
                        int endmins    = reportedTimes.getendmins();
                        int realstart = starthours*60+startmins;
                        int realend   = endhours*60+endmins;
                        int curr      = currhour*60+currmin;
                        if(realstart<realend){
                            if(curr>realstart && curr<realend){
                                return true;
                            }
                        }
                        if(realstart>realend){
                            if(curr<realstart && curr>realend){
                                return false;
                            }
                            else{
                                return true;
                            }
                        }
                    }
                }

            }
            if(weekDay.equals("Sunday")){
                if(reportedTimes.getsun()==true){
                    if(reportedTimes.getfullday()==true){
                        return true;
                    }
                    else if(reportedTimes.getfullday()==false){
                        int starthours = reportedTimes.getstarthours();
                        int startmins  = reportedTimes.getstartmins();
                        int endhours   = reportedTimes.getendhours();
                        int endmins    = reportedTimes.getendmins();
                        int realstart = starthours*60+startmins;
                        int realend   = endhours*60+endmins;
                        int curr      = currhour*60+currmin;
                        if(realstart<realend){
                            if(curr>realstart && curr<realend){
                                return true;
                            }
                        }
                        if(realstart>realend){
                            if(curr<realstart && curr>realend){
                                return false;
                            }
                            else{
                                return true;
                            }
                        }
                    }
                }

            }
        }
        return false;
    }
 }


