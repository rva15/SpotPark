package com.app.android.sp;
//All imports
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

/**
 * Created by ruturaj on 1/9/17.
 */
public class ComplainDialog extends DialogFragment {

    private String TAG="debugger";

    public ComplainDialog(){}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_complain, null);
        Bundle mArgs = getArguments();
        final Boolean isReported = mArgs.getBoolean("isReported");   //ask questions based on type of spot

        //Get UI elements and set their listeners
        final CardView fbackcard2 = (CardView) view.findViewById(R.id.fbackcard2);
        final RadioButton fbackyes = (RadioButton) view.findViewById(R.id.fbackyes);
        fbackyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbackcard2.setVisibility(View.GONE);      //show card2 based on answer for card1
            }
        });
        RadioButton fbackno = (RadioButton) view.findViewById(R.id.fbackno);
        fbackno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbackcard2.setVisibility(View.VISIBLE);
            }
        });

        //choose questions based on type of spot
        final RadioButton radioButton = (RadioButton)view.findViewById(R.id.reportonly);
        final RadioButton radioButton1 = (RadioButton)view.findViewById(R.id.notav);
        final RadioButton radioButton2 = (RadioButton) view.findViewById(R.id.nospace);
        if(!isReported){
            radioButton.setVisibility(View.GONE);
        }
        if(isReported){
            radioButton1.setVisibility(View.GONE);
        }


        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Boolean yes   = fbackyes.isChecked();
                        Boolean notav = radioButton1.isChecked();
                        Boolean nospace = radioButton2.isChecked();
                        Boolean notfree = false;
                        if(isReported){
                            notfree = radioButton.isChecked();  //this button only valid for reported spots
                        }
                        Intent i = new Intent();
                        i.putExtra("yes",yes);
                        i.putExtra("notav",notav);
                        i.putExtra("nospace",nospace);
                        i.putExtra("notfree",notfree);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        ComplainDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }
}
