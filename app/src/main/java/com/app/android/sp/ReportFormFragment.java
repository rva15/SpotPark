package com.app.android.sp;
//All imports
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
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

import static com.app.android.sp.R.id.remindmsg;
import static com.app.android.sp.R.id.start;

/**
 * Created by ruturaj on 1/13/17.
 */
public class ReportFormFragment extends Fragment implements View.OnClickListener{

    //All imports
    private static final String TAG = "Debugger ";
    private RadioGroup radioGroup1,radioGroup2;
    private RadioButton allweek,allday,choosedays,choosetimes;
    private int starthour=0,startmin=0,endhour=0,endmin=0;
    private boolean fullday=false,fullweek=false,radioflag,pickedstart=false,pickedend=false;
    private double latitude=0.0,longitude=0.0;
    private String UID="";
    private CheckBox mon,tue,wed,thu,fri,sat,sun;
    private  DatabaseReference database;
    private Button start,end,reportspot;
    private byte[] bytearray;
    private EditText description;
    private View view;
    private TextView starttime,endtime;

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
        setButtonsState(false);
        setDaysState(false);
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.allday) {
                    radioflag=false;
                    fullday = true;
                    setButtonsState(false);
                }
                else if(checkedId == R.id.choosetime) {
                    fullday = false;
                    radioflag=true;
                    setButtonsState(true);
                    if(!(pickedstart & pickedend)){
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
                    setDaysState(false);
                }
                else if(checkedId == R.id.choosedays) {
                    fullweek = false;
                    setDaysState(true);
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
        starttime = (TextView)view.findViewById(R.id.starttime);
        endtime = (TextView)view.findViewById(R.id.endtime);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.chooseStart){
            showStartDialog();
        }
        if(v.getId()==R.id.chooseEnd){
            showEndDialog();
        }
        if(v.getId()==R.id.reportspot){
            reportspot();
        }
    }

    private void showStartDialog() {
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                pickedstart = true;
                starthour = selectedHour;
                startmin = selectedMinute;
                printtime(starthour,startmin,endhour,endmin);

            }
        }, starthour,startmin, false);
        mTimePicker.setTitle("Pick a start time");
        mTimePicker.show();

    }

    private void showEndDialog() {
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                pickedend = true;
                endhour = selectedHour;
                endmin = selectedMinute;
                printtime(starthour,startmin,endhour,endmin);

            }
        }, endhour, endmin, false);
        mTimePicker.setTitle("Pick an end time");
        mTimePicker.show();

    }

    // function that prints user selected times on screen
    private void printtime(int starthour,int startmin,int endhour,int endmin){
        //set start time text
        String stime,etime;
        if(starthour<12) {
            if(startmin<10) {
                stime = Integer.toString(starthour) + ":0" + Integer.toString(startmin) + "am";
            }
            else{
                stime = Integer.toString(starthour) + ":" + Integer.toString(startmin) + "am";
            }
        }
        else if(starthour==12){
            if(startmin<10) {
                stime = Integer.toString(starthour) + ":0" + Integer.toString(startmin) + "pm";
            }
            else{
                stime = Integer.toString(starthour) + ":" + Integer.toString(startmin) + "pm";
            }
        }
        else {
            if(startmin<10) {
                stime = Integer.toString(starthour-12) + ":0" + Integer.toString(startmin) + "pm";
            }
            else{
                stime = Integer.toString(starthour-12) + ":" + Integer.toString(startmin) + "pm";
            }
        }

        //set end time text
        if(endhour<12) {
            if(endmin<10) {
                etime = Integer.toString(endhour) + ":0" + Integer.toString(endmin) + "am";
            }
            else{
                etime = Integer.toString(endhour) + ":" + Integer.toString(endmin) + "am";
            }
        }
        else if(endhour==12){
            if(endmin<10) {
                etime = Integer.toString(endhour) + ":0" + Integer.toString(endmin) + "pm";
            }
            else{
                etime = Integer.toString(endhour) + ":" + Integer.toString(endmin) + "pm";
            }
        }
        else {
            if(endmin<10) {
                etime = Integer.toString(endhour-12) + ":0" + Integer.toString(endmin) + "pm";
            }
            else{
                etime = Integer.toString(endhour-12) + ":" + Integer.toString(endmin) + "pm";
            }
        }
        if(pickedstart) {
            starttime.setText(stime);
        }
        if(pickedend) {
            if ((60 * starthour + startmin) < (60 * endhour + endmin)) {
                etime = "Same day, " + etime;
            } else {
                etime = "Next day, " + etime;
            }
            endtime.setText(etime);
        }
    }


    private void getdays(){
        mon = (CheckBox) view.findViewById(R.id.mon);
        tue = (CheckBox) view.findViewById(R.id.tue);
        wed = (CheckBox) view.findViewById(R.id.wed);
        thu = (CheckBox) view.findViewById(R.id.thu);
        fri = (CheckBox) view.findViewById(R.id.fri);
        sat = (CheckBox) view.findViewById(R.id.sat);
        sun = (CheckBox) view.findViewById(R.id.sun);
    }

    private void setDaysState(boolean b){
        mon.setEnabled(b);
        tue.setEnabled(b);
        wed.setEnabled(b);
        thu.setEnabled(b);
        fri.setEnabled(b);
        sat.setEnabled(b);
        sun.setEnabled(b);
    }

    private void setButtonsState(boolean b){
        start.setEnabled(b);
        end.setEnabled(b);
    }


    private void getButtons(){
        start = (Button) view.findViewById(R.id.chooseStart);
        start.setOnClickListener(this);
        end   = (Button) view.findViewById(R.id.chooseEnd);
        end.setOnClickListener(this);
    }

    private void reportspot(){
        if(radioGroup1.getCheckedRadioButtonId()== -1){
            Toast.makeText(this.getContext(), "Please choose a time range", Toast.LENGTH_SHORT).show();
            return;
        }
        if(radioGroup2.getCheckedRadioButtonId()== -1){
            Toast.makeText(this.getContext(), "Please pick at least one day of the week", Toast.LENGTH_SHORT).show();
            return;
        }
        if(radioflag==true) {
            if ((!pickedstart) || (!pickedend)) {
                Toast.makeText(this.getContext(), "Please choose a time range", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if(!fullweek){
            if(!(mon.isChecked()||tue.isChecked()||wed.isChecked()||thu.isChecked()||fri.isChecked()||sat.isChecked()||sun.isChecked())){
                Toast.makeText(getContext(),"Please pick at least one day of the week",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        database = FirebaseDatabase.getInstance().getReference();
        String LatLngCode = getLatLngCode(latitude,longitude);
        ReportedTimes reportedTimes = new ReportedTimes(latitude,longitude,0,fullday,starthour,startmin,endhour,endmin,fullweek,mon.isChecked(),tue.isChecked(),wed.isChecked(),
                thu.isChecked(),fri.isChecked(),sat.isChecked(),sun.isChecked(),description.getText().toString(),LatLngCode,false);
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
            }
        });

        showPostReport(bytearray);

    }

    private void showPostReport(byte[] bytearray){
        HomeScreenActivity homeScreenActivity = (HomeScreenActivity) this.getActivity();
        homeScreenActivity.getPostReport(bytearray);
        homeScreenActivity.refreshMenu();
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
