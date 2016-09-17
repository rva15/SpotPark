package com.example.android.sp;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReportForm extends AppCompatActivity implements ReportFormDialog.ReportFormDialogListener{
    private static final String TAG = "Debugger ";
    ArrayList<Integer> time = new ArrayList<Integer>();
    RadioGroup radioGroup1,radioGroup2;
    int starthour=99,startmin=99,endhour=99,endmin=99;
    boolean mond=false,tues=false,wedn=false,thur=false,frid=false,satu=false,sund=false;
    boolean fullday=false,fullweek=false;
    double latitude=0.0,longitude=0.0;
    String UID="";
    CheckBox mon,tue,wed,thu,fri,sat,sun;
    public DatabaseReference database;
    String x,y;

    TimePicker timePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_form);
        Intent intentfromreport = getIntent();
        UID     = intentfromreport.getStringExtra(ReportFragment.UID);
        x = intentfromreport.getStringExtra("lats");
        y = intentfromreport.getStringExtra("lons");
        latitude = Double.parseDouble(x);
        longitude = Double.parseDouble(y);
        radioGroup1 = (RadioGroup) findViewById(R.id.timesgroup);
        radioGroup1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.allday) {
                    fullday = true;
                }
                else if(checkedId == R.id.choosetime) {
                    fullday = false;
                    if(starthour==99 || startmin==99 || endhour==99 || endmin==99){
                        Toast.makeText(ReportForm.this,"Please choose a time",Toast.LENGTH_LONG).show();
                    }
                }
                else {

                }

            }

        });
        radioGroup2 = (RadioGroup) findViewById(R.id.daysgroup);
        radioGroup2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
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
    }

    public void showStartDialog(View v) {
        // Create an instance of the dialog fragment and show it
        time.add(123);
        DialogFragment dialog = new ReportFormDialog();
        dialog.show(getSupportFragmentManager(),"ReportFragment");

    }

    public void showEndDialog(View v) {
        // Create an instance of the dialog fragment and show it
        time.add(243);
        DialogFragment dialog = new ReportFormDialog();
        dialog.show(getSupportFragmentManager(),"ReportFragment");

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Dialog dialogView = dialog.getDialog();
        timePicker = (TimePicker) dialogView.findViewById(R.id.freetime);
        time.add((int)timePicker.getCurrentHour());
        time.add((int) timePicker.getCurrentMinute());
        Log.d(TAG,"positive click");
        printtime();


    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        Log.d(TAG,"negative click");
    }

    public void printtime(){
        for(int i=0;i<time.size();i++){
            if(time.get(i)>=0 && time.get(i)<=24){
                if(time.get(i-1)==123.){
                    TextView t = (TextView)findViewById(R.id.starttime);
                    t.setText(Integer.toString(time.get(i))+":"+Integer.toString(time.get(i+1)));
                    starthour=time.get(i);
                    startmin = time.get(i+1);
                    Log.d(TAG,"it is start time");
                }
                else if(time.get(i-1)==243.){
                    TextView t = (TextView)findViewById(R.id.endtime);
                    t.setText(Integer.toString(time.get(i))+":"+Integer.toString(time.get(i+1)));
                    endhour=time.get(i);
                    endmin=time.get(i+1);
                    Log.d(TAG,"it is end time");
                }
            }
        }
    }

    public void getdays(){
         mon = (CheckBox) findViewById(R.id.mon);
         tue = (CheckBox) findViewById(R.id.tue);
         wed = (CheckBox) findViewById(R.id.wed);
         thu = (CheckBox) findViewById(R.id.thu);
         fri = (CheckBox) findViewById(R.id.fri);
         sat = (CheckBox) findViewById(R.id.sat);
         sun = (CheckBox) findViewById(R.id.sun);


    }

    public void reportspot(View v){
        if(starthour==99 || startmin==99 || endhour==99 || endmin==99){
            Toast.makeText(ReportForm.this,"Please choose a time",Toast.LENGTH_LONG).show();
            return;
        }
        getdays();
        database = FirebaseDatabase.getInstance().getReference();
        ReportedDetails reportedDetails = new ReportedDetails(latitude,longitude,0,UID);
        Map<String,Object> reportedDetailsMap = reportedDetails.toMap();
        ReportedTimes reportedTimes = new ReportedTimes(fullday,starthour,startmin,endhour,endmin,fullweek,mon.isChecked(),tue.isChecked(),wed.isChecked(),
                thu.isChecked(),fri.isChecked(),sat.isChecked(),sun.isChecked());
        Map<String,Object> reportedTimesMap = reportedTimes.toMap();
        String LatLngCode = getLatLngCode(latitude,longitude);

        String key = database.child("ReportedDetails/"+LatLngCode).push().getKey();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/ReportedDetails/"+LatLngCode+"/"+key, reportedDetailsMap);
        childUpdates.put("/ReportedTimes/"+key,reportedTimesMap);
        database.updateChildren(childUpdates);
    }

    public String getLatLngCode(double lat, double lon){

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
