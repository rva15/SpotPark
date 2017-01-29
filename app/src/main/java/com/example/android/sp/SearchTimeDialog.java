package com.example.android.sp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ruturaj on 1/20/17.
 */
public class SearchTimeDialog extends android.support.v4.app.DialogFragment implements View.OnClickListener{


    Calendar startcalendar,endcalendar;
    ImageView psdate,pstime,pedate,petime;
    SimpleDateFormat mdformat;
    TextView startdate,starttime,enddate,endtime;
    Boolean startend;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_searchtime, null);

        startcalendar = Calendar.getInstance();
        mdformat = new SimpleDateFormat("yyyy / MM / dd ");
        startdate = (TextView) view.findViewById(R.id.searchsdate);
        setstartdatetv();
        starttime = (TextView) view.findViewById(R.id.searchstime);
        setstarttimetv();

        endcalendar = Calendar.getInstance();
        endcalendar.add(Calendar.HOUR_OF_DAY,3);
        enddate = (TextView) view.findViewById(R.id.searchedate);
        setenddatetv();
        endtime = (TextView) view.findViewById(R.id.searchetime);
        setendtimetv();

        psdate = (ImageView)view.findViewById(R.id.pickstartdate);
        pstime = (ImageView)view.findViewById(R.id.pickstarttime);
        pedate = (ImageView)view.findViewById(R.id.pickenddate);
        petime = (ImageView)view.findViewById(R.id.pickendtime);
        psdate.setOnClickListener(this);
        pedate.setOnClickListener(this);
        pstime.setOnClickListener(this);
        petime.setOnClickListener(this);


        builder.setView(view)
                // Add action buttons
                .setPositiveButton("SEARCH", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent();
                        i.putExtra("startcalendar",startcalendar);
                        i.putExtra("endcalendar",endcalendar);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        SearchTimeDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if(v.getId()==R.id.pickstartdate) {
            startend = false;
        new DatePickerDialog(getContext(), date, startcalendar
                .get(Calendar.YEAR), startcalendar.get(Calendar.MONTH),
                startcalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
        if(v.getId()==R.id.pickstarttime) {
            startend = false;
            new TimePickerDialog(getContext(), time, startcalendar
                    .get(Calendar.HOUR_OF_DAY), startcalendar.get(Calendar.MINUTE),
                   false).show();
        }
        if(v.getId()==R.id.pickenddate) {
            startend = true;
            new DatePickerDialog(getContext(), date, endcalendar
                    .get(Calendar.YEAR), endcalendar.get(Calendar.MONTH),
                    endcalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
        if(v.getId()==R.id.pickendtime) {
            startend = true;
            new TimePickerDialog(getContext(), time, endcalendar
                    .get(Calendar.HOUR_OF_DAY), endcalendar.get(Calendar.MINUTE),
                    false).show();
        }

    }


    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            if(startend) {
                endcalendar.set(Calendar.YEAR, year);
                endcalendar.set(Calendar.MONTH, monthOfYear);
                endcalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setenddatetv();
            }
            else{
                startcalendar.set(Calendar.YEAR, year);
                startcalendar.set(Calendar.MONTH, monthOfYear);
                startcalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setstartdatetv();

            }

        }

    };

    TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener(){

        @Override
        public void onTimeSet(TimePicker view,int hours,int mins){
            if(startend) {
                endcalendar.set(Calendar.HOUR_OF_DAY, hours);
                endcalendar.set(Calendar.MINUTE, mins);
                setendtimetv();
            }
            else{
                startcalendar.set(Calendar.HOUR_OF_DAY, hours);
                startcalendar.set(Calendar.MINUTE, mins);
                setstarttimetv();
            }


        }


    };

    private void setstartdatetv(){
        String strDate = mdformat.format(startcalendar.getTime());
        startdate.setText(strDate);
    }
    private void setenddatetv(){
        String endDate = mdformat.format(endcalendar.getTime());
        enddate.setText(endDate);
    }
    private void setstarttimetv(){
        String strTime = startcalendar.get(Calendar.HOUR)+" : "+startcalendar.get(Calendar.MINUTE)+" ";
        if(startcalendar.get(Calendar.AM_PM) == Calendar.AM){
            strTime = strTime +"am";
        }
        else if(startcalendar.get(Calendar.AM_PM) == Calendar.PM){
            strTime = strTime +"pm";
        }
        starttime.setText(strTime);

    }
    private void setendtimetv(){
        String endTime = endcalendar.get(Calendar.HOUR)+" : "+endcalendar.get(Calendar.MINUTE)+" ";
        if(endcalendar.get(Calendar.AM_PM) == Calendar.AM){
            endTime = endTime +"am";
        }
        else if(endcalendar.get(Calendar.AM_PM) == Calendar.PM){
            endTime = endTime +"pm";
        }
        endtime.setText(endTime);

    }


}
