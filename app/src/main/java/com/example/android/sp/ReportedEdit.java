package com.example.android.sp;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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

/**
 * Created by ruturaj on 11/26/16.
 */
public class ReportedEdit extends Fragment implements View.OnClickListener{
    String UID="",key;
    static String TAG = "debugger";
    ReportedTimes reportedTimes;
    ArrayList<Integer> time = new ArrayList<Integer>();
    RadioButton allweek,allday,selectdays,selecttime;
    CheckBox mon,tue,wed,thu,fri,sat,sun;
    int starthour=99,startmin=99,endhour=99,endmin=99;
    TextView starttime,endtime;
    RadioGroup radioGroup1,radioGroup2;
    boolean fullday=false,fullweek=false,radioflag;
    TimePicker timePicker;
    View pubview;
    Button start,end,save;
    private static final int REQ_CODE = 3;
    DatabaseReference database;
    EditText editText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();
        UID = extras.getString("UID");
        key = extras.getString("key");
        reportedTimes = extras.getParcelable("reportedtimes");
        Log.d(TAG,"cr times "+reportedTimes.getdescription());


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reportededit, container, false); //inflate the view
        pubview=view;
        editText = (EditText)view.findViewById(R.id.editdescription);
        editText.setText(reportedTimes.getdescription());
        getdays();
        getButtons();
        allday = (RadioButton)view.findViewById(R.id.editallday);
        allweek = (RadioButton)view.findViewById(R.id.editallweek);
        if(reportedTimes.getfullweek()){
            deactivateDays();
            allweek.setChecked(true);
        }
        else{
            activateDays();
            selectdays = (RadioButton)view.findViewById(R.id.editchoosedays);
            mon = (CheckBox) view.findViewById(R.id.editmon);
            tue = (CheckBox) view.findViewById(R.id.edittue);
            wed = (CheckBox) view.findViewById(R.id.editwed);
            thu = (CheckBox) view.findViewById(R.id.editthu);
            fri = (CheckBox) view.findViewById(R.id.editfri);
            sat = (CheckBox) view.findViewById(R.id.editsat);
            sun = (CheckBox) view.findViewById(R.id.editsun);
            selectdays.setChecked(true);
            if(reportedTimes.mon){
                mon.setChecked(true);
            }
            if(reportedTimes.tue){
                tue.setChecked(true);
            }
            if(reportedTimes.wed){
                wed.setChecked(true);
            }
            if(reportedTimes.thu){
                thu.setChecked(true);
            }
            if(reportedTimes.fri){
                fri.setChecked(true);
            }
            if(reportedTimes.sat){
                sat.setChecked(true);
            }
            if(reportedTimes.sun){
                sun.setChecked(true);
            }
        }

        if(reportedTimes.getfullday()){
            deactivateButtons();
            allday.setChecked(true);
        }
        else{
            activateButtons();
            selecttime = (RadioButton)view.findViewById(R.id.editchoosetime);
            selecttime.setChecked(true);
            starthour = reportedTimes.getstarthours();
            startmin = reportedTimes.getstartmins();
            endhour = reportedTimes.getendhours();
            endmin = reportedTimes.getendmins();
            if(starthour<=12){
                starttime = (TextView) view.findViewById(R.id.editstarttime);
                String time = starthour+":"+startmin+" am";
                starttime.setText(time);
            }
            if(starthour>12){
                starttime = (TextView) view.findViewById(R.id.editstarttime);
                String time = starthour-12 +":"+startmin+" pm";
                starttime.setText(time);
            }
            if(endhour<=12){
                endtime = (TextView) view.findViewById(R.id.editendtime);
                String time = endhour+":"+endmin+" am";
                endtime.setText(time);
            }
            if(endhour>12){
                endtime = (TextView) view.findViewById(R.id.editendtime);
                String time = endhour-12 +":"+endmin+" am";
                endtime.setText(time);
            }
        }

        radioGroup1 = (RadioGroup) view.findViewById(R.id.edittimesgroup);
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.editallday) {
                    radioflag=false;
                    fullday = true;

                    deactivateButtons();
                }
                else if(checkedId == R.id.editchoosetime) {
                    fullday = false;
                    radioflag=true;
                    activateButtons();
                    if(starthour==99 || startmin==99 || endhour==99 || endmin==99){
                        Toast.makeText(getContext(),"Please choose a time",Toast.LENGTH_LONG).show();
                    }
                }
                else {

                }

            }
        });

        radioGroup2 = (RadioGroup) view.findViewById(R.id.editdaysgroup);
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.editallweek) {
                    fullweek = true;
                    deactivateDays();
                }
                else if(checkedId == R.id.editchoosedays) {
                    fullweek = false;
                    activateDays();
                }
                else {

                }

            }

        });

        return view;
    }

    public void getdays(){
        mon = (CheckBox) pubview.findViewById(R.id.editmon);
        mon.setEnabled(false);
        tue = (CheckBox) pubview.findViewById(R.id.edittue);
        tue.setEnabled(false);
        wed = (CheckBox) pubview.findViewById(R.id.editwed);
        wed.setEnabled(false);
        thu = (CheckBox) pubview.findViewById(R.id.editthu);
        thu.setEnabled(false);
        fri = (CheckBox) pubview.findViewById(R.id.editfri);
        fri.setEnabled(false);
        sat = (CheckBox) pubview.findViewById(R.id.editsat);
        sat.setEnabled(false);
        sun = (CheckBox) pubview.findViewById(R.id.editsun);
        sun.setEnabled(false);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.editchooseStart) {
            time.add(123);
            showReditDialog();
            Log.d(TAG,"choose start");
        }
        if (view.getId() == R.id.editchooseEnd){
            time.add(243);
            showReditDialog();
        }
        if (view.getId() == R.id.editsave){
            reportspot();
        }

    }

    public void showReditDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ReditDialog();
        dialog.setTargetFragment(ReportedEdit.this, REQ_CODE);       //set target fragment to this fragment
        dialog.show(this.getActivity().getSupportFragmentManager(),"Names fragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //fetch information from the dialog and call the checkIn function
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==-1) {
            Bundle bundle = data.getExtras();
            time.add(bundle.getInt("hour"));
            time.add(bundle.getInt("mins"));
            printtime();
        }
    }

    public void printtime(){
        for(int i=0;i<time.size();i++){
            if(time.get(i)>=0 && time.get(i)<=24){
                if(time.get(i-1)==123.){
                    if(time.get(i)>=0 && time.get(i)<=12) {
                        TextView t = (TextView) pubview.findViewById(R.id.editstarttime);
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
                        TextView t = (TextView) pubview.findViewById(R.id.editstarttime);
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
                        TextView t = (TextView) pubview.findViewById(R.id.editendtime);
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
                        TextView t = (TextView) pubview.findViewById(R.id.editendtime);
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

    public void activateDays(){
        mon.setEnabled(true);
        tue.setEnabled(true);
        wed.setEnabled(true);
        thu.setEnabled(true);
        fri.setEnabled(true);
        sat.setEnabled(true);
        sun.setEnabled(true);
    }

    public void deactivateDays(){
        mon.setEnabled(false);
        tue.setEnabled(false);
        wed.setEnabled(false);
        thu.setEnabled(false);
        fri.setEnabled(false);
        sat.setEnabled(false);
        sun.setEnabled(false);
    }

    public void getButtons(){
        start = (Button) pubview.findViewById(R.id.editchooseStart);
        start.setOnClickListener(this);
        start.setEnabled(false);
        end   = (Button) pubview.findViewById(R.id.editchooseEnd);
        end.setOnClickListener(this);
        end.setEnabled(false);
        save = (Button) pubview.findViewById(R.id.editsave);
        save.setOnClickListener(this);

    }

    public void activateButtons(){
        start.setEnabled(true);
        end.setEnabled(true);
    }

    public void deactivateButtons(){
        start.setEnabled(false);
        end.setEnabled(false);
    }

    public void reportspot(){
        if(radioflag==true) {
            if (starthour == 99 || startmin == 99 || endhour == 99 || endmin == 99) {
                Toast.makeText(getContext(), "Please choose a time", Toast.LENGTH_LONG).show();
                return;
            }
        }
        database = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG,"description "+editText.getText().toString());
        String LatLngCode = reportedTimes.getlatlngcode();
        ReportedTimes newTimes = new ReportedTimes(reportedTimes.getlatitude(),reportedTimes.getlongitude(),reportedTimes.getverification(),allday.isChecked(),starthour,startmin,endhour,endmin,allweek.isChecked(),mon.isChecked(),tue.isChecked(),wed.isChecked(),
                thu.isChecked(),fri.isChecked(),sat.isChecked(),sun.isChecked(),editText.getText().toString(),LatLngCode);
        Map<String,Object> reportedTimesMap = newTimes.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/ReportedDetails/"+LatLngCode+"/"+key,UID);
        childUpdates.put("/ReportedTimes/"+UID+"/"+key,reportedTimesMap);
        database.updateChildren(childUpdates);
        HomeScreenActivity home = (HomeScreenActivity) getActivity();
        home.getContri();

    }

}
