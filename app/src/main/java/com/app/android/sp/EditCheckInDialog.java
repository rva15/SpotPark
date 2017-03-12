package com.app.android.sp;
// All imports
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by ruturaj on 8/26/16.
 */
public class EditCheckInDialog extends DialogFragment {
    //Variable Declarations
    private Activity a;
    public int couthours=123;
    int coutmins=123;
    private ImageView clear;
    private TextView msg;
    private CardView cincard3;
    private RadioButton free,costly;
    private EditText rate;
    private RadioButton othersyes,othersno;


    public EditCheckInDialog(){}   //empty constructor


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_editcheckin, null);
        Bundle args = getArguments();
        final String dollars = Integer.toString(args.getInt("dollars")); //get the rate from hs activity
        final String cents = Integer.toString(args.getInt("cents"));
        couthours = args.getInt("couthours");  // and the reminder time
        coutmins = args.getInt("coutmins");
        othersyes = (RadioButton) view.findViewById(R.id.editothersyes);
        othersno = (RadioButton) view.findViewById(R.id.editothersno);
        cincard3 = (CardView) view.findViewById(R.id.editcincard3);
        free = (RadioButton) view.findViewById(R.id.editfree);
        rate = (EditText) view.findViewById(R.id.editcph);
        costly = (RadioButton) view.findViewById(R.id.editcostly);

        rate = (EditText) view.findViewById(R.id.editcph); //set the previous rate to edittext
        if(Integer.parseInt(cents)<10) {
            rate.setText(dollars + ".0" + cents);
        }
        else{
            rate.setText(dollars + "." + cents);
        }
        msg = (TextView) view.findViewById(R.id.editremindmsg);
        if(couthours!=123 && coutmins!=123) {  //set the correct default user message
            if (couthours < 12) {
                if (coutmins < 10) {
                    msg.setText("Reminder scheduled for " + Integer.toString(couthours) + ":0" + Integer.toString(coutmins) + "am");
                } else {
                    msg.setText("Reminder scheduled for " + Integer.toString(couthours) + ":" + Integer.toString(coutmins) + "am");
                }
            }
            else if(couthours==12){
                if (coutmins < 10) {
                    msg.setText("Reminder scheduled for " + Integer.toString(couthours) + ":0" + Integer.toString(coutmins) + "pm");
                } else {
                    msg.setText("Reminder scheduled for " + Integer.toString(couthours) + ":" + Integer.toString(coutmins) + "pm");
                }
            }
            else {
                if (couthours < 10) {
                    msg.setText("Reminder scheduled for " + Integer.toString(couthours - 12) + ":0" + Integer.toString(coutmins) + "am");
                } else {
                    msg.setText("Reminder scheduled for " + Integer.toString(couthours - 12) + ":" + Integer.toString(coutmins) + "am");
                }
            }
        }
        else{
            msg.setText("Reminder not set");
        }

        clear = (ImageView) view.findViewById(R.id.editclear);
        if(msg.getText().equals("Reminder not set")){
            clear.setVisibility(View.GONE);
        }
        ImageView alarm = (ImageView)view.findViewById(R.id.editalarm);
        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {   //listener for setting alarm
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        couthours = selectedHour;
                        coutmins = selectedMinute;
                        if(selectedHour<12) {
                            if(selectedMinute<10) {
                                msg.setText("Reminder scheduled for " + Integer.toString(selectedHour) + ":0" + Integer.toString(selectedMinute) + "am");
                            }
                            else{
                                msg.setText("Reminder scheduled for " + Integer.toString(selectedHour) + ":" + Integer.toString(selectedMinute) + "am");
                            }
                        }
                        else if(selectedHour==12){
                            if(selectedMinute<10) {
                                msg.setText("Reminder scheduled for " + Integer.toString(selectedHour) + ":0" + Integer.toString(selectedMinute) + "pm");
                            }
                            else{
                                msg.setText("Reminder scheduled for " + Integer.toString(selectedHour) + ":" + Integer.toString(selectedMinute) + "pm");
                            }
                        }
                        else {
                            if(selectedMinute<10) {
                                msg.setText("Reminder scheduled for " + Integer.toString(selectedHour-12) + ":0" + Integer.toString(selectedMinute) + "pm");
                            }
                            else{
                                msg.setText("Reminder scheduled for " + Integer.toString(selectedHour-12) + ":" + Integer.toString(selectedMinute) + "pm");
                            }
                        }
                        clear.setVisibility(View.VISIBLE);
                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Set Reminder Time");
                mTimePicker.show();
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                couthours=123;
                coutmins= 123;
                msg.setText("No reminder set");
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
                rate.setEnabled(false);
                rate.setText("");
            }
        });
        costly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rate.setEnabled(true);
            }
        });



        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        HomeScreenActivity homeScreenActivity = (HomeScreenActivity) getActivity();
                        homeScreenActivity.setCoutTime(couthours,coutmins); //pass reminder time to activity
                        mListener.onDialogPositiveClick(EditCheckInDialog.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(EditCheckInDialog.this);
                        EditCheckInDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();

    }

    public interface EditCheckInDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    EditCheckInDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            a=(Activity) context;
        }
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the EditCheckInDialogListener so we can send events to the host
            mListener = (EditCheckInDialogListener) a;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(a.toString()
                    + " must implement EditCheckInListener");
        }
    }







}
