package com.example.android.sp;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

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

public class ReportForm extends AppCompatActivity implements ReportFormDialog.ReportFormDialogListener{
    private static final String TAG = "Debugger ";
    ArrayList<Integer> time = new ArrayList<Integer>();
    RadioGroup radioGroup1,radioGroup2;
    int starthour=99,startmin=99,endhour=99,endmin=99;
    boolean mond=false,tues=false,wedn=false,thur=false,frid=false,satu=false,sund=false;
    boolean fullday=false,fullweek=false,radioflag;
    double latitude=0.0,longitude=0.0;
    String UID="";
    CheckBox mon,tue,wed,thu,fri,sat,sun,choosetime;
    public DatabaseReference database;
    String x,y;
    Button start,end;
    byte[] bytearray;
    EditText description;

    TimePicker timePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_form);
        Intent intentfromreport = getIntent();
        UID     = intentfromreport.getStringExtra("user_id");
        Log.d(TAG,"user id reportform "+UID);
        x = intentfromreport.getStringExtra("lats");
        y = intentfromreport.getStringExtra("lons");
        description = (EditText) findViewById(R.id.description);
        bytearray = intentfromreport.getByteArrayExtra("image");
        latitude = Double.parseDouble(x);
        longitude = Double.parseDouble(y);
        radioGroup1 = (RadioGroup) findViewById(R.id.timesgroup);
        getdays();
        getButtons();
        radioGroup1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
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

    public void setid(String s){
        String id = s;
        Log.d(TAG,"user id reportform "+s);
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
                    if(time.get(i)>=0 && time.get(i)<=12) {
                        TextView t = (TextView) findViewById(R.id.starttime);
                        if(time.get(i+1)==0) {
                            t.setText(Integer.toString(time.get(i)) + ":" + Integer.toString(time.get(i + 1))+"0 am");
                        }
                        else{
                            t.setText(Integer.toString(time.get(i)) + ":" + Integer.toString(time.get(i + 1))+" am");
                        }
                        starthour = time.get(i);
                        startmin = time.get(i + 1);
                        Log.d(TAG, "it is start time");
                    }
                    if(time.get(i)>12 && time.get(i)<=24) {
                        int hour=time.get(i)-12;
                        TextView t = (TextView) findViewById(R.id.starttime);
                        if(time.get(i+1)==0) {
                            t.setText(Integer.toString(hour) + ":" + Integer.toString(time.get(i + 1))+"0 pm");
                        }
                        else{
                            t.setText(Integer.toString(hour) + ":" + Integer.toString(time.get(i + 1))+" pm");
                        }
                        starthour = time.get(i);
                        startmin = time.get(i + 1);
                        Log.d(TAG, "it is start time");
                    }

                }
                else if(time.get(i-1)==243.){
                    if(time.get(i)>=0 && time.get(i)<=12) {
                        TextView t = (TextView) findViewById(R.id.endtime);
                        if(time.get(i+1)==0) {
                            t.setText(Integer.toString(time.get(i)) + ":" + Integer.toString(time.get(i + 1))+"0 am");
                        }
                        else{
                            t.setText(Integer.toString(time.get(i)) + ":" + Integer.toString(time.get(i + 1))+" am");
                        }
                        endhour = time.get(i);
                        endmin = time.get(i + 1);
                        Log.d(TAG, "it is end time");
                    }
                    if(time.get(i)>12 && time.get(i)<=24) {
                        int hour=time.get(i)-12;
                        TextView t = (TextView) findViewById(R.id.endtime);
                        if(time.get(i+1)==0) {
                            t.setText(Integer.toString(hour) + ":" + Integer.toString(time.get(i + 1))+"0 pm");
                        }
                        else{
                            t.setText(Integer.toString(hour) + ":" + Integer.toString(time.get(i + 1))+" pm");
                        }
                        endhour = time.get(i);
                        endmin = time.get(i + 1);
                        Log.d(TAG, "it is start time");
                    }
                }
            }
        }
    }

    public void getdays(){
        mon = (CheckBox) findViewById(R.id.mon);
        mon.setEnabled(false);
        tue = (CheckBox) findViewById(R.id.tue);
        tue.setEnabled(false);
        wed = (CheckBox) findViewById(R.id.wed);
        wed.setEnabled(false);
        thu = (CheckBox) findViewById(R.id.thu);
        thu.setEnabled(false);
        fri = (CheckBox) findViewById(R.id.fri);
        fri.setEnabled(false);
        sat = (CheckBox) findViewById(R.id.sat);
        sat.setEnabled(false);
        sun = (CheckBox) findViewById(R.id.sun);
        sun.setEnabled(false);
    }

    public void activateDays(View view){
        mon.setEnabled(true);
        tue.setEnabled(true);
        wed.setEnabled(true);
        thu.setEnabled(true);
        fri.setEnabled(true);
        sat.setEnabled(true);
        sun.setEnabled(true);
    }

    public void deactivateDays(View view){
        mon.setEnabled(false);
        tue.setEnabled(false);
        wed.setEnabled(false);
        thu.setEnabled(false);
        fri.setEnabled(false);
        sat.setEnabled(false);
        sun.setEnabled(false);
    }

    public void getButtons(){
        start = (Button) findViewById(R.id.chooseStart);
        start.setEnabled(false);
        end   = (Button) findViewById(R.id.chooseEnd);
        end.setEnabled(false);
    }

    public void activateButtons(View view){
        start.setEnabled(true);
        end.setEnabled(true);
    }

    public void deactivateButtons(View view){
        start.setEnabled(false);
        end.setEnabled(false);
    }

    public void reportspot(View v){
        if(radioflag==true) {
            if (starthour == 99 || startmin == 99 || endhour == 99 || endmin == 99) {
                Toast.makeText(ReportForm.this, "Please choose a time", Toast.LENGTH_LONG).show();
                return;
            }
        }
        database = FirebaseDatabase.getInstance().getReference();
        //ReportedDetails reportedDetails = new ReportedDetails(latitude,longitude,0,UID);
        //Map<String,Object> reportedDetailsMap = reportedDetails.toMap();
        Log.d(TAG,"description "+description.getText().toString());
        ReportedTimes reportedTimes = new ReportedTimes(latitude,longitude,0,fullday,starthour,startmin,endhour,endmin,fullweek,mon.isChecked(),tue.isChecked(),wed.isChecked(),
                thu.isChecked(),fri.isChecked(),sat.isChecked(),sun.isChecked(),description.getText().toString());
        Map<String,Object> reportedTimesMap = reportedTimes.toMap();
        String LatLngCode = getLatLngCode(latitude,longitude);
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
                Log.d(TAG,"image upload failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d(TAG,"image upload success");
            }
        });
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
