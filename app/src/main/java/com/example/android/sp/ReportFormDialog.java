package com.example.android.sp;
//All imports
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
import android.view.View;
import android.widget.TimePicker;

/**
 * Created by ruturaj on 8/26/16.
 */
public class ReportFormDialog extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_reportform, null);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Report", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        TimePicker timePicker = (TimePicker) view.findViewById(R.id.rftimepicker);
                        String TAG="debugger";
                        int hours,mins;
                        if (Build.VERSION.SDK_INT >= 23 ) {            //Use the correct method according to API levels
                            hours =  timePicker.getHour();
                        }
                        else {
                            hours =  timePicker.getCurrentHour();
                        }
                        if (Build.VERSION.SDK_INT >= 23 ) {
                            mins =  timePicker.getMinute();
                        }
                        else{
                            mins = timePicker.getCurrentMinute();
                        }
                        Intent i = new Intent().putExtra("hour", hours);
                        i.putExtra("mins", mins);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ReportFormDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();

    }






}
