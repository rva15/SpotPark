package com.app.android.sp;
// All imports
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ruturaj on 8/26/16.
 */
public class CheckInDialog extends DialogFragment {
    //Variable Declarations
    private String TAG="debugger";
    private TextView remindmsg;
    private RadioButton othersyes,othersno,free,costly;
    private ImageView alarm,clear;
    private EditText cph;
    private int selecthour=123,selectmin=123;
    private CardView cincard3,cincard1;


    public CheckInDialog(){}


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_checkin, null);

        // Get views and declare UI components
        remindmsg = (TextView) view.findViewById(R.id.remindmsg);
        othersyes = (RadioButton) view.findViewById(R.id.othersyes);
        othersno = (RadioButton) view.findViewById(R.id.othersno);
        alarm = (ImageView) view.findViewById(R.id.alarm);
        clear = (ImageView) view.findViewById(R.id.clear);
        clear.setVisibility(View.GONE);
        cincard3 = (CardView) view.findViewById(R.id.cincard3);
        cincard1 = (CardView) view.findViewById(R.id.cincard1);
        free = (RadioButton) view.findViewById(R.id.free);
        cph = (EditText) view.findViewById(R.id.cph);
        costly = (RadioButton) view.findViewById(R.id.costly);
        cincard1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {     //open time picker if alarm image is clicked
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);         //get current time and pass to timepicker
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        selecthour = selectedHour;
                        selectmin = selectedMinute;
                        //set user message as per time selected
                        if(selectedHour<12) {
                            if(selectedMinute<10) {
                                remindmsg.setText("Reminder scheduled for " + Integer.toString(selectedHour) + ":0" + Integer.toString(selectedMinute) + "am");
                            }
                            else{
                                remindmsg.setText("Reminder scheduled for " + Integer.toString(selectedHour) + ":" + Integer.toString(selectedMinute) + "am");
                            }
                        }
                        else if(selectedHour==12){
                            if(selectedMinute<10) {
                                remindmsg.setText("Reminder scheduled for " + Integer.toString(selectedHour) + ":0" + Integer.toString(selectedMinute) + "pm");
                            }
                            else{
                                remindmsg.setText("Reminder scheduled for " + Integer.toString(selectedHour) + ":" + Integer.toString(selectedMinute) + "pm");
                            }
                        }
                        else {
                            if(selectedMinute<10) {
                                remindmsg.setText("Reminder scheduled for " + Integer.toString(selectedHour-12) + ":0" + Integer.toString(selectedMinute) + "pm");
                            }
                            else{
                                remindmsg.setText("Reminder scheduled for " + Integer.toString(selectedHour-12) + ":" + Integer.toString(selectedMinute) + "pm");
                            }
                        }
                        clear.setVisibility(View.VISIBLE); //make the cancel button visible
                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Set Reminder Time");
                mTimePicker.show();

            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               selecthour=123;          //reset the selected time
                selectmin= 123;
                remindmsg.setText("No reminder set");
                clear.setVisibility(View.GONE);
            }
        });

        othersyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cincard3.setVisibility(View.VISIBLE);
            }
        });
        othersno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cincard3.setVisibility(View.GONE);
            }
        });

        free.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cph.setEnabled(false);
                cph.setText("");
            }
        });
        costly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cph.setEnabled(true);
            }
        });




        builder.setView(view)
                // Add action buttons
                .setPositiveButton("CheckIn", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        Intent i = new Intent();      //bundle all the information and send to CheckIn fragment
                        i.putExtra("hours",selecthour);
                        i.putExtra("mins",selectmin);
                        i.putExtra("otherspark",othersyes.isChecked());
                        i.putExtra("free",free.isChecked());
                        i.putExtra("cph",cph.getText().toString());
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        CheckInDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();



    }

}
