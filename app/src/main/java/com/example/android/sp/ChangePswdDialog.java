package com.example.android.sp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * Created by ruturaj on 12/10/16.
 */
public class ChangePswdDialog extends DialogFragment {
    Activity a;
    static String TAG="debugger";


    public ChangePswdDialog() {
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.changepswddialog, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText oldpswd = (EditText) view.findViewById(R.id.oldpswd);
                        EditText newpswd1 = (EditText) view.findViewById(R.id.newpswd1);
                        EditText newpswd2 = (EditText) view.findViewById(R.id.newpswd2);
                        String old = oldpswd.getText().toString();
                        String new1 = newpswd1.getText().toString();
                        String new2 = newpswd2.getText().toString();
                        Log.d(TAG,"new1 "+new1);
                        Log.d(TAG,"new2 "+new2);
                        Intent i = new Intent();
                        i.putExtra("oldpswd", old);
                        i.putExtra("newpswd1", new1);
                        i.putExtra("newpswd2", new2);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        ChangePswdDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();


    }

}