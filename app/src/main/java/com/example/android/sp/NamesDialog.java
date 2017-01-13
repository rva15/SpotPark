package com.example.android.sp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

/**
 * Created by ruturaj on 10/12/16.
 */
public class NamesDialog extends DialogFragment {
    Activity a;
    EditText rph;
    TimePicker timePicker;
    String hourlyrate;
    double hours, mins;
    String hour, min;

    public NamesDialog() {
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_names, null);
        Bundle mArgs = getArguments();
        final double latitude = mArgs.getDouble("latitude");
        final double longitude = mArgs.getDouble("longitude");
        final Bitmap bitmap = mArgs.getParcelable("bitmap");
        final String key    = mArgs.getString("key");

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editText = (EditText) view.findViewById(R.id.spotname);
                        String spotname = editText.getText().toString();
                        Intent i = new Intent().putExtra("rates", hourlyrate);
                        i.putExtra("spotname", spotname);
                        i.putExtra("latitude",latitude);
                        i.putExtra("longitude",longitude);
                        i.putExtra("bitmap",bitmap);
                        i.putExtra("key",key);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        NamesDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();


    }

}
