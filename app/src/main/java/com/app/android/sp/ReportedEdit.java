package com.app.android.sp;
import android.app.TimePickerDialog;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 11/26/16.
 */
public class ReportedEdit extends Fragment implements View.OnClickListener{
    //Variable Declaration
    private String UID="",key;
    private static String TAG = "debugger";
    private ReportedTimes reportedTimes;
    private RadioButton allweek,allday,selectdays,selecttime;
    private CheckBox mon,tue,wed,thu,fri,sat,sun;
    private int starthour=0,startmin=0,endhour=0,endmin=0;
    private TextView starttime,endtime;
    private RadioGroup radioGroup1,radioGroup2;
    private boolean fullday=false,fullweek=false,radioflag;
    private View pubview;
    private Button start,end,save;
    private DatabaseReference database;
    private EditText editText;
    private boolean pickedstart=false,pickedend=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();
        UID = extras.getString("UID");
        key = extras.getString("key");
        reportedTimes = extras.getParcelable("reportedtimes");


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
        starttime = (TextView)view.findViewById(R.id.editstarttime);
        endtime   = (TextView)view.findViewById(R.id.editendtime);
        editText.setText(reportedTimes.getdescription());
        getdays();
        getButtons();
        allday = (RadioButton)view.findViewById(R.id.editallday);
        allweek = (RadioButton)view.findViewById(R.id.editallweek);
        if(reportedTimes.getfullweek()){
            fullweek = true;
            setDaysState(false);
            allweek.setChecked(true);
        }
        else{
            fullweek = false;
            setDaysState(true);
            selectdays = (RadioButton)view.findViewById(R.id.editchoosedays);
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
            setButtonsState(false);
            allday.setChecked(true);
        }
        else{
            pickedstart=true;
            pickedend  =true;
            setButtonsState(true);
            selecttime = (RadioButton)view.findViewById(R.id.editchoosetime);
            selecttime.setChecked(true);
            starthour = reportedTimes.getstarthours();
            startmin = reportedTimes.getstartmins();
            endhour = reportedTimes.getendhours();
            endmin = reportedTimes.getendmins();
            printtime(starthour,startmin,endhour,endmin);

        }

        radioGroup1 = (RadioGroup) view.findViewById(R.id.edittimesgroup);
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.editallday) {
                    radioflag=false;
                    fullday = true;

                    setButtonsState(false);
                }
                else if(checkedId == R.id.editchoosetime) {
                    fullday = false;
                    radioflag=true;
                    setButtonsState(true);
                    if(!(pickedstart & pickedend)){
                        Toast.makeText(getContext(),"Please choose a time",Toast.LENGTH_SHORT).show();
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
                    setDaysState(false);
                }
                else if(checkedId == R.id.editchoosedays) {
                    fullweek = false;
                    setDaysState(true);
                }
                else {

                }

            }

        });

        return view;
    }

    private void getdays(){
        mon = (CheckBox) pubview.findViewById(R.id.editmon);
        tue = (CheckBox) pubview.findViewById(R.id.edittue);
        wed = (CheckBox) pubview.findViewById(R.id.editwed);
        thu = (CheckBox) pubview.findViewById(R.id.editthu);
        fri = (CheckBox) pubview.findViewById(R.id.editfri);
        sat = (CheckBox) pubview.findViewById(R.id.editsat);
        sun = (CheckBox) pubview.findViewById(R.id.editsun);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.editchooseStart) {
            showStartDialog();
        }
        if (view.getId() == R.id.editchooseEnd){
            showEndDialog();
        }
        if (view.getId() == R.id.editsave){
            reportspot();
        }

    }

    private void showStartDialog(){
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

    private void showEndDialog(){
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                pickedend = true;
                endhour = selectedHour;
                endmin = selectedMinute;
                printtime(starthour,startmin,endhour,endmin);

            }
        }, endhour,endmin, false);
        mTimePicker.setTitle("Pick an end time");
        mTimePicker.show();
    }

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
        start = (Button) pubview.findViewById(R.id.editchooseStart);
        start.setOnClickListener(this);
        start.setEnabled(false);
        end   = (Button) pubview.findViewById(R.id.editchooseEnd);
        end.setOnClickListener(this);
        end.setEnabled(false);
        save = (Button) pubview.findViewById(R.id.editsave);
        save.setOnClickListener(this);

    }

    private void reportspot(){
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
        String LatLngCode = reportedTimes.getlatlngcode();
        ReportedTimes newTimes = new ReportedTimes(reportedTimes.getlatitude(),reportedTimes.getlongitude(),reportedTimes.getverification(),allday.isChecked(),starthour,startmin,endhour,endmin,allweek.isChecked(),mon.isChecked(),tue.isChecked(),wed.isChecked(),
                thu.isChecked(),fri.isChecked(),sat.isChecked(),sun.isChecked(),editText.getText().toString(),LatLngCode,reportedTimes.getawarded());
        Map<String,Object> reportedTimesMap = newTimes.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/ReportedDetails/"+LatLngCode+"/"+key,UID);
        childUpdates.put("/ReportedTimes/"+UID+"/"+key,reportedTimesMap);
        database.updateChildren(childUpdates);
        HomeScreenActivity home = (HomeScreenActivity) getActivity();
        home.getContri();

    }

}
