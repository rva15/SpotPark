package com.application.android.sp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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

import java.util.Calendar;

/**
 * Created by ruturaj on 1/20/17.
 */
public class SearchTimeDialog extends android.support.v4.app.DialogFragment implements View.OnClickListener{


    private Calendar startcalendar,endcalendar;
    private ImageView psdate,pstime,pedate,petime;
    private TextView startdate,starttime,enddate,endtime;
    private Boolean startend;
    private String fromtime="Now",untiltime="Next 3 hours";
    private Boolean timeset=false;
    private String TAG="debugger";




    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_searchtime, null);

        startcalendar = Calendar.getInstance();
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
                        if(timeset) {
                            fromtime = startdate.getText() + "  " + starttime.getText();
                            untiltime = enddate.getText() + "  " + endtime.getText();
                        }
                        i.putExtra("startcalendar",startcalendar);
                        i.putExtra("endcalendar",endcalendar);
                        i.putExtra("displaystart",fromtime);
                        i.putExtra("displayend",untiltime);
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

            timeset = true;
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
            timeset=true;
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
        String strDate="";
        int day = startcalendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.SUNDAY:
                strDate = "Sun";
                break;
            case Calendar.MONDAY:
                strDate = "Mon";
                break;
            case Calendar.TUESDAY:
                strDate = "Tue";
                break;
            case Calendar.WEDNESDAY:
                strDate = "Wed";
                break;
            case Calendar.THURSDAY:
                strDate = "Thu";
                break;
            case Calendar.FRIDAY:
                strDate = "Fri";
                break;
            case Calendar.SATURDAY:
                strDate = "Sat";
                break;
        }
        strDate = strDate + ", ";
        int month = startcalendar.get(Calendar.MONTH);
        switch (month) {
            case Calendar.JANUARY:
                strDate = strDate+"Jan ";
                break;
            case Calendar.FEBRUARY:
                strDate = strDate+"Feb ";
                break;
            case Calendar.MARCH:
                strDate = strDate+"Mar ";
                break;
            case Calendar.APRIL:
                strDate = strDate+"Apr ";
                break;
            case Calendar.MAY:
                strDate = strDate+"May ";
                break;
            case Calendar.JUNE:
                strDate = strDate+"Jun ";
                break;
            case Calendar.JULY:
                strDate = strDate+"Jul ";
                break;
            case Calendar.AUGUST:
                strDate = strDate+"Aug ";
                break;
            case Calendar.SEPTEMBER:
                strDate = strDate+"Sep ";
                break;
            case Calendar.OCTOBER:
                strDate = strDate+"Oct ";
                break;
            case Calendar.NOVEMBER:
                strDate = strDate+"Nov ";
                break;
            case Calendar.DECEMBER:
                strDate = strDate+"Dec ";
                break;
        }
        strDate = strDate+Integer.toString(startcalendar.get(Calendar.DAY_OF_MONTH));

        startdate.setText(strDate);
    }
    private void setenddatetv(){
        String strDate="";
        int day = endcalendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY:
                strDate = "Sun";
                break;
            case Calendar.MONDAY:
                strDate = "Mon";
                break;
            case Calendar.TUESDAY:
                strDate = "Tue";
                break;
            case Calendar.WEDNESDAY:
                strDate = "Wed";
                break;
            case Calendar.THURSDAY:
                strDate = "Thu";
                break;
            case Calendar.FRIDAY:
                strDate = "Fri";
                break;
            case Calendar.SATURDAY:
                strDate = "Sat";
                break;
        }
        strDate = strDate + ", ";
        int month = endcalendar.get(Calendar.MONTH);
        switch (month) {
            case Calendar.JANUARY:
                strDate = strDate+"Jan ";
                break;
            case Calendar.FEBRUARY:
                strDate = strDate+"Feb ";
                break;
            case Calendar.MARCH:
                strDate = strDate+"Mar ";
                break;
            case Calendar.APRIL:
                strDate = strDate+"Apr ";
                break;
            case Calendar.MAY:
                strDate = strDate+"May ";
                break;
            case Calendar.JUNE:
                strDate = strDate+"Jun ";
                break;
            case Calendar.JULY:
                strDate = strDate+"Jul ";
                break;
            case Calendar.AUGUST:
                strDate = strDate+"Aug ";
                break;
            case Calendar.SEPTEMBER:
                strDate = strDate+"Sep ";
                break;
            case Calendar.OCTOBER:
                strDate = strDate+"Oct ";
                break;
            case Calendar.NOVEMBER:
                strDate = strDate+"Nov ";
                break;
            case Calendar.DECEMBER:
                strDate = strDate+"Dec ";
                break;
        }
        strDate = strDate+Integer.toString(endcalendar.get(Calendar.DAY_OF_MONTH));

        enddate.setText(strDate);
    }
    private void setstarttimetv(){
        String strTime;
        if((int)startcalendar.get(Calendar.HOUR)!= 0) {
            if(startcalendar.get(Calendar.MINUTE)>9) {
                strTime = startcalendar.get(Calendar.HOUR) + " : " + startcalendar.get(Calendar.MINUTE) + " ";
            }
            else{
                strTime = startcalendar.get(Calendar.HOUR) + " : 0" + startcalendar.get(Calendar.MINUTE) + " ";
            }
        }
        else{
            if(startcalendar.get(Calendar.MINUTE)>9) {
                strTime = " 12: " + startcalendar.get(Calendar.MINUTE) + " ";
            }
            else{
                strTime = " 12: 0" + startcalendar.get(Calendar.MINUTE) + " ";
            }
        }
        if( (int) startcalendar.get(Calendar.AM_PM) == Calendar.AM){
            strTime = strTime +"am";
        }
        else if((int)startcalendar.get(Calendar.AM_PM) == Calendar.PM){
            strTime = strTime +"pm";
        }
        starttime.setText(strTime);

    }
    private void setendtimetv(){
        String strTime;
        if((int) endcalendar.get(Calendar.HOUR)!= 0) {
            if(endcalendar.get(Calendar.MINUTE)>9) {
                strTime = endcalendar.get(Calendar.HOUR) + " : " + endcalendar.get(Calendar.MINUTE) + " ";
            }
            else{
                strTime = endcalendar.get(Calendar.HOUR) + " : 0" + endcalendar.get(Calendar.MINUTE) + " ";
            }
        }
        else{
            if(endcalendar.get(Calendar.MINUTE)>9) {
                strTime = " 12: " + endcalendar.get(Calendar.MINUTE) + " ";
            }
            else{
                strTime = " 12: 0" + endcalendar.get(Calendar.MINUTE) + " ";
            }
        }
        if((int)endcalendar.get(Calendar.AM_PM) == Calendar.AM){
            strTime = strTime +"am";
        }
        else if((int)endcalendar.get(Calendar.AM_PM) == Calendar.PM){
            strTime = strTime +"pm";
        }
        endtime.setText(strTime);

    }


}
