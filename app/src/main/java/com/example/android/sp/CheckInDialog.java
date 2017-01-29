package com.example.android.sp;
// All imports
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.view.View;
import android.widget.Spinner;
import android.widget.TimePicker;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruturaj on 8/26/16.
 */
public class CheckInDialog extends DialogFragment {
    //Variable Declarations
    private Activity a;
    private EditText rph;
    private TimePicker timePicker;
    private String hourlyrate;
    private double hours,mins;
    private String hour,min;
    public CheckInDialog(){}


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_checkin, null);

        // Get views and declare UI components
        final Spinner spin;
        spin = (Spinner)view.findViewById(R.id.spinner1);
        final CheckBox remind = (CheckBox)view.findViewById(R.id.remind);
        final CheckBox tagfavorite = (CheckBox) view.findViewById(R.id.fav);
        final String TAG="debugger";
        List<String> list = new ArrayList<String>();
        list.add("15");
        list.add("30");
        list.add("45");
        list.add("60");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(dataAdapter);


        builder.setView(view)
                // Add action buttons
                .setPositiveButton("CheckIn", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String checked="1";    //default values for checked and favorite
                        String favorite="0";
                        if(!remind.isChecked()){
                            checked = "0";
                        }
                        if(tagfavorite.isChecked()){  //check user inputs
                            favorite="1";
                        }
                        String text = spin.getSelectedItem().toString();
                        rph = (EditText) view.findViewById(R.id.rate);
                        timePicker = (TimePicker) view.findViewById(R.id.time);
                        hourlyrate = rph.getText().toString();
                        if (Build.VERSION.SDK_INT >= 23 ) {            //Use the correct method according to API levels
                            hours = (double) timePicker.getHour();
                        }
                        else {
                            hours = (double) timePicker.getCurrentHour();
                        }
                        if (Build.VERSION.SDK_INT >= 23 ) {
                            mins = (double) timePicker.getMinute();
                        }
                        else{
                            mins = (double) timePicker.getCurrentMinute();
                        }
                        hour = Double.toString(hours);
                        min = Double.toString(mins);
                        Intent i = new Intent().putExtra("rates",hourlyrate);      //bundle all the information and send to CheckIn fragment
                        i.putExtra("hours",hour);
                        i.putExtra("mins",min);
                        i.putExtra("option",text);
                        i.putExtra("checked",checked);
                        i.putExtra("tag",favorite);
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
