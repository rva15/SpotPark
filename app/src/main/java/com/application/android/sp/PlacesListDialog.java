package com.application.android.sp;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;


/**
 * Created by ruturaj on 4/15/17.
 */

public class PlacesListDialog extends DialogFragment {
    //Variable declarations
    private String TAG = "debugger";
    private RecyclerView recList;
    private ProgressBar progressBar;
    private DatabaseReference database;
    private int maxplaces, count = 0;
    private View view;
    private ImageView deleteicon;
    private String UID="",type;
    private TextView defaultmsg,placeslisttitle;
    private ArrayList<Places> placesArrayList = new ArrayList<Places>();
    private ArrayList<String> placesKeysList = new ArrayList<String>();
    private PlacesAdapter placesAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_placeslist, null);
        Bundle mArgs = getArguments();
        UID = (String) mArgs.get("UID");
        type = (String) mArgs.get("type"); //is it an add places list or a veto places list
        progressBar = (ProgressBar) view.findViewById(R.id.placeslist_progressbar);
        defaultmsg = (TextView) view.findViewById(R.id.defaultmsg);
        placeslisttitle = (TextView) view.findViewById(R.id.placeslisttitle);
        deleteicon = (ImageView)view.findViewById(R.id.deleteplaces);
        if(type.equals("Places")){
            placeslisttitle.setText("Remind me at");
        }
        else if(type.equals("Vetoes")){
            placeslisttitle.setText("DO NOT Remind me near");
        }

        builder.setView(view)
                // Add action buttons
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PlacesListDialog.this.getDialog().cancel();
                    }
                });

        recList = (RecyclerView) view.findViewById(R.id.placesrecview);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());     //inflate the recycler view
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        getPlacesData();


        return builder.create();

    }

    private void getPlacesData() {
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
        database.child("ST"+type).child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                maxplaces = (int) dataSnapshot.getChildrenCount();
                if (maxplaces == 0) {
                    showdefault();
                }

                database.child("ST"+type).child(UID).orderByKey().addChildEventListener(listener1);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //define the ChildEventListener
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
            count = count + 1;
            if (count < (maxplaces+1)) {
                final Places places = dataSnapshot.getValue(Places.class);
                placesArrayList.add(places);
                placesKeysList.add(dataSnapshot.getKey());

                if (count == maxplaces) {
                    progressBar.setVisibility(View.GONE);
                    placesAdapter = new PlacesAdapter(placesArrayList,placesKeysList,UID,recList,deleteicon,type,getContext());
                    recList.setAdapter(placesAdapter);
                    database.child("ST"+type).child(UID).orderByKey().removeEventListener(listener1);

                }



            }

        }

            @Override
            public void onChildChanged (DataSnapshot dataSnapshot, String s){

            }

            @Override
            public void onChildRemoved (DataSnapshot dataSnapshot)
            {                 //currently all these functions have been left empty

            }

            @Override
            public void onChildMoved (DataSnapshot dataSnapshot, String s){

            }

            @Override
            public void onCancelled (DatabaseError databaseError){

            }
        };




    private void showdefault() {
        progressBar.setVisibility(View.GONE);
        defaultmsg.setVisibility(View.VISIBLE);
    }




}
