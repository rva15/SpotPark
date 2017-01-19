package com.example.android.sp;
// All imports
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.view.View;
import android.widget.Spinner;
import android.widget.TimePicker;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruturaj on 8/26/16.
 */
public class EditCheckInDialog extends DialogFragment {
    //Variable Declarations
    private Activity a;


    public EditCheckInDialog(){}   //empty constructor


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_editcheckin, null);
        Bundle args = getArguments();
        String dollars = Integer.toString(args.getInt("dollars"));
        String cents = Integer.toString(args.getInt("cents"));
        // Get views and declare UI components
        final Spinner spin;
        spin = (Spinner)view.findViewById(R.id.editspinner1);
        final String TAG="debugger";
        List<String> list = new ArrayList<String>();
        list.add("15");
        list.add("30");
        list.add("45");
        list.add("60");
        EditText rate = (EditText) view.findViewById(R.id.editrate); //set the previous rate to edittext
        rate.setText(dollars+"."+cents);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(dataAdapter);


        builder.setView(view)
                // Add action buttons
                .setPositiveButton("CheckIn", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
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
