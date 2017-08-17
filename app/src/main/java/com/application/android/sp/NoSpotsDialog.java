package com.application.android.sp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import static com.application.android.sp.R.id.endtime;
import static com.application.android.sp.R.id.starttime;

/**
 * Created by ruturaj on 8/9/17.
 */

public class NoSpotsDialog extends android.support.v4.app.DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_nospots, null);

        builder.setView(view)
                // Add action buttons
                .setNegativeButton("Got It", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        NoSpotsDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

}
