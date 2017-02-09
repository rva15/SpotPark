package com.app.android.sp;
//All imports
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 1/13/17.
 */
public class ReportFormFragment extends Fragment implements View.OnClickListener{

    //All imports
    private static final String TAG = "Debugger ";
    private ArrayList<Integer> time = new ArrayList<Integer>();
    private RadioGroup radioGroup1,radioGroup2;
    private RadioButton allweek,allday,choosedays,choosetimes;
    private int starthour=99,startmin=99,endhour=99,endmin=99;
    private boolean fullday=false,fullweek=false,radioflag;
    private double latitude=0.0,longitude=0.0;
    private String UID="";
    private CheckBox mon,tue,wed,thu,fri,sat,sun;
    private  DatabaseReference database;
    private Button start,end,reportspot;
    private byte[] bytearray;
    private EditText description;
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle extras = getArguments();
        UID = extras.getString("userid");       //get the user id, map image and location of the spot
        bytearray = extras.getByteArray("mapimage");
        latitude = extras.getDouble("latitude");
        longitude = extras.getDouble("longitude");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reportform, container, false); //inflate the view


        description = (EditText) view.findViewById(R.id.description);
        radioGroup1 = (RadioGroup) view.findViewById(R.id.timesgroup);
        getdays();
        getButtons();   // initialize objects
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.allday) {
                    radioflag=false;
                    fullday = true;
                }
                else if(checkedId == R.id.choosetime) {
                    fullday = false;
                    radioflag=true;
                    if(starthour==99 || startmin==99 || endhour==99 || endmin==99){
                        Toast.makeText(getContext(),"Please choose a time",Toast.LENGTH_LONG).show(); //notify user that he needs to choose a time
                    }
                }
                else {

                }

            }

        });
        radioGroup2 = (RadioGroup) view.findViewById(R.id.daysgroup);
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.allweek) {
                    fullweek = true;
                }
                else if(checkedId == R.id.choosedays) {
                    fullweek = false;
                }
                else {

                }

            }

        });

        allday = (RadioButton) view.findViewById(R.id.allday);
        allweek = (RadioButton) view.findViewById(R.id.allweek);
        choosedays = (RadioButton) view.findViewById(R.id.choosedays);
        choosetimes = (RadioButton) view.findViewById(R.id.choosetime);
        reportspot = (Button) view.findViewById(R.id.reportspot);
        reportspot.setOnClickListener(this);
        allday.setOnClickListener(this);
        allweek.setOnClickListener(this);
        choosedays.setOnClickListener(this);
        choosetimes.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //fetch information from the dialog
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==7) {

            Bundle bundle = data.getExtras();
            time.add(bundle.getInt("hour"));
            time.add(bundle.getInt("mins"));
            printtime();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.chooseStart){
            showStartDialog();
        }
        if(v.getId()==R.id.chooseEnd){
            showEndDialog();
        }
        if(v.getId()==R.id.allweek){
            deactivateDays();
        }
        if(v.getId()==R.id.allday){
            deactivateButtons();
        }
        if(v.getId()==R.id.choosedays){
            activateDays();
        }
        if(v.getId()==R.id.choosetime){
            activateButtons();
        }
        if(v.getId()==R.id.reportspot){
            reportspot();
        }
    }

    private void showStartDialog() {
        // Create an instance of the dialog fragment and show it
        time.add(123);
        DialogFragment dialog = new ReportFormDialog();
        dialog.setTargetFragment(ReportFormFragment.this, 7);       //
        dialog.show(this.getActivity().getSupportFragmentManager(),"ReportFragment");

    }

    private void showEndDialog() {
        // Create an instance of the dialog fragment and show it
        time.add(243);
        DialogFragment dialog = new ReportFormDialog();
        dialog.setTargetFragment(ReportFormFragment.this, 7);
        dialog.show(this.getActivity().getSupportFragmentManager(),"ReportFragment");

    }

    // function that prints user selected times on screen
    private void printtime(){
        for(int i=0;i<time.size();i++){
            if(time.get(i)>=0 && time.get(i)<=24){
                if(time.get(i-1)==123.){
                    if(time.get(i)>=0 && time.get(i)<=12) {
                        TextView t = (TextView) view.findViewById(R.id.starttime);
                        if(time.get(i+1)==0) {
                            t.setText(Integer.toString(time.get(i)) + ":" + Integer.toString(time.get(i + 1))+"0 am");
                        }
                        else{
                            t.setText(Integer.toString(time.get(i)) + ":" + Integer.toString(time.get(i + 1))+" am");
                        }
                        starthour = time.get(i);
                        startmin = time.get(i + 1);
                    }
                    if(time.get(i)>12 && time.get(i)<=24) {
                        int hour=time.get(i)-12;
                        TextView t = (TextView) view.findViewById(R.id.starttime);
                        if(time.get(i+1)==0) {
                            t.setText(Integer.toString(hour) + ":" + Integer.toString(time.get(i + 1))+"0 pm");
                        }
                        else{
                            t.setText(Integer.toString(hour) + ":" + Integer.toString(time.get(i + 1))+" pm");
                        }
                        starthour = time.get(i);
                        startmin = time.get(i + 1);
                    }

                }
                else if(time.get(i-1)==243.){
                    if(time.get(i)>=0 && time.get(i)<=12) {
                        TextView t = (TextView) view.findViewById(R.id.endtime);
                        if(time.get(i+1)==0) {
                            t.setText(Integer.toString(time.get(i)) + ":" + Integer.toString(time.get(i + 1))+"0 am");
                        }
                        else{
                            t.setText(Integer.toString(time.get(i)) + ":" + Integer.toString(time.get(i + 1))+" am");
                        }
                        endhour = time.get(i);
                        endmin = time.get(i + 1);
                    }
                    if(time.get(i)>12 && time.get(i)<=24) {
                        int hour=time.get(i)-12;
                        TextView t = (TextView) view.findViewById(R.id.endtime);
                        if(time.get(i+1)==0) {
                            t.setText(Integer.toString(hour) + ":" + Integer.toString(time.get(i + 1))+"0 pm");
                        }
                        else{
                            t.setText(Integer.toString(hour) + ":" + Integer.toString(time.get(i + 1))+" pm");
                        }
                        endhour = time.get(i);
                        endmin = time.get(i + 1);
                    }
                }
            }
        }
    }


    private void getdays(){
        mon = (CheckBox) view.findViewById(R.id.mon);
        mon.setEnabled(false);
        tue = (CheckBox) view.findViewById(R.id.tue);
        tue.setEnabled(false);
        wed = (CheckBox) view.findViewById(R.id.wed);
        wed.setEnabled(false);
        thu = (CheckBox) view.findViewById(R.id.thu);
        thu.setEnabled(false);
        fri = (CheckBox) view.findViewById(R.id.fri);
        fri.setEnabled(false);
        sat = (CheckBox) view.findViewById(R.id.sat);
        sat.setEnabled(false);
        sun = (CheckBox) view.findViewById(R.id.sun);
        sun.setEnabled(false);
    }

    private void activateDays(){
        mon.setEnabled(true);
        tue.setEnabled(true);
        wed.setEnabled(true);
        thu.setEnabled(true);
        fri.setEnabled(true);
        sat.setEnabled(true);
        sun.setEnabled(true);
    }

    private void deactivateDays(){
        mon.setEnabled(false);
        tue.setEnabled(false);
        wed.setEnabled(false);
        thu.setEnabled(false);
        fri.setEnabled(false);
        sat.setEnabled(false);
        sun.setEnabled(false);
    }

    private void getButtons(){
        start = (Button) view.findViewById(R.id.chooseStart);
        start.setOnClickListener(this);
        start.setEnabled(false);
        end   = (Button) view.findViewById(R.id.chooseEnd);
        end.setOnClickListener(this);
        end.setEnabled(false);
    }

    private void activateButtons(){
        start.setEnabled(true);
        end.setEnabled(true);
    }

    private void deactivateButtons(){
        start.setEnabled(false);
        end.setEnabled(false);
    }

    private void reportspot(){
        if(radioflag==true) {
            if (starthour == 99 || startmin == 99 || endhour == 99 || endmin == 99) {
                Toast.makeText(this.getContext(), "Please choose a time", Toast.LENGTH_LONG).show();
                return;
            }
        }
        database = FirebaseDatabase.getInstance().getReference();
        String LatLngCode = getLatLngCode(latitude,longitude);
        ReportedTimes reportedTimes = new ReportedTimes(latitude,longitude,0,fullday,starthour,startmin,endhour,endmin,fullweek,mon.isChecked(),tue.isChecked(),wed.isChecked(),
                thu.isChecked(),fri.isChecked(),sat.isChecked(),sun.isChecked(),description.getText().toString(),LatLngCode);
        Map<String,Object> reportedTimesMap = reportedTimes.toMap();
        String key = database.child("ReportedDetails/"+LatLngCode).push().getKey();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/ReportedDetails/"+LatLngCode+"/"+key,UID);
        childUpdates.put("/ReportedTimes/"+UID+"/"+key,reportedTimesMap);
        database.updateChildren(childUpdates);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
        StorageReference historyRef = storageRef.child(UID+"/Reported/"+key+".jpg");

        UploadTask uploadTask = historyRef.putBytes(bytearray);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });

        showPostReport(bytearray);

    }

    private void showPostReport(byte[] bytearray){
        HomeScreenActivity homeScreenActivity = (HomeScreenActivity) this.getActivity();
        homeScreenActivity.getPostReport(bytearray);
    }

    private String getLatLngCode(double lat, double lon){

        lat = lat*100;     //get the centi latitudes and centi longitudes
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

}
