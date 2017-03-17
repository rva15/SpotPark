package com.app.android.sp;

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
import android.widget.ImageView;

/**
 * Created by ruturaj on 12/11/16.
 */
public class EditProfileDialog extends DialogFragment {
    Activity a;
    static String TAG="debugger";


    public EditProfileDialog() {
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_editprofile, null);
        Bundle mArgs = getArguments();
        final String firstname = mArgs.getString("fn");
        final String lastname = mArgs.getString("ln");
        final String emailid = mArgs.getString("email");
        final EditText editfn = (EditText) view.findViewById(R.id.editfn);
        editfn.setText(firstname);
        final EditText editln = (EditText) view.findViewById(R.id.editln);
        editln.setText(lastname);
        final EditText editemail = (EditText) view.findViewById(R.id.editemail);
        editemail.setText(emailid);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        String fn = editfn.getText().toString();
                        String ln = editln.getText().toString();
                        String email = editemail.getText().toString();

                        Intent i = new Intent();
                        i.putExtra("fn", fn);
                        i.putExtra("ln", ln);
                        i.putExtra("email", email);
                        i.putExtra("changedp",false);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        EditProfileDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();


    }
}
