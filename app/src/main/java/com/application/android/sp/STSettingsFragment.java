package com.application.android.sp;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 4/8/17.
 */

public class STSettingsFragment extends Fragment implements View.OnClickListener {
    //Variable Declaration
    private View view;
    private SupportPlaceAutocompleteFragment addPlace,vetoPlace;
    private TextView placeaddedmsg, vetoaddedmsg, placeslist, vetoeslist;
    private DatabaseReference database;
    private String UID="";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_stsettings, container, false);
        super.onCreate(savedInstanceState);
        Bundle extras = getArguments();
        UID = extras.getString("userid");
        placeaddedmsg = (TextView) view.findViewById(R.id.placeaddedmsg);
        //vetoaddedmsg = (TextView) view.findViewById(R.id.vetoaddedmsg);
        placeslist = (TextView) view.findViewById(R.id.placeslist);
        //vetoeslist = (TextView) view.findViewById(R.id.vetoeslist);
        placeslist.setOnClickListener(this);
        //vetoeslist.setOnClickListener(this);
        initializeAutoComplete(); //get the google place autocomplete fragments


        return view;
    }

    private void initializeAutoComplete() {
        addPlace = new SupportPlaceAutocompleteFragment();

        addPlace.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                placeaddedmsg.setText(place.getName()+" added to list");
                Places places = new Places((String)place.getName(),place.getLatLng().latitude,place.getLatLng().longitude);
                database = FirebaseDatabase.getInstance().getReference();
                Map<String, Object> placesMap = places.toMap();
                Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
                final String key = database.child("STPlaces/"+UID).push().getKey();
                childUpdates.put("/STPlaces/"+UID+"/"+key,placesMap);
                database.updateChildren(childUpdates);

            }
            @Override
            public void onError(Status status) {

            }
        });
        FragmentManager mgr1 = getFragmentManager();
        FragmentTransaction transaction1 = mgr1.beginTransaction();
        transaction1.replace(R.id.addplace,addPlace, "AutoSearchFragment");
        transaction1.commitAllowingStateLoss();

        //vetoPlace = new SupportPlaceAutocompleteFragment();

        /*vetoPlace.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                vetoaddedmsg.setText(place.getName()+" added to list");
                Places places = new Places((String)place.getName(),place.getLatLng().latitude,place.getLatLng().longitude);
                database = FirebaseDatabase.getInstance().getReference();
                Map<String, Object> placesMap = places.toMap();
                Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
                final String key = database.child("STVetoes/"+UID).push().getKey();
                childUpdates.put("/STVetoes/"+UID+"/"+key,placesMap);
                database.updateChildren(childUpdates);

            }
            @Override
            public void onError(Status status) {

            }
        });
        FragmentManager mgr2 = getFragmentManager();
        FragmentTransaction transaction2 = mgr2.beginTransaction();
        transaction2.replace(R.id.vetoplace,vetoPlace, "AutoSearchFragment");
        transaction2.commitAllowingStateLoss();*/
    }

    @Override
    public void onClick(View v){
        if(v.getId()==R.id.placeslist){
            showPlacesListDialog("Places");
        }
        /*if(v.getId()==R.id.vetoeslist){
            showPlacesListDialog("Vetoes");
        }*/
    }

    private void showPlacesListDialog(String type) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new PlacesListDialog();
        Bundle args = new Bundle();
        args.putString("UID",UID);
        args.putString("type",type); //pass the type of list to be shown
        dialog.setArguments(args);
        dialog.setTargetFragment(STSettingsFragment.this, 33);       //set target fragment to this fragment
        dialog.show(this.getActivity().getSupportFragmentManager(),"STSettings fragment");
    }
}
