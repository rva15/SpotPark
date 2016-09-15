package com.example.android.sp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * Created by ruturaj on 8/26/16.
 */
public class CheckInDialog extends DialogFragment {
    Activity a;
    EditText rph;
    TimePicker timePicker;
    String hourlyrate;
    double hours,mins;
    String hour,min;
    public CheckInDialog(){}

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // R.layout.my_layout - that's the layout where your textview is placed
        View view = inflater.inflate(R.layout.checkindialog, container, false);
        rph = (EditText) view.findViewById(R.id.rate);
        // you can use your textview.
        return view;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout. checkindialog, null);


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("CheckIn", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //rph = (EditText) getView().findViewById(R.id.rate);
                        rph = (EditText) view.findViewById(R.id.rate);
                        timePicker = (TimePicker) view.findViewById(R.id.time);
                        hourlyrate = rph.getText().toString();
                        hours = (double)timePicker.getCurrentHour();
                        mins = (double) timePicker.getCurrentMinute();
                        hour = Double.toString(hours);
                        min = Double.toString(mins);
                        Intent i = new Intent().putExtra("rates",hourlyrate);
                        i.putExtra("hours",hour);
                        i.putExtra("mins",min);
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
